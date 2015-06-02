package sandbox;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.io.Opener;
import ij.plugin.Filters3D;
import ij.util.ThreadUtil;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class MinimumFilter {
	private final Img<FloatType> image;

	private Img<FloatType> output;

	private final long sigma;

	public static String fileName = "./flybrain-32bit.tif";

	public MinimumFilter(final long sigma) throws ImgIOException {
		// define the file to open
		final File file = new File(fileName);

		// open a file with ImageJ
		image = (Img<FloatType>) new ImgOpener()
		.openImg(file.getAbsolutePath());

		// create a new Image with the same properties
		output = image.factory().create(image, image.firstElement());

		this.sigma = sigma;
	}

	public void show() {
		// display it via ImgLib using ImageJ
		ImageJFunctions.show(output);
	}

	void runShape() {
		// get mirror view
		final IntervalView<FloatType> infinite = Views.interval(
				Views.extendMirrorSingle(image), image);

		final Shape shape = new RectangleShape((int) sigma, false);

		final RandomAccess<FloatType> ra = output.randomAccess();

		final FloatType min = new FloatType();

		for (final Neighborhood<FloatType> neighborhood : shape
				.neighborhoods(infinite)) {

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

	void runImageJ1(ImagePlus im) {
		final ImageStack is = Filters3D.filter(im.getImageStack(),
				Filters3D.MIN, (float) sigma, (float) sigma, (float) sigma);
		final ImagePlus ip = new ImagePlus("Minimum_sigma=" + sigma, is);
		output = ImagePlusAdapter.wrap(ip);
	}

	public static void main(final String[] args) throws ImgIOException {
		// open an ImageJ window
		new ImageJ();

		int numThreads = 1;

		ImagePlus im = IJ.openImage(MinimumFilter.fileName);
		Prefs.setThreads(numThreads);

		for (long l = 1; l <= 4; l *= 2) {
			final MinimumFilter filter = new MinimumFilter(l);
			final long start = System.currentTimeMillis();

			// run the ImageJ1 example
			filter.runImageJ1(im);
			final long end = System.currentTimeMillis();

			System.out.println("ImageJ1 Minimum filter with sigma = " + l
					+ " took " + (end - start) + "ms. (# threads = "
					+ numThreads + ")");

			// run the example
			filter.runShape();
			final long end2 = System.currentTimeMillis();
			// filter.show();
			System.out.println("ImgLib2 Minimum filter with sigma = " + l
					+ " took " + (end2 - end) + "ms.");

		}
	}
}