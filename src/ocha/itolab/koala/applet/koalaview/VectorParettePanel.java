package ocha.itolab.koala.applet.koalaview;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import ocha.itolab.koala.core.data.*;

public class VectorParettePanel extends JPanel {
	Graph graph;
	
	public VectorParettePanel(Graph graph) {
		super();
		this.graph = graph;
	}
	
	
	/**
	 * Ä•`‰æ
	 */
	public void draw(int clusterId) {
		Graphics g = getGraphics();
		if (g == null)
			return;
		paintComponent(g);
	}

	
	/**
	 * •`‰æ‚ğÀs‚·‚é
	 * @param g Graphics
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // clear the background
		Graphics2D g2 = (Graphics2D) g;
		int BLOCK_HEIGHT = 24;
		
		for(int i = 0; i < graph.vectorname.length; i++) {
			Color color = calcColor(i, graph.vectorname.length);
			g2.setPaint(color);
			g.fillRect(10, (i * BLOCK_HEIGHT), 30, BLOCK_HEIGHT);
			g2.setPaint(Color.BLACK);
		}
	}
	
	
	public static Color calcColor(int id, int num) {
		float hue = (float)id / (float)num;
		Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
		return color;
	}
}
