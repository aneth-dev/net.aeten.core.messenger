package net.aeten.core.gui.test;

import java.awt.Color;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.aeten.core.gui.Colors;
import net.aeten.core.gui.swing.HSxSelector;
import net.aeten.core.gui.swing.HueSelector;

public class ColorsTest {

	public static void main (String[] args) throws InvocationTargetException,
														InterruptedException {
		SwingUtilities.invokeAndWait (new Runnable () {

			@Override
			public void run () {
				JFrame frame = new JFrame ();
				frame.setLayout (new GridLayout (3, 2));
				final HueSelector hue = new HueSelector ();
				frame.add (hue);
				final JPanel colorPanel = new JPanel ();
				frame.add (colorPanel);
				final HSxSelector[] selectors = new HSxSelector[Colors.HSx.values ().length];
				for (Colors.HSx hsx: Colors.HSx.values ()) {
					final HSxSelector selector = new HSxSelector (hsx, hue, false);
					selectors[hsx.ordinal ()] = selector;
					selector.addChangeListener (new ChangeListener () {
						@Override
						public void stateChanged (ChangeEvent event) {
							Color color = ((HSxSelector) event.getSource ()).getColor ();
							colorPanel.setBackground (color);
							for (HSxSelector s: selectors) {
								if (s != selector) {
									s.setColor (color);
								}
							}
						}
					});
					frame.add (selector);
				}
				hue.addChangeListener (new ChangeListener () {
					@Override
					public void stateChanged (ChangeEvent event) {
						colorPanel.setBackground (selectors[0].getColor ());
					}
				});
				frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
				frame.pack ();
				frame.setVisible (true);
			}
		});
	}
}
