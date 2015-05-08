package de.squareys.nhbench.main;

import java.util.Arrays;
import java.util.List;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import de.squareys.nhbench.imglib2.BufferedRectangularNhBenchmark;

/**
 * Main class, contains main function and is responsible for running the
 * benchmarks via {@link Runner}.
 * 
 * Runs all benchmarks in the
 * 
 * @author Jonathan Hale (University of Konstanz)
 *
 */
public class NeighborhoodBenchmarks {

	/**
	 * List of benchmarks in the project.
	 */
	public static List<String> benchmarks = Arrays
			.asList(BufferedRectangularNhBenchmark.class.getSimpleName());

	/**
	 * Creates a OptionsBuilder with default settings:
	 * warmupIterations = 5
	 * forks = 1
	 * threads = 4
	 * result = result.csv
	 * 
	 * @return {@link ChainedOptionsBuilder} with some default settings
	 */
	public static ChainedOptionsBuilder createDefaultOptionsBuilder() {
		return new OptionsBuilder()
			.warmupIterations(5)
			.forks(1)
			.threads(4)
			.result("result.csv")
			.resultFormat(ResultFormatType.CSV);
	}

	/**
	 * Main function.
	 * 
	 * @param args
	 *            do nothing
	 * @throws RunnerException
	 *             thrown when jmh runs into trouble
	 */
	public static void main(String[] args) throws RunnerException {
		ChainedOptionsBuilder builder = createDefaultOptionsBuilder();

		for (String benchmarkName : benchmarks) {
			builder.include(benchmarkName);
		}

		Options opt = builder.build();

		new Runner(opt).run();
	}

	/**
	 * Method to run a single benchmark.
	 * 
	 * @param simpleName
	 * @throws RunnerException
	 *             thrown when jmh runs into trouble
	 */
	public static void runBenchmark(String simpleName) throws RunnerException {
		Options opt = createDefaultOptionsBuilder().include(simpleName).build();
		
		new Runner(opt).run();
	}
}
