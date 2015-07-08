package de.squareys.nhbench.imagej;

import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.threshold.local.methods.LocalMean;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

/**
 * Simple class which I use to test if the imagej-ops implementations of the
 * thresholds work more or less alight.
 * 
 * @author Jonathan Hale
 *
 */
public class ImageJPreview {

	public static <T extends RealType<T>> void main(String[] args)
			throws IOException, IncompatibleTypeException {
		final ImageJ ij = new ImageJ();

		Dataset ds = ij.dataset().open("lena_grey.jpeg");

		Img<T> in = (Img<T>) ds.getImgPlus().getImg();
		Img<BitType> out = in.factory().imgFactory(new BitType())
				.create(in, new BitType());

		// ij.op().run(Ops.Threshold.class, out, in,
		// ij.op().op(LocalMean.class, BitType.class, Pair.class, 0.0),
		// new RectangleShape(1, false),
		// new OutOfBoundsMirrorFactory<T, Img<T>>(Boundary.SINGLE));

		ij.op().run(
				net.imagej.ops.neighborhood.NeighborhoodWithCenterMap.class,
				out, in,
				ij.op().op(LocalMean.class, BitType.class, Pair.class, 0.0), 1);

		ij.ui().show("Lena Thersholded", out);

	}
}
