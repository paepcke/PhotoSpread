/**
 * 
 */
package edu.stanford.photoSpreadObjects.photoSpreadComponents;

/**
 * @author paepcke
 * 
 * Class to add a delayed switch to a
 * wait cursor to any action listeners that
 * are passed in. The passed-in listeners
 * are augmented with the start of a separate
 * thread that switches the cursor after a
 * settable delay. Once that thread runs, the
 * passed-in actual listener gets control.
 * The wait cursor is later switched back to
 * the regular cursor, once the listener is
 * done.
 * 
 * The delay ensures that the wait cursor only
 * appears if an action really takes an appreciable
 * amount of time.
 * 
 * Code from tutorial at http://www.catalysoft.com/articles/busyCursor.html.
 *
 */

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CursorController {
	public static final Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
	public static final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	public static final int delay = 500; // in milliseconds

	private CursorController() {}

	public static ActionListener createActionListener(final Component component, final ActionListener mainActionListener) {

		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(final ActionEvent ae) {

				TimerTask timerTask = new TimerTask() {
					public void run() {
						component.setCursor(busyCursor);
					}
				};
				Timer timer = new Timer(); 

				try {   
					timer.schedule(timerTask, delay);
					mainActionListener.actionPerformed(ae);
				} finally {
					timer.cancel();
					component.setCursor(defaultCursor);
				}
			}
		};
		return actionListener;
	}

	public static ChangeListener createChangeListener(final Component component, final ChangeListener mainChangeListener) {

		ChangeListener changeListener = new ChangeListener() {

			public void stateChanged(final ChangeEvent ce) {
				
				JSlider source = (JSlider)ce.getSource();

				if (source.getValueIsAdjusting()) return;
				
				TimerTask timerTask = new TimerTask() {
					public void run() {
						component.setCursor(busyCursor);
					}
				};
				Timer timer = new Timer(); 

				try {
					timer.schedule(timerTask, delay);
					mainChangeListener.stateChanged(ce);				
				} finally {				

					timer.cancel();
					component.setCursor(defaultCursor);
				}
			}
		};
		return changeListener;
	}
}
