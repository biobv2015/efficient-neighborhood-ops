
package de.squareys.nhbench.minfilter;

import de.squareys.nhbench.main.NeighborhoodBenchmarks;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Filters3D;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.map.neighborhood.array.MapNeighborhoodNativeType;
import net.imagej.ops.map.neighborhood.array.MapNeighborhoodNativeTypeExtended;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
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

import sandbox.MinimumFilter;

/**
 * Benchmark for iterating through a {@link IterableInterval}<
 * {@link Neighborhood}<{@link FloatType}>> completely.
 * 
 * @param sigma Size of the neighborhood
 * @param library Library to perform the minimum filter with
 * @author Jonathan Hale (University of Konstanz)
 */
@State(Scope.Thread)
public class MinimumFilterBenchmark {

	final static ImageJ ij = new ImageJ();

	final static String IMAGEJ_OPS = "imagej-ops";
	final static String IMAGEJ_OPS_EXTENDED = "imagej-ops-extended";
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
		public static String filename = "./flybrain-32bit.tif";

		public Img<FloatType> image;
		public ImagePlus im;

		public Img<FloatType> output;

		@Setup
		public void setup() throws IOException {
			// open the image file
			Dataset ds = (Dataset) ij.io().open(filename);
			Img<FloatType> planarin = (Img<FloatType>) ds
					.getImgPlus().getImg();
			image = new ArrayImgFactory<FloatType>().create(planarin,
					planarin.firstElement());

			Cursor<FloatType> c1 = planarin.cursor();
			Cursor<FloatType> c2 = image.cursor();

			while (c1.hasNext()) {
				c2.next().set(c1.next());
			}

			output = image.factory().create(image, new FloatType());

			// create a new Image with the same properties
			output = image.factory().create(image, image.firstElement());

			IJ.openImage(MinimumFilter.fileName);

			im = ImageJFunctions.wrap(image, "input");
		}
	}

	@Param({ "1", "2", "4" })
	private String sigma;
	private int sigma_i;

	@Param({ IMAGEJ_OPS_EXTENDED, IMAGEJ_OPS,	IMGLIB2 })
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

		if (IMAGEJ_OPS_EXTENDED.equals(library)) {
			final Op op =
				ij.op().op(
					MapNeighborhoodNativeTypeExtended.class,
					state.output,
					state.image,
					ij.op().op(Ops.Stats.Min.class, state.output.firstElement(),
						Iterable.class), new RectangleShape(sigma_i, false),
					new OutOfBoundsMirrorFactory(Boundary.SINGLE));

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
