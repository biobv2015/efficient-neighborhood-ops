
package de.squareys.nhbench.minfilter;

import de.squareys.nhbench.main.NeighborhoodBenchmarks;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Filters3D;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

import java.io.File;
import java.util.concurrent.TimeUnit;

import net.imagej.ImageJ;
import net.imagej.ops.DefaultOpService;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.map.neighborhood.array.MapNeighborhoodNativeType;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;
import org.scijava.Context;

/**
 * Benchmark for iterating through a {@link IterableInterval}<
 * {@link Neighborhood}<{@link FloatType}>> completely.
 * 
 * @param useOutOfBounds (true/false) extend the created image with a border
 * @param iterationType (foreach/while) iterate with foreach or while(hasNext)
 * @param iteratorType (safe/unsafe) type of the neighborhoods iterator
 * @author Jonathan Hale (University of Konstanz)
 */
@State(Scope.Thread)
public class MinimumFilterBenchmark {

	final static ImageJ ij = new ImageJ();

	final static String IMAGEJ_OPS = "imagej-ops";
	final static String IMGLIB2 = "imglib2";
	final static String IMAGEJ1 = "imagej";

	/**
	 * State which creates holds an image. It is Thread Scope, since the pixels
	 * are incremented during the benchmark, which may result in the JIT
	 * optimizing access to the image over the execution of multiple benchmarks.
	 * 
	 * @author Jonathan Hale (University of Konstanz)
	 */
	@State(Scope.Benchmark)
	public static class ImageState {

		public static String fileName = "./flybrain-32bit.tif";

		public Img<FloatType> image;
		public ImagePlus im;

		public Img<FloatType> output;

		@Setup
		public void setup() throws ImgIOException {
			// define the file to open
			final File file = new File(fileName);

			// open a file with ImageJ
			image = (Img<FloatType>) new ImgOpener().openImg(file.getAbsolutePath());

			// create a new Image with the same properties
			output = image.factory().create(image, image.firstElement());

			System.out.println("Opening ij1 image");
			im = IJ.openImage(fileName);
			System.out.println("... done");
		}
	}

	@Param({ "1", "2"/* , "4" */})
	private String sigma;
	private int sigma_i;

	@Param({ IMAGEJ1, IMAGEJ_OPS, /* "imglib2-optimized", */IMGLIB2 })
	private String library;

	/**
	 * Setup the state of this benchmark.
	 * 
	 * @param imgState
	 */
	@Setup
	public void setup(ImageState imgState) {
		sigma_i = Integer.parseInt(sigma);
	}

	/**
	 * Iterate through the neighborhoods of all the pixels and increase their
	 * values by one.
	 */
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void minimumFilter(ImageState state) {
		if (IMGLIB2.equals(library)) {
			// get mirror view
			final IntervalView<FloatType> infinite =
				Views.interval(Views.extendMirrorSingle(state.image), state.image);

			final Shape shape =
				new net.imglib2.algorithm.neighborhood.RectangleShape((int) sigma_i,
					false);

			final RandomAccess<FloatType> ra = state.output.randomAccess();

			final FloatType min = new FloatType();

			for (final Neighborhood<FloatType> neighborhood : shape
				.neighborhoods(infinite))
			{

				final Cursor<FloatType> cursor = neighborhood.cursor();
				min.setReal(min.getMaxValue());

				while (cursor.hasNext()) {
					cursor.fwd();

					final FloatType val = cursor.get();
					if (val.compareTo(min) < 0) {
						min.set(val);
					}
				}

				ra.setPosition(neighborhood);
				ra.get().set(min);
			}
		}
		
		if (IMAGEJ1.equals(library)) { /* ImageJ1 */
			Prefs.setThreads(1);

			final ImageStack is =
				Filters3D.filter(state.im.getImageStack(), Filters3D.MIN, sigma_i,
					sigma_i, sigma_i);

			final ImagePlus ip = new ImagePlus("Minimum_sigma=" + sigma_i, is);
			state.output = ImagePlusAdapter.wrap(ip);

		}
		
		if ("imglib2-optimized".equals(library)) {
			// get mirror view
			final IntervalView<FloatType> infinite =
				Views.interval(Views.extendMirrorSingle(state.image), state.image);

			final net.imglib2.algorithm.neighborhood.Shape shape =
				new net.imglib2.algorithm.neighborhood.RectangleShape(sigma_i, false);

			final RandomAccess<FloatType> ra = state.output.randomAccess();

			final FloatType min = new FloatType();

			for (final net.imglib2.algorithm.neighborhood.Neighborhood<FloatType> neighborhood : shape
				.neighborhoods(infinite))
			{

				final Cursor<FloatType> cursor = neighborhood.cursor();
				min.setReal(min.getMaxValue());

				while (cursor.hasNext()) {
					cursor.fwd();

					final FloatType val = cursor.get();
					if (val.compareTo(min) < 0) {
						min.set(val);
					}
				}

				ra.setPosition(neighborhood);
				ra.get().set(min);
			}
		}
		
		if (IMAGEJ_OPS.equals(library)) {
			final Op op =
				ij.op().op(
					MapNeighborhoodNativeType.class,
					state.output,
					state.image,
					ij.op().op(Ops.Stats.Min.class, state.output.firstElement(),
						Iterable.class), sigma_i);

			op.run();
		}
	}

	/**
	 * Run this benchmark separately.
	 * 
	 * @param args do nothing
	 * @throws RunnerException thrown when jmh runs into trouble
	 */
	public static void main(String[] args) throws RunnerException {
		NeighborhoodBenchmarks.runBenchmark(MinimumFilterBenchmark.class
			.getSimpleName());
	}
}
