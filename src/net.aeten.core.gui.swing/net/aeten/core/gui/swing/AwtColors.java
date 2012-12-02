package net.aeten.core.gui.swing;

import static net.aeten.core.gui.Colors.HSx.HSI;
import static net.aeten.core.gui.Colors.HSx.HSL;
import static net.aeten.core.gui.Colors.HSx.HSV;
import static net.aeten.core.gui.Colors.HSx.HSYp;

import java.awt.Color;

public class AwtColors {
	public static Color fromRGB (double[] rgb) {
		int r = (int) StrictMath.round (rgb[0] * 255.0);
		int g = (int) StrictMath.round (rgb[1] * 255.0);
		int b = (int) StrictMath.round (rgb[2] * 255.0);
		return new Color (r, g, b);
	}

	public static boolean isValidRGB (double[] rgb) {
		return !(rgb[0] < 0.0 || rgb[0] > 1.0 || rgb[1] < 0.0 || rgb[1] > 1.0 || rgb[2] < 0.0 || rgb[2] > 1.0);
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
		return fromRGB (HSV.rgb (hue, saturation, value));
	}

	public static Color fromHSI (double[] hsi) {
		return fromHSI (hsi[0], hsi[1], hsi[2]);
	}

	public static Color fromHSI (	double hue,
											double saturation,
											double intensity) {
		return fromRGB (HSI.rgb (hue, saturation, intensity));
	}

	public static Color fromHSL (double[] hsl) {
		return fromHSL (hsl[0], hsl[1], hsl[2]);
	}

	public static Color fromHSL (	double hue,
											double saturation,
											double lightness) {
		return fromRGB (HSL.rgb (hue, saturation, lightness));
	}

	public static Color fromHueChromaIntensity (double[] hci) {
		return fromHueChromaIntensity (hci[0], hci[1], hci[2]);
	}

	public static Color fromHueChromaIntensity (	double hue,
																double chroma,
																double intensity) {
		return fromRGB (HSI.rgb (hue, HSI.getSaturationFromChroma (chroma, hue, intensity), intensity));
	}

	public static Color fromLumaChromaHue (double[] lch) {
		return fromLumaChromaHue (lch[0], lch[1], lch[2]);
	}

	public static Color fromLumaChromaHue (double luma,
														double chroma,
														double hue) {
		return fromRGB (HSYp.rgb (hue, HSYp.getSaturationFromChroma (chroma, hue, luma), luma));
	}

	public static Color fromHSYp (double[] hcYp) {
		return fromHSYp (hcYp[0], hcYp[1], hcYp[2]);
	}

	public static Color fromHSYp (double hue,
											double chroma,
											double Yp) {
		return fromRGB (HSYp.rgb (hue, chroma, Yp));
	}

	public static double perceptualLlightness (Color color) {
		return HSYp.getLightness (rgb (color));
	}
}
