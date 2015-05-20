package de.squareys.nhbench.imglib2;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsIterableInterval;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import de.squareys.nhbench.main.NeighborhoodBenchmarks;

/**
 * Benchmark for iterating through a {@link NeighborhoodsIterableInterval}
 * completely.
 * 
 * @author Jonathan Hale
 *
 */
@State(Scope.Thread)
public class IterateNeighborhoodsBenchmark {

	@State(Scope.Benchmark)
	public static class ImageState {
		RandomAccessibleInterval<FloatType> img;

		@Param({ "true", "false" })
		private String useOutOfBounds;

		@Setup
		public void setup() {
			img = new ArrayImgFactory<FloatType>().create(
					new long[] { 100, 100 }, new FloatType());

			if (Boolean.parseBoolean(useOutOfBounds)) {
				img = Views.interval(Views.extendBorder(img), img);
			}
		}
	}

	private NeighborhoodsIterableInterval<FloatType> neighborhoods;

	@Param({ "foreach", "while" })
	private String iterateType;

	@Param({ "safe", "unsafe" })
	private String iteratorType;

	/**
	 * Setup the state of this benchmark.
	 * 
	 * @param imgState
	 */
	@Setup
	public void setup(ImageState imgState) {
		if ("safe".equals(iterateType)) {
			neighborhoods = (NeighborhoodsIterableInterval<FloatType>) new RectangleShape(
					3, true).neighborhoodsSafe(imgState.img);
		} else {
			neighborhoods = (NeighborhoodsIterableInterval<FloatType>) new RectangleShape(
					3, true).neighborhoods(imgState.img);
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void iterateThroughNeighborhood(Blackhole O) {
		if ("foreach".equals(iterateType)) {
			for (Neighborhood<?> s : neighborhoods) {
				for (Object t : s) {
					O.consume(t);
				}
			}
		} else if ("while".equals(iterateType)) {
			Iterator<Neighborhood<FloatType>> outeritor = neighborhoods
					.iterator();

			while (outeritor.hasNext()) {
				Iterator<FloatType> inneritor = outeritor.next().iterator();
				while (inneritor.hasNext()) {
					O.consume(inneritor.next());
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
