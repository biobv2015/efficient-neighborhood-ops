package de.squareys.nhbench.main;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import de.squareys.nhbench.imglib2.CreateShapeBenchmark;
import de.squareys.nhbench.imglib2.IterateNeighborhoodsBenchmark;
import de.squareys.nhbench.minfilter.MinimumFilterBenchmark;

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
	public static List<String> benchmarks = Arrays.asList(
			IterateNeighborhoodsBenchmark.class.getSimpleName(),
			MinimumFilterBenchmark.class.getSimpleName(),
			CreateShapeBenchmark.class.getSimpleName());

	/**
	 * Creates a OptionsBuilder with default settings: warmupIterations = 5
	 * forks = 1 threads = 4 result = <resultsFilename>.csv
	 * 
	 * @param resultsFilename
	 *            Name of the file to write the output to, without file
	 *            extension
	 * @return {@link ChainedOptionsBuilder} with some default settings
	 */
	public static ChainedOptionsBuilder createDefaultOptionsBuilder(
			String resultsFilename) {
		return new OptionsBuilder()
			.warmupIterations(5)
			.forks(1)
			.threads(4)
			.result(resultsFilename + ".csv")
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
		ChainedOptionsBuilder builder = createDefaultOptionsBuilder(
				NeighborhoodBenchmarks.class.getSimpleName() + "_" + timestamp());

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
	 * @param resultsFilename
	 *            name of the output file, without file extension
	 * @throws RunnerException
	 *             thrown when jmh runs into trouble
	 */
	public static void runBenchmark(String simpleName, String resultsFilename)
			throws RunnerException {
		Options opt = createDefaultOptionsBuilder(resultsFilename)
				.include(simpleName)
				.build();

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
		NeighborhoodBenchmarks
				.runBenchmark(simpleName, simpleName + "_" + timestamp());
	}
	
	public static String timestamp() {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY_MM_DD_HH_MM");
        return sdf.format(cal.getTime());
	}
}
