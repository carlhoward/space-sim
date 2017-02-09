/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spacesim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.JPanel;

/**
 *
 * @author carl
 */
public class SpacePanel extends JPanel {

	private static final int MIN_SEPARATION = 15;
	
	List<Planet> planets = new ArrayList<>();
	Random rand = new Random();
	Thread spacingThread;
	int planetCount;
	long currentSeed;
	
	public SpacePanel(int width, int height, int planetCount) {
		setLayout(null);
		setBackground(Color.BLACK);
		setSize(width, height);
		setPreferredSize(new Dimension(width, height));
		this.planetCount = planetCount;
		init();
	}
	
	public void init() {
		planets.clear();
		this.removeAll();
		currentSeed = rand.nextLong(); // so we can save the current map if wanted.
		System.out.println("Space Seed: " + currentSeed);
		rand.setSeed(currentSeed);
		for (int i = 0; i < planetCount; i++) {
			planets.add(generateNewPlanet());
		}
		separatePlanets();
	}
	
	Point dragOffset = null;
	
	MouseAdapter mListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			dragOffset = e.getPoint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			super.mouseDragged(e);
			if (e.getSource() instanceof Component) {
				int dx = e.getX() - dragOffset.x;
				int dy = e.getY() - dragOffset.y;
				// move the component
				Component c = (Component) e.getSource();
				Point p = c.getLocation();
				p.translate(dx, dy);
				c.setLocation(p);
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e); //To change body of generated methods, choose Tools | Templates.
			separatePlanets();
		}

		
	};

	private Planet generateNewPlanet() {
		Color c = Color.getHSBColor(rand.nextFloat(), 1f, 1f);
		int d = 10 + rand.nextInt(30);
		int x = d + rand.nextInt(getWidth() - 2*d);
		int y = d + rand.nextInt(getHeight() - 2*d);
		Planet p = new Planet("P-" + planets.size(), d, c, x, y);
		p.addMouseListener(mListener);
		p.addMouseMotionListener(mListener);
		add(p);
		return p;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.DARK_GRAY);
		// join planet to its 3 closest neighboring planets
		for (Planet p : planets) {
			Planet[] neighbors = getNeighborsByDistance(p);
			Point p1 = p.center();
			for (int i = 0; i<Math.min(neighbors.length, 3); i++) {
				Point p2 = neighbors[i].center();
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
		}
	}
	
	Planet[] getNeighborsByDistance(Planet p) {
		double min = Double.MAX_VALUE;
		TreeMap<Double, Planet> closest = new TreeMap<>();
		for (Planet x : planets) {
			if (x != p) {
				closest.put(p.distance(x), x);
			}
		}
		return closest.values().toArray(new Planet[closest.size()]);
	}
	
	/**
	 * Move planets so that their surfaces are separated by at least min distance.
	 * MIN_SEPARATION = The minimum distance between planet surfaces in pixels.
	 */
	public void separatePlanets() {
		if (spacingThread != null) {
			return; // already running.
		}
		final int dMin = MIN_SEPARATION;
		spacingThread = new Thread() {
			@Override
			public void run() {
				// algorithm to move planets until they're separated by a minimum space.
				System.out.println("Starting planet spacing algorithm...");
				boolean done = false;
				int itterations = 0;
				List<Point> tooCloseCorrections = new ArrayList<>();
				while (!done) {
					itterations++;
					done = true;
					for (Planet target : planets) {
						Point tCenter = target.center();
						int tRadius = target.radius();
						tooCloseCorrections.clear();
						for (Planet p : planets) {
							if (p == target) {
								continue; // dont compare with self;
							}
							Point pCenter = p.center();
							int pRadius = p.radius();
							// calculate required separation between planet centers
							int min = dMin + tRadius + pRadius;
							// find actual distance between planet centers
							int actual = (int) target.distance(p);
							if (actual == 0) {
								System.out.printf("OOPS! Planets %s and %s overlap!\n", target.getName(), p.getName());
								// planets are right on top of eachother!
								// force move planet p toward the center of the space
								int fx = 2, fy = 2;
								if (pCenter.x > getWidth() / 2) fx = -fx;
								if (pCenter.y > getHeight() / 2) fy = -fy;
								pCenter.translate(fx, fy);
								p.setLocation(pCenter);
								actual = (int) target.distance(p);
								done = false;
							}
							if (actual < min) {
								int dx = tCenter.x - pCenter.x;
								int dy = tCenter.y - pCenter.y;
								// note: the +2 gives us a larger margin of error so it
								// greatly reduces the number of itterations to stabilize.
								int targetX = pCenter.x  + dx * (min + 2) / actual;
								int targetY = pCenter.y  + dy * (min + 2) / actual;
								tooCloseCorrections.add(new Point(targetX, targetY));
							}
						}
						
						// add corrections for edge proximity
						if (tCenter.x - tRadius - dMin < 0) { // left edge check
							tooCloseCorrections.add(new Point(dMin + tRadius, tCenter.y));
						}
						if (tCenter.x + tRadius + dMin > getWidth()) { // right edge check
							tooCloseCorrections.add(new Point(getWidth() - dMin - tRadius, tCenter.y));
						}
						if (tCenter.y - tRadius - dMin < 0) { // top edge check
							tooCloseCorrections.add(new Point(tCenter.x, dMin + tRadius));
						}
						if (tCenter.y + tRadius + dMin > getHeight()) { // bottom edge check
							tooCloseCorrections.add(new Point(tCenter.x, getHeight() - dMin - tRadius));
						}
						
						// now we have a map of all other planets that are too
						// close to the target planet.
						int n = tooCloseCorrections.size();
						if (n > 0) {
							done = false;
							// move the planet a little bit in the appropriate direction
							int sumX = 0, sumY = 0;
							for (Point p : tooCloseCorrections) {
								sumX += p.x;
								sumY += p.y;
							}
							int tx = sumX/n;
							int ty = sumY/n;
							int dx = tx - tCenter.x;
							int dy = ty - tCenter.y;
							// should we scale between current and target location?
							if (dx == 0 && dy == 0) {
								// invalid equilibrium. Try disturbing the state somehow
								tx += rand.nextInt(3)-1;
								ty += rand.nextInt(3)-1;
							}
							target.setLocation(tx - tRadius, ty - tRadius);
						}
					}
					repaint();
				}
				System.out.printf("Finished spacing out planets. (%d itterations)\n", itterations);
				spacingThread = null;
			}
		};
		spacingThread.setName("Spacing");
		spacingThread.setDaemon(true);
		spacingThread.start();
	}
}
