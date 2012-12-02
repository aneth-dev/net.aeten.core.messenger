package net.aeten.core.gui;

public class Colors {
	public static final float RED_HUE = 0;
	public static final float YELLOW_HUE = 60;
	public static final float GREEN_HUE = 120;
	public static final float CYAN_HUE = 180;
	public static final float BLUE_HUE = 240;
	public static final float PINK_HUE = 300;

	public static enum HSx {
		HSV {
			@Override
			public double[] rgb (double hue,
										double saturation,
										double value) {
				double chroma = saturation * value;
				double[] r1g1b1 = r1g1b1 (hue, chroma);
				double m = value - chroma;
				return addM (r1g1b1, m);
			}

			@Override
			public double getLightness (	double r,
													double g,
													double b) {
				return StrictMath.max (r, StrictMath.max (g, b));
			}

			@Override
			public double getSaturation (	double r,
													double g,
													double b) {
				return getChroma (r, g, b) / getLightness (r, g, b);
			}
		},
		HSL {
			@Override
			public double[] rgb (double hue,
										double saturation,
										double lightness) {
				double chroma = saturation * (1 - StrictMath.abs ((2 * lightness) - 1));
				double[] r1g1b1 = r1g1b1 (hue, chroma);
				double m = lightness - (chroma / 2);
				return addM (r1g1b1, m);
			}

			@Override
			public double getLightness (	double r,
													double g,
													double b) {
				return (StrictMath.max (r, StrictMath.max (g, b)) + StrictMath.min (r, StrictMath.min (g, b))) / 2;
			}

			public double getSaturation (	double r,
													double g,
													double b) {
				double lightness = getLightness (r, g, b);
				return lightness > 0.5? getChroma (r, g, b) / (2 - 2 * lightness): getChroma (r, g, b) / (2 * lightness);
			}
		},
		HSI {
			@Override
			public double[] rgb (double hue,
										double saturation,
										double intensity) {
				double[] r1g1b1 = r1g1b1 (hue, getChromaFromHsl (hue, saturation, intensity));
				double m = intensity - getLightness (r1g1b1[0], r1g1b1[1], r1g1b1[2]);
				return addM (r1g1b1, m);
			}

			@Override
			public double getLightness (	double r,
													double g,
													double b) {
				return (r + g + b) / 3;
			}

			public double getSaturation (	double r,
													double g,
													double b) {
				double hue = getHue (r, g, b);
				double lightness = getLightness (r, g, b);
				return getChroma (r, g, b) / getMaxChroma (hue, lightness);
			}

			@Override
			public double getChroma (	double r,
												double g,
												double b) {
				return StrictMath.max (r, StrictMath.max (g, b)) - StrictMath.min (r, StrictMath.min (g, b));
			}
		},
		HSYp {
			@Override
			public double[] rgb (double hue,
										double saturation,
										double Yp) {

				double[] r1g1b1 = r1g1b1 (hue, getChromaFromHsl (hue, saturation, Yp));
				double m = Yp - getLightness (r1g1b1[0], r1g1b1[1], r1g1b1[2]);
				return addM (r1g1b1, m);
			}

			@Override
			public double getLightness (	double r,
													double g,
													double b) {
				return (r * 0.299 + g * 0.587 + b * 0.114);
			}

			public double getSaturation (	double r,
													double g,
													double b) {

				double hue = getHue (r, g, b);
				double lightness = getLightness (r, g, b);
				return getChroma (r, g, b) / getMaxChroma (hue, lightness);
			}
		};

		protected final double optimalLightness = 0.5;

		public abstract double[] rgb (double h,
												double s,
												double x);

		public double[] rgb (double[] hsx) {
			return rgb (hsx[0], hsx[1], hsx[2]);
		}

		/** @return the lightness (or value) */
		public abstract double getLightness (	double r,
															double g,
															double b);

		public abstract double getSaturation (	double r,
															double g,
															double b);

		public double getSaturationFromChroma (double chroma,
															double hue,
															double lightness) {
			return chroma / getMaxChroma (hue, lightness);
		}

		public double getChroma (	double r,
											double g,
											double b) {
			return StrictMath.max (r, StrictMath.max (g, b)) - StrictMath.min (r, StrictMath.min (g, b));
		}

		public double getChroma (double[] rgb) {
			return getChroma (rgb[0], rgb[1], rgb[2]);
		}

		/** Return hue/chroma/lightness (or value) */
		public double[] hcl (double r,
									double g,
									double b) {
			return new double[] {
					getHue (r, g, b),
					getChroma (r, g, b),
					getLightness (r, g, b)
			};
		}

		/** @return hue/saturation/lightness (or value) */
		public double[] hsx (double r,
									double g,
									double b) {
			return new double[] {
					getHue (r, g, b),
					getSaturation (r, g, b),
					getLightness (r, g, b)
			};
		}

		public double getHue (	double r,
										double g,
										double b) {
			if (r >= g && g >= b) {
				return 60.0 * (g - b) / (r - b);
			} else if (g > r && r >= b) {
				return 60.0 * (2 - ((r - b) / (g - b)));
			} else if (g >= b && b > r) {
				return 60.0 * (2 + ((b - r) / (g - r)));
			} else if (b > g && g > r) {
				return 60.0 * (4 - ((g - r) / (b - r)));
			} else if (b > r && r >= g) {
				return 60.0 * (4 + ((r - g) / (b - g)));
			}
			return 60.0 * (6 - ((b - g) / (r - g)));
		}

		public double[] rgbBest (	double hue,
											double relativeToLightness) {
			double[] rgb = HSV.rgb (hue, 1, 1);
			double perseptualLightness = getLightness (rgb);

			double rho = StrictMath.sqrt (1 + (1 - perseptualLightness) * (1 - perseptualLightness));
			double theta = StrictMath.PI / 2 - StrictMath.acos (1 / rho);

			double lightness = relativeToLightness > 0.5? (relativeToLightness - (relativeToLightness * optimalLightness)): relativeToLightness + ((1.0 - relativeToLightness) * optimalLightness);
			rho = (1.0 - lightness) / (StrictMath.cos (theta));
			double chroma = rho * StrictMath.sin (theta);

			rgb = rgb (hue, chroma, lightness);
			for (int i = 0; i < rgb.length; i++) {
				rgb[i] = StrictMath.abs (rgb[i]);
			}

			return rgb;
		}

		public double getLightness (double[] rgb) {
			return getLightness (rgb[0], rgb[1], rgb[2]);
		}

		public double getSaturation (double[] rgb) {
			return getSaturation (rgb[0], rgb[1], rgb[2]);
		}

		public double[][] rgbBest (double[] background,
											double rgbStartHue,
											int size) {
			double backgroundLightness = getLightness (background);
			double[][] colors = new double[size][];

			int steps = (int) StrictMath.floor ((double) size / 3) + 1;
			for (int iStep = 0, iColor = 0; iStep < steps; iStep++) {
				double startHue = rgbStartHue + iStep * 180.0 / steps;
				if (startHue > 360.0) startHue -= 360.0;
				for (int i = 0; i < 3 && iColor < size; i++) {
					double hue = startHue + i * 120.0;
					if (hue >= 360.0) hue -= 360.0;
					colors[iColor++] = rgbBest (hue, backgroundLightness);
				}
			}
			return colors;
		}

		private static double[] addM (double[] r1g1b1,
												double m) {
			for (int i = 0; i < r1g1b1.length; i++) {
				r1g1b1[i] = r1g1b1[i] + m;
			}
			return r1g1b1;
		}

		public double getChromaFromHsl (	double hue,
													double saturation,
													double lightness) {
			return saturation * getMaxChroma (hue, lightness);
		}

		public double getMaxChroma (	double hue,
												double lightness) {
			double[] rgb = HSV.rgb (hue, 1, 1);
			double lightnessForMaxSaturation = getLightness (rgb);

			double rho, theta;
			if (lightness > lightnessForMaxSaturation) {
				rho = StrictMath.sqrt (1 + (1 - lightnessForMaxSaturation) * (1 - lightnessForMaxSaturation));
				theta = StrictMath.PI / 2 - StrictMath.acos (1 / rho);
				rho = (1.0 - lightness) / (StrictMath.cos (theta));
			} else {
				rho = StrictMath.sqrt (1 + lightnessForMaxSaturation * lightnessForMaxSaturation);
				theta = StrictMath.PI / 2 - StrictMath.acos (1 / rho);
				rho = lightness / (StrictMath.cos (theta));
			}

			return rho * StrictMath.sin (theta);
		}

		private static double[] r1g1b1 (	double hue,
													double chroma) {
			double Hp = hue / 60;
			double x = chroma * (1 - StrictMath.abs (Hp % 2 - 1));

			double r, g, b;
			switch ((int) StrictMath.floor (Hp)) {
			case 0:
				r = chroma;
				g = x;
				b = 0;
				break;
			case 1:
				r = x;
				g = chroma;
				b = 0;
				break;
			case 2:
				r = 0;
				g = chroma;
				b = x;
				break;
			case 3:
				r = 0;
				g = x;
				b = chroma;
				break;
			case 4:
				r = x;
				g = 0;
				b = chroma;
				break;
			case 5:
				r = chroma;
				g = 0;
				b = x;
				break;
			default:
				r = 0;
				g = 0;
				b = 0;
				break;
			}
			return new double[] {
					r,
					g,
					b
			};
		}
	}

}
