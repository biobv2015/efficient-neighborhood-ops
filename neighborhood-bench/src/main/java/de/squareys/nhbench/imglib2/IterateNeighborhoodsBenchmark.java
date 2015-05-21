package de.squareys.nhbench.imglib2;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;
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

import de.squareys.nhbench.main.NeighborhoodBenchmarks;

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
public class IterateNeighborhoodsBenchmark {

	public static final int SPAN = 3;

	/**
	 * State which creates holds an image. It is Thread Scope, since the pixels
	 * are incremented during the benchmark, which may result in the JIT
	 * optimizing access to the image over the execution of multiple benchmarks.
	 * 
	 * @author Jonathan Hale (University of Konstanz)
	 */
	@State(Scope.Thread)
	public static class ImageState {
		RandomAccessibleInterval<FloatType> img;

		@Param({ "false", "true" })
		private String useOutOfBounds;

		@Setup
		public void setup() {
			// for non-outOfBounds, we need to make sure, all iterated pixel can
			// be accessed.
			final long[] iteratedAreaSize = { 100, 100 };

			if (Boolean.parseBoolean(useOutOfBounds)) {
				img = Views.interval(Views
						.extendBorder(new ArrayImgFactory<FloatType>().create(
								iteratedAreaSize, new FloatType())),
						new long[] { 0, 0 }, iteratedAreaSize);
			} else {
				final long[] size = new long[] {
						iteratedAreaSize[0] + 2 * SPAN,
						iteratedAreaSize[1] + 2 * SPAN };
				final long[] min = new long[] { SPAN, SPAN };
				final long[] max = new long[] { size[0] - SPAN - 1,
						size[1] - SPAN - 1 };

				img = Views.interval(new ArrayImgFactory<FloatType>().create(
						size, new FloatType()), min, max);
			}

		}
	}

	private IterableInterval<Neighborhood<FloatType>> neighborhoods;

	@Param({ "foreach", "while" })
	private String iterationType;

	@Param({ "safe", "unsafe" })
	private String iteratorType;

	/**
	 * Setup the state of this benchmark.
	 * 
	 * @param imgState
	 */
	@Setup
	public void setup(ImageState imgState) {
		if ("safe".equals(iterationType)) {
			neighborhoods = new RectangleShape(SPAN, true)
					.neighborhoodsSafe(imgState.img);
		} else {
			neighborhoods = new RectangleShape(SPAN, true)
					.neighborhoods(imgState.img);
		}
	}

	/**
	 * Iterate through the neighborhoods of all the pixels and increase their
	 * values by one.
	 */
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void iterateThroughNeighborhood() {
		if ("foreach".equals(iterationType)) {
			for (Neighborhood<FloatType> s : neighborhoods) {
				for (FloatType t : s) {
					t.set(t.get() + 1.0f);
				}
			}
		} else if ("while".equals(iterationType)) {
			Iterator<Neighborhood<FloatType>> outeritor = neighborhoods
					.iterator();

			while (outeritor.hasNext()) {
				Iterator<FloatType> inneritor = outeritor.next().iterator();
				while (inneritor.hasNext()) {
					FloatType t = inneritor.next();
					t.set(t.get() + 1.0f);
				}
			}
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
		NeighborhoodBenchmarks.runBenchmark(IterateNeighborhoodsBenchmark.class
				.getSimpleName());
	}
}
