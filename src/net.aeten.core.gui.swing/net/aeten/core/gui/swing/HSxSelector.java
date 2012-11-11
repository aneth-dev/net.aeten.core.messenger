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
	private double hue;
	private int x, y;
	private Color color;
	private final List <ChangeListener> listeners = new ArrayList <> ();

	private void select (int x,
								int y) {
		int width = getWidth ();
		int height = getHeight ();
		setColor (AwtColors.fromRGB (hsx.rgb (hue, (double) x / width, (double) (getHeight () - y) / height)));
	}

	public HSxSelector (	final HSx hsx,
								final HueSelector hueSelector) {
		this.hsx = hsx;
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
				int width = getWidth ();
				int height = getHeight ();
				color = AwtColors.fromRGB (hsx.rgb (hue, (double) x / width, (double) (y) / height));
				if (color == null) {
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
					Color color = AwtColors.fromRGB (hsx.rgb (hue, (double) x / (double) width, (double) y / height));
					if (color == null) {
						continue;
					}
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
		if (c != null) {
			Color oldColor = color;
			if (c.equals (color)) {
				return;
			}
			color = c;

			double[] rgb = AwtColors.rgb (c);
			x = (int) (hsx.getSaturation (rgb) * getWidth ());
			y = (int) (hsx.getLightness (rgb) * getHeight ());
			repaint ();

			firePropertyChange (AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, oldColor, color);
			ChangeEvent event = new ChangeEvent (this);
			for (ChangeListener listener: listeners) {
				listener.stateChanged (event);
			}
		}
	}

}
