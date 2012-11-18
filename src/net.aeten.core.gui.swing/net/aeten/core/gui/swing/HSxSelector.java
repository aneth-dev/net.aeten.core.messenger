package net.aeten.core.gui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.aeten.core.gui.Colors.HSx;

public class HSxSelector extends
		JComponent {
	private static final long serialVersionUID = 8194893126074967510L;
	private final HSx hsx;
	private final boolean extentChroma;
	private double hue;
	private int x, y;
	private Color color;
	private final List <ChangeListener> listeners = new ArrayList <> ();

	public HSxSelector (	final HSx hsx,
								final HueSelector hueSelector) {
		this (hsx, hueSelector, true);
	}

	public HSxSelector (	final HSx hsx,
								final HueSelector hueSelector,
								boolean extentChroma) {
		this.hsx = hsx;
		this.extentChroma = extentChroma;
		addMouseListener (new MouseAdapter () {
			@Override
			public void mouseClicked (MouseEvent event) {
				select (event.getX (), event.getY ());
			}
		});
		addMouseMotionListener (new MouseMotionAdapter () {
			@Override
			public void mouseDragged (MouseEvent event) {
				select (event.getX (), event.getY ());
			}
		});

		hueSelector.addChangeListener (new ChangeListener () {
			@Override
			public void stateChanged (ChangeEvent event) {
				hue = hueSelector.getHue ();
				int height = getHeight ();
				double[] rgb = hsx.rgb (hue, getSaturation (x, y), (double) y / height);
				if (AwtColors.isValidRGB (rgb)) {
					setColor (AwtColors.fromRGB (rgb));
				} else {
					select (0, 0);
				}
				repaint ();
			}
		});
	}

	@Override
	@Transient
	public Dimension getPreferredSize () {
		return new Dimension (256, 256);
	}

	@Override
	protected void paintComponent (Graphics graphics) {
		int width = getWidth ();
		int height = getHeight ();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (this.x == x || this.y == y) {
					graphics.setColor (Color.BLACK);
					graphics.drawLine (x, height - y, x, height - y);
				} else {
					double[] rgb = hsx.rgb (hue, getSaturation (x, height - y), (double) y / height);
					if (!AwtColors.isValidRGB (rgb)) {
						continue;
					}
					Color color = AwtColors.fromRGB (rgb);
					graphics.setColor (color);
					graphics.drawLine (x, height - y, x, height - y);
				}
			}
		}
	}

	public void addChangeListener (ChangeListener listener) {
		listeners.add (listener);
	}

	public void removeChangeListener (ChangeListener listener) {
		listeners.remove (listener);
	}

	public Color getColor () {
		return color;
	}

	public void setColor (Color c) {
		Color oldColor = color;
		if (c.equals (color)) {
			return;
		}
		color = c;

		double[] rgb = AwtColors.rgb (c);
		x = getX (rgb);
		y = (int) (hsx.getLightness (rgb) * getHeight ());
		repaint ();

		firePropertyChange (AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, oldColor, color);
		ChangeEvent event = new ChangeEvent (this);
		for (ChangeListener listener: listeners) {
			listener.stateChanged (event);
		}
	}

	private void select (int x,
								int y) {
		int height = getHeight ();
		double[] rgb = hsx.rgb (hue, getSaturation (x, y), (double) (height - y) / height);
		if (AwtColors.isValidRGB (rgb)) {
			setColor (AwtColors.fromRGB (rgb));
		}
	}

	private double getSaturation (int x,
											int y) {
		if (!extentChroma && (hsx == HSx.HSI || hsx == HSx.HSYp)) {
			return hsx.getSaturationFromChroma ((double) x / (double) getWidth (), hue, (double) (getHeight () - y) / (double) getHeight ());
		}
		return (double) x / (double) getWidth ();
	}

	private int getX (double[] rgb) {
		if (!extentChroma && (hsx == HSx.HSI || hsx == HSx.HSYp)) {
			return (int) (hsx.getChroma (rgb) * getWidth ());
		}
		return (int) (hsx.getSaturation (rgb) * getWidth ());
	}
}
