package net.aeten.core.gui.swing;

import java.awt.Color;

import net.aeten.core.gui.Colors;

public class AwtColors {
	public static Color fromRGB (double[] rgb) {
		if (rgb[0] < 0.0 || rgb[0] > 1.0 || rgb[1] < 0.0 || rgb[1] > 1.0 || rgb[2] < 0.0 || rgb[2] > 1.0) {
			return null;
		}
		int r = (int) StrictMath.round (rgb[0] * 255.0);
		int g = (int) StrictMath.round (rgb[1] * 255.0);
		int b = (int) StrictMath.round (rgb[2] * 255.0);
		return new Color (r, g, b);
	}

	public static double[] rgb (Color color) {
		return new double[] {
				(double) color.getRed () / 255.0,
				(double) color.getGreen () / 255.0,
				(double) color.getBlue () / 255.0
		};
	}

	public static Color fromHSV (double[] hsv) {
		return fromHSV (hsv[0], hsv[1], hsv[2]);
	}

	public static Color fromHSV (	double hue,
											double saturation,
											double value) {
		return fromRGB (Colors.HSx.HSV.rgb (hue, saturation, value));
	}

	public static Color fromHSI (double[] hsi) {
		return fromHSI (hsi[0], hsi[1], hsi[2]);
	}

	public static Color fromHSI (	double hue,
											double saturation,
											double intensity) {
		return fromRGB (Colors.HSx.HSI.rgb (hue, saturation, intensity));
	}

	public static Color fromHSL (double[] hsl) {
		return fromHSL (hsl[0], hsl[1], hsl[2]);
	}

	public static Color fromHSL (	double hue,
											double saturation,
											double lightness) {
		return fromRGB (Colors.HSx.HSL.rgb (hue, saturation, lightness));
	}

	public static Color fromHueChromaYp (double[] hcYp) {
		return fromHueChromaYp (hcYp[0], hcYp[1], hcYp[2]);
	}

	public static Color fromHueChromaYp (	double hue,
														double chroma,
														double Yp) {
		return fromRGB (Colors.HSx.HSYp.rgb (hue, chroma, Yp));
	}

	public static double perceptualLlightness (Color color) {
		return Colors.HSx.HSYp.getLightness (rgb(color));
	}
}
