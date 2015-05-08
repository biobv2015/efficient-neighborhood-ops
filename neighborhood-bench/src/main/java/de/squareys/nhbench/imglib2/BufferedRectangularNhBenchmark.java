package de.squareys.nhbench.imglib2;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.BufferedRectangularNeighborhood;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.real.FloatType;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import de.squareys.nhbench.main.NeighborhoodBenchmarks;

/**
 * Benchmark for imglib2 {@link BufferedRectangularNeighborhood}.
 * 
 * @author Jonathan Hale (University of Konstanz)
 */
public class BufferedRectangularNhBenchmark {

	/**
	 * State containing an {@link Img}<{@link FloatType}> to remove Img creation
	 * time from iteration benchmarks.
	 * 
	 * @author Jonathan Hale (University of Konstanz)
	 */
	@State(Scope.Benchmark)
	public static class ImgState {
		public final Img<FloatType> img = new ArrayImgFactory<FloatType>()
				.create(new int[] { 100, 100 }, new FloatType());
	}

	/**
	 * State containing an {@link Img}<{@link FloatType}> and
	 * {@link BufferedRectangularNeighborhood} to remove creation time from
	 * iteration benchmarks.
	 * 
	 * @author Jonathan Hale (University of Konstanz)
	 */
	@State(Scope.Benchmark)
	public static class NeighborhoodState extends ImgState {
		public final Img<FloatType> img = new ArrayImgFactory<FloatType>()
				.create(new int[] { 100, 100 }, new FloatType());
		public final BufferedRectangularNeighborhood<FloatType> neighborhood = new BufferedRectangularNeighborhood<FloatType>(
				img,
				new OutOfBoundsBorderFactory<FloatType, RandomAccessibleInterval<FloatType>>(),
				new long[] { 3, 3 });
	}

	/**
	 * Benchmark the creation of a {@link BufferedRectangularNeighborhood} on a
	 * {@link Img}<{@link FloatType}>.
	 * 
	 * @param imgState
	 *            set by jmh
	 * @return something senseless so that the neighborhood wont be optimized
	 *         out
	 */
	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public int createNeighborhood(ImgState imgState) {
		BufferedRectangularNeighborhood<FloatType> neighborhood = new BufferedRectangularNeighborhood<FloatType>(
				imgState.img,
				new OutOfBoundsBorderFactory<FloatType, RandomAccessibleInterval<FloatType>>(),
				new long[] { 3, 3 });

		// use neighborhood so that it doesn't get optimized out
		// TODO is this really neccessary
		return (int) (neighborhood.dimension(0) + 1);
	}

	/**
	 * Benchmark the time it takes to iterate through the
	 * {@link BufferedRectangularNeighborhood}.
	 * 
	 * @param state
	 *            set by jmh
	 */
	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void iterateThroughNeighborhood(NeighborhoodState state) {
		Iterator<FloatType> itor = state.neighborhood.iterator();
		while (itor.hasNext()) {
			FloatType t = itor.next();

			// do something with the value to avoid optimizing out
			t.add(new FloatType(1.0f));
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
		NeighborhoodBenchmarks
				.runBenchmark(BufferedRectangularNhBenchmark.class
						.getSimpleName());
	}
}
