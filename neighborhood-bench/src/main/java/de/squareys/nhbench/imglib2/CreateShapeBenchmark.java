package de.squareys.nhbench.imglib2;

import java.util.concurrent.TimeUnit;

import net.imglib2.algorithm.neighborhood.CenteredRectangleShape;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.DiamondTipsShape;
import net.imglib2.algorithm.neighborhood.HorizontalLineShape;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.PairOfPointsShape;
import net.imglib2.algorithm.neighborhood.PeriodicLineShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;

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
 * Benchmark to compare speed of Shapecreation of various types.
 * 
 * Note: This benchmark is more for learning jmh that to actually do something
 * senseful.
 * 
 * @author Jonathan Hale (University of Konstanz)
 *
 */
@State(Scope.Benchmark)
public class CreateShapeBenchmark {

	/*
	 * constants for shape type ids
	 */
	public static final int RECTANGLE_SHAPE = 0;
	public static final int CENTERED_RECTANGLE_SHAPE = 1;
	public static final int DIAMOND_SHAPE = 2;
	public static final int DIAMOND_TIPS_SHAPE = 3;
	public static final int HORIZONTAL_LINE_SHAPE = 4;
	public static final int HYPER_SPHERE_SHAPE = 5;
	public static final int PAIR_OF_POINTS_SHAPE = 6;
	public static final int PERIODIC_LINE_SHAPE = 7;

	public static final int SPAN = 3;
	public static final int[] SPAN_2D = new int[] { 3, 3 };

	/*
	 * Type of the shape to be created. The benchmark will be run once for every
	 * type.
	 */
	@Param({ "RectangleShape", "CenteredRectangleShape", "RectangleShapeSC",
			"CenteredRectangleShapeSC", "DiamondShape", "DiamondTipsShape",
			"HorizontalLineShape", "HorizontalLineShapeSC", "HyperSphereShape",
			"PairOfPointsShape", "PeriodicLineShape" })
	private String shapeType;

	/* The String is converted to an integer id to make comparison faster */
	private int shapeTypeId;
	/* For shapes which have the skipCenter feature */
	private boolean skipCenter;

	/**
	 * Some code executed before the benchmark to initialize the State.
	 * 
	 * Resolves the id from the shapeType parameter.
	 */
	@Setup
	public void setup() {
		// resolve shapeType id
		if (shapeType.startsWith("RectangleShape")) {
			shapeTypeId = RECTANGLE_SHAPE;
		} else if (shapeType.startsWith("CenteredRectangleShape")) {
			shapeTypeId = CENTERED_RECTANGLE_SHAPE;
		} else if (shapeType.equals("DiamondShape")) {
			shapeTypeId = DIAMOND_SHAPE;
		} else if (shapeType.equals("DiamondTipsShape")) {
			shapeTypeId = DIAMOND_TIPS_SHAPE;
		} else if (shapeType.startsWith("HorizontalLineShape")) {
			shapeTypeId = HORIZONTAL_LINE_SHAPE;
		} else if (shapeType.equals("HyperSphereShape")) {
			shapeTypeId = HYPER_SPHERE_SHAPE;
		} else if (shapeType.equals("PairOfPointsShape")) {
			shapeTypeId = PAIR_OF_POINTS_SHAPE;
		} else if (shapeType.equals("PeriodicLineShape")) {
			shapeTypeId = PERIODIC_LINE_SHAPE;
		}

		skipCenter = shapeType.endsWith("SC");
	}

	/**
	 * Create a shape of the given type.
	 * 
	 * @param bh
	 *            black hole to avoid code being optimized out
	 */
	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void createShape(Blackhole bh) {
		Shape shape = null;

		switch (shapeTypeId) {
		case RECTANGLE_SHAPE:
			shape = new RectangleShape(3, skipCenter);
			break;
		case CENTERED_RECTANGLE_SHAPE:
			shape = new CenteredRectangleShape(SPAN_2D, skipCenter);
			break;
		case DIAMOND_SHAPE:
			shape = new DiamondShape(SPAN);
			break;
		case DIAMOND_TIPS_SHAPE:
			shape = new DiamondTipsShape(SPAN);
			break;
		case HORIZONTAL_LINE_SHAPE:
			shape = new HorizontalLineShape(SPAN, 2, skipCenter);
			break;
		case HYPER_SPHERE_SHAPE:
			shape = new HyperSphereShape(SPAN);
			break;
		case PAIR_OF_POINTS_SHAPE:
			shape = new PairOfPointsShape(new long[] { 3, 3 });
			break;
		case PERIODIC_LINE_SHAPE:
			shape = new PeriodicLineShape(SPAN, new int[] { 1, 1 });
			break;
		default:
			break;
		}

		bh.consume(shape);
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
		NeighborhoodBenchmarks.runBenchmark(CreateShapeBenchmark.class
				.getSimpleName());
	}
}
