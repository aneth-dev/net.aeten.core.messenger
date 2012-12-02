package net.aeten.core.gui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

public class HueSelector extends
		JComponent {
	private static final long serialVersionUID = 1L;
	private static final int CURSOR_SIZE = 5;

	private double hue;
	private final List <ChangeListener> listeners = new ArrayList <> ();

	public HueSelector () {
		addMouseListener (new MouseAdapter () {
			@Override
			public void mouseClicked (MouseEvent event) {
				setHue (getHue (event.getX ()));
				repaint ();
			}
		});
		addMouseMotionListener (new MouseMotionAdapter () {
			@Override
			public void mouseDragged (MouseEvent event) {
				setHue (getHue (event.getX ()));
				repaint ();
			}
		});
	}

	private final double getHue (int x) {
		return StrictMath.max (0, StrictMath.min (360.0 * x / getWidth (), 360.0));
	}

	private final int getX (double hue) {
		return (int) StrictMath.floor ((((double) getWidth ()) * hue / 360.0));
	}

	@Override
	public void paintComponent (Graphics graphics) {
		int width = getWidth ();
		int height = getHeight ();
		if (graphics instanceof Graphics2D) {
			((Graphics2D) graphics).setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		graphics.setColor (getBackground ());
		graphics.fillRect (0, 0, width, width);

		for (int i = 0; i < width; i++) {
			double hue = getHue (i);
			graphics.setColor (AwtColors.fromHSV (hue, 1, 1));
			graphics.drawLine (i, CURSOR_SIZE, i, height - CURSOR_SIZE);
		}

		int cursor = getX (hue);
		if (graphics instanceof Graphics2D) {
			((Graphics2D) graphics).setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		graphics.setColor (AwtColors.perceptualLlightness (getBackground ()) > 0.5? Color.BLACK: Color.WHITE);
		graphics.fillPolygon (new int[] {
				cursor - CURSOR_SIZE / 2,
				cursor + CURSOR_SIZE / 2,
				cursor
		}, new int[] {
				0,
				0,
				CURSOR_SIZE
		}, 3);
		graphics.fillPolygon (new int[] {
				cursor - CURSOR_SIZE / 2,
				cursor + CURSOR_SIZE / 2,
				cursor
		}, new int[] {
				height,
				height,
				height - CURSOR_SIZE
		}, 3);

	}

	@Override
	@Transient
	public Dimension getPreferredSize () {
		return new Dimension (256, 20 + CURSOR_SIZE * 2);
	}

	@Override
	@Transient
	public Dimension getMinimumSize () {
		return new Dimension (256 / 6, 20 + CURSOR_SIZE * 2);
	}

	public void addChangeListener (ChangeListener listener) {
		listeners.add (listener);
	}

	public void removeChangeListener (ChangeListener listener) {
		listeners.remove (listener);
	}

	public double getHue () {
		return hue;
	}

	public void setHue (double hue) {
		if (hue == this.hue) {
			return;
		}
		double oldHue = hue;
		this.hue = hue;

		firePropertyChange (AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, oldHue, hue);
		ChangeEvent event = new ChangeEvent (this);
		for (ChangeListener listener: listeners) {
			listener.stateChanged (event);
		}
	}

}
