/**
 * 
 */
package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 * @author paepcke
 *
 */
public class AlphaCapableLabel extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	float _alphaValue = Const.defaultLabelAlpha;
	// static Dimension _defaultLabelDim = PhotoSpread.photoSpreadPrefs.getDimension(PhotoSpread.dragGhostSizeKey);
	
	public void setAlpha (float newAlpha) {
		_alphaValue = newAlpha;
	}
	
	public float getAlpha () {
		return _alphaValue;
	}
	
	public void paintComponent (Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alphaValue));
		g2.drawImage(((ImageIcon) this.getIcon()).getImage(), 0, 0, null);
	}
	
	public static void main (String[] args) {
		
		JFrame app = new JFrame("Test xparent label");
		app.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		app.setLocationRelativeTo(null);
		app.setBackground(Color.BLUE);
		AlphaCapableLabel label = new AlphaCapableLabel();
		label.setAlpha(1.0f);
		ImageIcon imgIcon = Misc.createImageIcon(
				// "PhotoSpread/img/rightArrow.png",
				"PhotoSpread/img/grouchSmiling.png",
				new Dimension (300,350));
				// new Dimension (43,50));
				// PhotoSpread.photoSpreadPrefs.getDimension(PhotoSpread.dragGhostSizeKey));
		label.setIcon(imgIcon);
		app.add(label);
		app.setVisible(true);
		app.pack();
	}
}
