package de.squareys.nhbench.minfilter;

import de.squareys.nhbench.main.NeighborhoodBenchmarks;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Filters3D;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.map.neighborhood.array.MapNeighborhoodNativeType;
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
import net.imglib2.type.numeric.integer.UnsignedByteType;
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
 * @param useOutOfBounds
 *            (true/false) extend the created image with a border
 * @param iterationType
 *            (foreach/while) iterate with foreach or while(hasNext)
 * @param iteratorType
 *            (safe/unsafe) type of the neighborhoods iterator
 * 
 * @author Jonathan Hale (University of Konstanz)
 *
 */
@State(Scope.Thread)
public class MinimumFilterBenchmark2D {
	final static ImageJ ij = new ImageJ();

	/**
	 * State which creates holds an image. It is Thread Scope, since the pixels
	 * are incremented during the benchmark, which may result in the JIT
	 * optimizing access to the image over the execution of multiple benchmarks.
	 * 
	 * @author Jonathan Hale (University of Konstanz)
	 */
	@State(Scope.Benchmark)
	public static class ImageState {
		public static String fileName = "./lena_grey.jpeg";

		public Img<UnsignedByteType> image;
		public ImagePlus im;

		public Img<UnsignedByteType> output;

		@Setup
		public void setup() throws IOException {
			// define the file to open
			final File file = new File(fileName);

			// open the image file
			Dataset ds = (Dataset) ij.io().open("lena_grey.jpeg");
			Img<UnsignedByteType> planarin = (Img<UnsignedByteType>) ds
					.getImgPlus().getImg();
			image = new ArrayImgFactory<UnsignedByteType>().create(planarin,
					planarin.firstElement());

			Cursor<UnsignedByteType> c1 = planarin.cursor();
			Cursor<UnsignedByteType> c2 = image.cursor();

			while (c1.hasNext()) {
				c2.next().set(c1.next());
			}

			output = image.factory().create(image, new UnsignedByteType());

			// create a new Image with the same properties
			output = image.factory().create(image, image.firstElement());

			IJ.openImage(MinimumFilter.fileName);

			im = ImageJFunctions.wrap(image, "input");
		}
	}

	@Param({ "1", "2", "4" })
	private String sigma;
	private int sigma_i;

	@Param({ "imagej-ops", "imagej1", "imglib2-optimized", "imglib2", })
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
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void minimumFilter(ImageState state) {
		if ("imglib2".equals(library)) {
			// get mirror view
			final IntervalView<UnsignedByteType> infinite = Views.interval(
					Views.extendMirrorSingle(state.image), state.image);

			final Shape shape = new RectangleShape((int) sigma_i, false);

			final RandomAccess<UnsignedByteType> ra = state.output
					.randomAccess();

			final UnsignedByteType min = new UnsignedByteType();

			for (final Neighborhood<UnsignedByteType> neighborhood : shape
					.neighborhoods(infinite)) {

				final Cursor<UnsignedByteType> cursor = neighborhood.cursor();
				min.setReal(min.getMaxValue());

				while (cursor.hasNext()) {
					cursor.fwd();

					final UnsignedByteType val = cursor.get();
					if (val.compareTo(min) < 0) {
						min.set(val);
					}
				}

				ra.setPosition(neighborhood);
				ra.get().set(min);
			}
		} else if ("imagej".equals(library)) { /* ImageJ1 */
			final ImageStack is = Filters3D.filter(state.im.getImageStack(),
					Filters3D.MIN, (float) sigma_i, (float) sigma_i,
					(float) sigma_i);
			final ImagePlus ip = new ImagePlus("Minimum_sigma=" + sigma_i, is);
			state.output = ImagePlusAdapter.wrap(ip);
		} else if ("imglib2-optimized".equals(library)) {
			// get mirror view
			final IntervalView<UnsignedByteType> infinite = Views.interval(
					Views.extendMirrorSingle(state.image), state.image);

			final net.imglib2.algorithm.neighborhood.Shape shape = new net.imglib2.algorithm.neighborhood.RectangleShape(
					(int) sigma_i, false);

			final RandomAccess<UnsignedByteType> ra = state.output
					.randomAccess();

			final UnsignedByteType min = new UnsignedByteType();

			for (final net.imglib2.algorithm.neighborhood.Neighborhood<UnsignedByteType> neighborhood : shape
					.neighborhoods(infinite)) {

				final Cursor<UnsignedByteType> cursor = neighborhood.cursor();
				min.setReal(min.getMaxValue());

				while (cursor.hasNext()) {
					cursor.fwd();

					final UnsignedByteType val = cursor.get();
					if (val.compareTo(min) < 0) {
						min.set(val);
					}
				}

				ra.setPosition(neighborhood);
				ra.get().set(min);
			}
		} else if ("imagej-ops".equals(library)) {
			final Op op = ij.op().op(
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
	 * @param args
	 *            do nothing
	 * @throws RunnerException
	 *             thrown when jmh runs into trouble
	 */
	public static void main(String[] args) throws RunnerException {
		NeighborhoodBenchmarks.runBenchmark(MinimumFilterBenchmark2D.class
				.getSimpleName());
	}
}