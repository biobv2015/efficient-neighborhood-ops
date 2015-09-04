
package de.squareys.nhbench.imagej;

import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.Ops;
import net.imagej.ops.threshold.localMean.LocalMean;
import net.imglib2.Cursor;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Pair;

/**
 * Simple class which I use to test if the imagej-ops implementations of the
 * thresholds work more or less alight.
 * 
 * @author Jonathan Hale
 */
public class ImageJPreview {

	private static final boolean optimized = false;

	public static <T extends NativeType<T>> void main(String[] args)
		throws IOException, IncompatibleTypeException
	{
		final ImageJ ij = new ImageJ();

		Dataset ds = (Dataset) ij.io().open("lena_grey.jpeg");

		Img<T> planarin = (Img<T>) ds.getImgPlus().getImg();
		Img<T> in =
			new ArrayImgFactory<T>().create(planarin, planarin.firstElement());

		Cursor<T> c1 = planarin.cursor();
		Cursor<T> c2 = in.cursor();

		while (c1.hasNext()) {
			c2.next().set(c1.next());
		}

		Img<BitType> out =
			in.factory().imgFactory(new BitType()).create(in, new BitType());
		Img<BitType> outopt =
			in.factory().imgFactory(new BitType()).create(in, new BitType());

		ij.op().threshold().apply(out, in,
			ij.op().op(LocalMean.class, BitType.class, Pair.class, 0.0),
			new RectangleShape(3, false),
			new OutOfBoundsMirrorFactory<T, Img<T>>(Boundary.SINGLE));

		ij.op()
			.run(
				net.imagej.ops.map.neighborhood.array.MapNeighborhoodWithCenterNativeType.class,
				outopt, in,
				ij.op().op(LocalMean.class, BitType.class, Pair.class, 0.0), 3);

		ij.ui().show("Lena Thresholded", out);
		ij.ui().show("Lena Thresholded - optimized", outopt);

	}
}
