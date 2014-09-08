package ocha.itolab.koala.applet.koalaview;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import ocha.itolab.koala.core.data.*;


public class VectorPanel extends JPanel {

	Graph graph;
	Canvas canvas;
	
	public JCheckBox dimensionChecks[];
	
	RadioButtonListener rbl = null;
	CheckBoxListener cbl = null;
	
	public VectorPanel(Graph g, Object c) {
		super();
		
		canvas = (Canvas)c;
		
		if(g == null) return;
		graph = g;
		dimensionChecks = new JCheckBox[graph.vectorname.length];
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(graph.vectorname.length, 1));
		boolean[] colorSwitch = new boolean[graph.vectorname.length];
		
		// for each button
		for(int i = 0; i < graph.vectorname.length; i++) {
			String name = graph.vectorname[i];
			dimensionChecks[i] = new JCheckBox(name, true);
			p1.add(dimensionChecks[i]);
			colorSwitch[i] = true;
		}
		canvas.setColorSwitch(colorSwitch);
		
		VectorParettePanel p2 = new VectorParettePanel(graph);	

		JPanel p0 = new JPanel();
		p0.setLayout(new GridLayout(1, 2));
		p0.add(p1);
		p0.add(p2);
		
		JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        	JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setViewportView(p0);
		scroll.setVisible(true);
	
		add(scroll);
		
		if (rbl == null)
			rbl = new RadioButtonListener();
		addRadioButtonListener(rbl);
		
		
		if (cbl == null)
			cbl = new CheckBoxListener();
		addCheckBoxListener(cbl);
	}
	


	/**
	 * ラジオボタンのアクションの検出を設定する
	 * @param actionListener ActionListener
	 */
	public void addRadioButtonListener(ActionListener actionListener) {
	}

	
	/**
	 * チェックボックスのアクションの検出を設定する
	 * @param actionListener ActionListener
	 */
	public void addCheckBoxListener(CheckBoxListener checkBoxListener) {
		for(int i = 0; i < dimensionChecks.length; i++)
			dimensionChecks[i].addItemListener(checkBoxListener);
	}
	
	/**
	 * ラジオボタンのアクションを検知するActionListener
	 * @or itot
	 */
	public class RadioButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JRadioButton buttonPushed = (JRadioButton) e.getSource();	
		}
	}
	
	/**
	 * チェックボックスのアクションを検知するItemListener
	 * @author itot
	 */
	class CheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			JCheckBox stateChanged = (JCheckBox) e.getSource();
			boolean colorSwitch[] = new boolean[dimensionChecks.length];
			
			for(int i = 0; i < dimensionChecks.length; i++) {
				colorSwitch[i] = dimensionChecks[i].isSelected();
			}
			canvas.setColorSwitch(colorSwitch);
			canvas.display();
		}
	}
	
	
	
}
