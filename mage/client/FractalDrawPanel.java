package mage.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

public class FractalDrawPanel extends JPanel {
	Image image;
	MemoryImageSource source;
	int[] pixels;
	int width;
	int height;
	String title;

	FractalDrawPanel (int[] pixels, int width, int height, String title) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
		this.title = title;
		
		source = new MemoryImageSource(width, height, pixels, 0, width);
		image = createImage(source);
		image.flush();	
	}

	public void paintComponent (Graphics g) { 
		super.paintComponent (g); 

		g.drawImage (image, 0, 0, this); 
		g.setColor(new Color(255, 255, 255));
		g.drawString(title, 10, 20);
	}

}
