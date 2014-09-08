package ocha.itolab.koala.applet.koalaview;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.mesh.*;

public class TextPanel extends JPanel {
	JTextArea clusterText;
	JTextArea connectionText;
	
	public TextPanel(Graph g) {
		super();
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(2,1));
		clusterText = new JTextArea();
		clusterText.setPreferredSize(new Dimension(500, 500));
		connectionText = new JTextArea();
		connectionText.setPreferredSize(new Dimension(500, 500));
		JScrollPane scroll1 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
	        	JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll1.setViewportView(clusterText);
		scroll1.setPreferredSize(new Dimension(250, 250));
		scroll1.setVisible(true);
		JScrollPane scroll2 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
	        	JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll2.setViewportView(connectionText);
		scroll2.setPreferredSize(new Dimension(250, 250));
		scroll2.setVisible(true);
		p1.add(scroll1);
		p1.add(scroll2);
		
		this.add(p1);
	}

	public void setPickedObject(Object picked) {
		Node node = (Node)picked;
		
		String tnode = "";
		int numd = node.getNumDescription();
		for(int i = 0; i < numd; i++)
			tnode += (node.getDescription(i) + " ");
		String text1 = (tnode + '\n' + '\n');
		
		Vertex vertex = node.getVertex();
		for(Node node2 : vertex.getNodes()) {
			if(node == node2) continue;
			numd = node2.getNumDescription();
			tnode = "";
			for(int i = 0; i < numd; i++)
				tnode += (node2.getDescription(i) + " ");
			text1 += (tnode + '\n');
		}
		clusterText.setText(text1);
		
		String text2 = "";
		for(int j = 0; j < node.getNumConnectedEdge(); j++) {
			Edge e = node.getConnectedEdge(j);
			Node narray[] = e.getNode();
			Node node2 = (node == narray[0]) ? narray[1] : narray[0];
			numd = node2.getNumDescription();
			tnode = "";
			for(int i = 0; i < numd; i++)
				tnode += (node2.getDescription(i) + " ");
			text2 += (tnode + '\n');
		}
		for(int j = 0; j < node.getNumConnectingEdge(); j++) {
			Edge e = node.getConnectingEdge(j);
			Node narray[] = e.getNode();
			Node node2 = (node == narray[0]) ? narray[1] : narray[0];
			numd = node2.getNumDescription();
			tnode = "";
			for(int i = 0; i < numd; i++)
				tnode += (node2.getDescription(i) + " ");
			text2 += (tnode + '\n');
		}
		connectionText.setText(text2);
		
	}
	

}
