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
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class NeighborhoodBenchmark {

	@State(Scope.Benchmark)
	public static class ImgState {
		public final Img<FloatType> img = new ArrayImgFactory<FloatType>().create(new int[] {
				100, 100 }, new FloatType());
	}
	
	@State(Scope.Benchmark)
	public static class NeighborhoodState {
		public final Img<FloatType> img = new ArrayImgFactory<FloatType>().create(new int[] {
				100, 100 }, new FloatType());
	}
	
	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public int createNeighborhood(ImgState imgState) {
		BufferedRectangularNeighborhood<FloatType> neighborhood = new BufferedRectangularNeighborhood<FloatType>(
				imgState.img, new OutOfBoundsBorderFactory<FloatType, RandomAccessibleInterval<FloatType>>(),
				new long[] { 3, 3 });
		
		// use neighborhood so that it doesn't get optimized out
		return (int) (neighborhood.dimension(0) + 1);
	}
	
	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void iterateThroughNeighborhood(ImgState imgState) {
		BufferedRectangularNeighborhood<FloatType> neighborhood = new BufferedRectangularNeighborhood<FloatType>(
				imgState.img, new OutOfBoundsBorderFactory<FloatType, RandomAccessibleInterval<FloatType>>(),
				new long[] { 3, 3 });
		
		Iterator<FloatType> itor = neighborhood.iterator();
		while (itor.hasNext()) {
			FloatType t = itor.next();
			t.add(new FloatType(1.0f));
		}
	}
	
	public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NeighborhoodBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .forks(1)
                .threads(4)
                .result("result.tex")
                .resultFormat(ResultFormatType.LATEX)
                .build();
        
        new Runner(opt).run();
    }

}
