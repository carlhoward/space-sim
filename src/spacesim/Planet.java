/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spacesim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.JComponent;

/**
 *
 * @author carl
 */
public class Planet extends JComponent {

	double radius; // meters
	double mass; // kilograms

	Planet(String name, int size, Color c, int x, int y) {
		setName(name);
		setToolTipText(name);
		setForeground(c);
		setBounds(x, y, size, size); // absolute positioning
	}

	public int radius() {
		return getWidth() / 2;
	}
	
	public Point center() {
		Point c = getLocation();
		int r = radius();
		c.translate(r, r);
		return c;
	}

	public double distance(Planet p) {
		Point p1 = center();
		Point p2 = p.center();
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
		paintSphere(g, getForeground());
	}

	public void paintSphere(Graphics g, Color baseColor) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Retains the previous state
		Paint oldPaint = g2.getPaint();

		// Fills the circle with solid color
		g2.setColor(baseColor);
		g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

		// Creates dark edges for 3D effect
		Paint p = new RadialGradientPaint(new Point2D.Double(getWidth() / 2.0,
				getHeight() / 2.0), getWidth() / 2.0f,
				new float[]{0f, 1f},
				new Color[]{new Color(0, 0, 0, 0),
					new Color(0.0f, 0.0f, 0.0f, .6f)}
		);
		g2.setPaint(p);
		g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

		// Adds oval specular highlight at the top left
		p = new RadialGradientPaint(
				new Point2D.Double(getWidth() / 2.0, getHeight() / 2.0),
				getWidth() / 1.4f,
				new Point2D.Double(getWidth() / 4.0, getHeight() / 4.0),
				new float[]{0.0f, 0.5f},
				new Color[]{new Color(1.0f, 1.0f, 1.0f, 0.8f),
					new Color(1.0f, 1.0f, 1.0f, 0.0f)},
				RadialGradientPaint.CycleMethod.NO_CYCLE);
		g2.setPaint(p);
		g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

		// Restores the previous state
		g2.setPaint(oldPaint);
	}

}
