/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spacesim;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 *
 * @author carl
 */
public class SpaceSim extends JFrame {

	JPanel mainPanel;
	SpacePanel spacePanel;
	
	public SpaceSim() {
		init();
	}
	
	private void init() {
		setTitle("Space Simulator");
		setIcon("/res/planet.jpg");
		spacePanel = new SpacePanel(800, 600, 50);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(spacePanel, BorderLayout.CENTER);
		setContentPane(mainPanel);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		pack();
		setVisible(true);

		JButton resetButton = new JButton("RESET");
		resetButton.addActionListener(aListener);
		mainPanel.add(resetButton, BorderLayout.SOUTH);
		addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				spacePanel.separatePlanets();
			}
			
		});
	}
	
	private ActionListener aListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JButton) {
				spacePanel.init();
			}
		}
	};
			

	private void setIcon(String imagePath) {
		try {
			InputStream imgStream = SpaceSim.class.getResourceAsStream(imagePath );
			BufferedImage myImg = ImageIO.read(imgStream);
			setIconImage(myImg);
		} catch (IOException ex) {
			Logger.getLogger(SpaceSim.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	static SpaceSim instance;
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			// ignore
		}
		
		instance = new SpaceSim();
	}
	
}
