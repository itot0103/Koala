package ocha.itolab.koala.applet.koalaview;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.mesh.*;

public class ViewingPanel extends JPanel {
	static int SMOOTHING_ITERATION = 30;
	
	public JButton  fileOpenButton, placeAgainButton, viewResetButton;
	public JSlider  bundleDensitySlider, bundleShapeSlider, transparencySlider,
		placeRatioSlider, clusteringRatioSlider, clusterSizeSlider, keyEmphasisSlider;
	public JRadioButton edgeDissimilarityButton, edgeDegreeButton, clickButton, moveButton, 
		colorTopicButton, colorDegreeButton;
	public Container container;
	JTabbedPane pane = null;
	VectorPanel vecpanel = null;
	TextPanel textpanel = null;
	
	
	/* Selective canvas */
	Canvas canvas;
	CursorListener listener;
	FileOpener fileOpener;
	Graph graph;
	File currentDirectory = null;
	
	/* Action listener */
	ButtonListener bl = null;
	RadioButtonListener rbl = null;
	CheckBoxListener cbl = null;
	SliderListener sl = null;
	
	public ViewingPanel() {
		// super class init
		super();
		setSize(300, 800);
		
		//
		// �t�@�C�����͂̃p�l��
		//
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(6,1));
		fileOpenButton = new JButton("File Open");
		viewResetButton = new JButton("View Reset");
		p1.add(fileOpenButton);
		p1.add(viewResetButton);
		edgeDissimilarityButton = new JRadioButton("Num. Edge by Dissimilarity");
		edgeDegreeButton = new JRadioButton("Num. Edge by Degree");
		ButtonGroup group1 = new ButtonGroup();
		p1.add(edgeDissimilarityButton);
		p1.add(edgeDegreeButton);
		group1.add(edgeDissimilarityButton);
		group1.add(edgeDegreeButton);
		
		clickButton = new JRadioButton("React by Click");
		moveButton = new JRadioButton("React by Move");
		ButtonGroup group2 = new ButtonGroup();
		JPanel p11 = new JPanel();
		p11.setLayout(new GridLayout(1,2));
		p11.add(clickButton);
		p11.add(moveButton);
		group2.add(clickButton);
		group2.add(moveButton);
		p1.add(p11);
		
		colorTopicButton = new JRadioButton("Topic Color");
		colorDegreeButton = new JRadioButton("Degree Color");
		ButtonGroup group3 = new ButtonGroup();
		JPanel p12 = new JPanel();
		p12.setLayout(new GridLayout(1,2));
		p12.add(colorTopicButton);
		p12.add(colorDegreeButton);
		group3.add(colorTopicButton);
		group3.add(colorDegreeButton);
		p1.add(p12);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(8,1));
		bundleDensitySlider = new JSlider(0, 100, 50);
		p2.add(new JLabel("Num. of Drawn Edges"));
		p2.add(bundleDensitySlider);
		bundleShapeSlider = new JSlider(0, 100, 50);
		p2.add(new JLabel("Bundle Shape (Linear <-> Curved)"));
		p2.add(bundleShapeSlider);
		transparencySlider = new JSlider(0, 100, 50);
		p2.add(new JLabel("Background Transparency"));
		p2.add(transparencySlider);
		keyEmphasisSlider = new JSlider(0, 100, 50);
		p2.add(new JLabel("Key Node Emphasis"));
		p2.add(keyEmphasisSlider);
		
		// �X���C�_�̃p�l��
		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(8, 1));
		p3.add(new JLabel(""));
		placeAgainButton = new JButton("Place Again");
		p3.add(placeAgainButton);
		placeRatioSlider = new JSlider(0, 100, 50);
		p3.add(new JLabel("Distance Ratio for Layout"));
		p3.add(placeRatioSlider);
		clusteringRatioSlider = new JSlider(0, 100, 50);
		p3.add(new JLabel("Distance Ratio for Clustering"));
		p3.add(clusteringRatioSlider);
		clusterSizeSlider = new JSlider(0, 100, 50);
		p3.add(new JLabel("Cluster Size Ratio (Small <-> Large)"));
		p3.add(clusterSizeSlider);
		
		//
		// �p�l���Q�̃��C�A�E�g
		//
		JPanel pp = new JPanel();
		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		pp.add(p1);
		pp.add(p2);
		pp.add(p3);
		
		pane = new JTabbedPane();
		pane.add(pp);
		pane.setTabComponentAt(0, new JLabel("Main"));
		this.add(pane);
		
		
		//
		// ���X�i�[�̒ǉ�
		//
		if (bl == null)
			bl = new ButtonListener();
		addButtonListener(bl);

		if (rbl == null)
			rbl = new RadioButtonListener();
		addRadioButtonListener(rbl);
		
		if (cbl == null)
			cbl = new CheckBoxListener();
		addCheckBoxListener(cbl);
		
		if (sl == null)
			sl = new SliderListener();
		addSliderListener(sl);
	}
	
	

	
	/**
	 * Canvas���Z�b�g����
	 * @param c Canvas
	 */
	public void setCanvas(Object c) {
		canvas = (Canvas) c;
		canvas.setViewingPanel(this);
	}
	
	/**
	 * FileOpener���Z�b�g����
	 */
	public void setFileOpener(FileOpener fo) {
		fileOpener = fo;
	}
	
	public void setCursorListener(CursorListener l) {
		listener = l;
	}

	/**
	 * �^�u�ŋ�؂�ꂽ�ʂ̃p�l�������
	 */
	public void generatePanels() {
		if(graph == null) return;
		
		if (textpanel != null) {
			textpanel.setVisible(false);
			textpanel = null;
			pane.remove(2);
		}
		if (vecpanel != null) {
			vecpanel.setVisible(false);
			vecpanel = null;
			pane.remove(1);
		}
		
		if(graph.attributeType == graph.ATTRIBUTE_VECTOR) {
			vecpanel = new VectorPanel(graph, (Object)canvas);
			pane.add(vecpanel);
			pane.setTabComponentAt(1, new JLabel("Vector"));
		}
		textpanel = new TextPanel(graph);
		pane.add(textpanel);
		pane.setTabComponentAt(2, new JLabel("Text"));
		
	}
	
	
	public void setPickedObject(Object picked) {
		if(textpanel != null)
			textpanel.setPickedObject(picked);
	}
	
	/**
	 * ���W�I�{�^���̃A�N�V�����̌��o��ݒ肷��
	 * @param actionListener ActionListener
	 */
	public void addRadioButtonListener(ActionListener actionListener) {
		edgeDissimilarityButton.addActionListener(actionListener);
		edgeDegreeButton.addActionListener(actionListener);
		clickButton.addActionListener(actionListener);
		moveButton.addActionListener(actionListener);
		colorDegreeButton.addActionListener(actionListener);
		colorTopicButton.addActionListener(actionListener);
	}

	/**
	 * �{�^���̃A�N�V�����̌��o��ݒ肷��
	 * @param actionListener ActionListener
	 */
	public void addButtonListener(ActionListener actionListener) {
		fileOpenButton.addActionListener(actionListener);
		placeAgainButton.addActionListener(actionListener);
		viewResetButton.addActionListener(actionListener);
	}
	
	
	/**
	 *CheckBox�̃A�N�V�����̌��o��ݒ肷��
	 * @param actionListener ActionListener
	 */
	public void addCheckBoxListener(ActionListener actionListener) {
	}
	
	
	/**
	 * �X���C�_�̃A�N�V�����̌��o��ݒ肷��
	 * @param actionListener ActionListener
	 */
	public void addSliderListener(ChangeListener changeListener) {
		bundleDensitySlider.addChangeListener(changeListener);
		bundleShapeSlider.addChangeListener(changeListener);
		transparencySlider.addChangeListener(changeListener);
		clusteringRatioSlider.addChangeListener(changeListener);
		placeRatioSlider.addChangeListener(changeListener);
		clusterSizeSlider.addChangeListener(changeListener);
		keyEmphasisSlider.addChangeListener(changeListener);
	}
	
	/**
	 * �{�^���̃A�N�V���������m����ActionListener
	 * @or itot
	 */
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			JButton buttonPushed = (JButton) e.getSource();
			if(buttonPushed == fileOpenButton) {	
				// �t�@�C�����w�肷��
				fileOpener.setCanvas(canvas);
				File datafile = fileOpener.getFile();
				currentDirectory = fileOpener.getCurrentDirectory();
				graph = fileOpener.readFile(datafile);
				canvas.setGraph(graph);
				generatePanels();
				canvas.viewReset();
				canvas.display();			
				
				for(int i = 0; i < SMOOTHING_ITERATION; i++) {
					MeshTriangulator.triangulate(graph.mesh);
					MeshSmoother.smooth(graph.mesh, graph.maxDegree);
					canvas.display();
				}
				
				graph.mesh.finalizePosition();
				canvas.display();
			}	
			
			if(buttonPushed == placeAgainButton) {
				if(graph == null) return;
				
				graph.postprocess();
				canvas.setGraph(graph);
				canvas.viewReset();
				canvas.display();
				
				for(int i = 0; i < SMOOTHING_ITERATION; i++) {
					MeshTriangulator.triangulate(graph.mesh);
					MeshSmoother.smooth(graph.mesh, graph.maxDegree);
					canvas.display();
				}

				graph.mesh.finalizePosition();				
				canvas.display();			
			}
			
			if(buttonPushed == viewResetButton) {
				canvas.viewReset();
				canvas.display();
			}
		}
	}

	/**
	 * ���W�I�{�^���̃A�N�V���������m����ActionListener
	 * @or itot
	 */
	class RadioButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JRadioButton buttonPushed = (JRadioButton) e.getSource();
			if(buttonPushed == edgeDissimilarityButton) {
				canvas.setEdgeDensityMode(canvas.EDGE_DENSITY_DISSIMILARITY);
				canvas.display();
			}
			if(buttonPushed == edgeDegreeButton) {
				canvas.setEdgeDensityMode(canvas.EDGE_DENSITY_DEGREE);
				canvas.display();
			}
			if(buttonPushed == clickButton) {
				listener.pickByMove(false);
			}
			if(buttonPushed == moveButton) {
				listener.pickByMove(true);
			}
			if(buttonPushed == colorTopicButton) {
				canvas.setColorMode(canvas.COLOR_TOPIC);
				canvas.display();
			}
			if(buttonPushed == colorDegreeButton) {
				canvas.setColorMode(canvas.COLOR_DEGREE);
				canvas.display();
			}
		}
	}
	

	/**
	 * �`�F�b�N�{�b�N�X�̃A�N�V���������m����ActionListener
	 * @or itot
	 */
    class CheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

		}
	}
    
	/**
	 * �X���C�_�̃A�N�V���������m����ActionListener
	 * @or itot
	 */
	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider sliderChanged = (JSlider) e.getSource();
			if(sliderChanged == bundleDensitySlider) {
				double ratio = (double)bundleDensitySlider.getValue() * 0.01;
				canvas.setEdgeDensityThreshold(ratio);
				canvas.display();
			}
			if(sliderChanged == bundleShapeSlider) {
				double ratio = (double)bundleShapeSlider.getValue() * 0.01;
				canvas.setBundleShape(ratio);
				canvas.display();
			}
			if(sliderChanged == transparencySlider) {
				double ratio = 1.0 - (double)transparencySlider.getValue() * 0.003;
				canvas.setBackgroundTransparency(ratio);
				canvas.display();
			}
			if(sliderChanged == placeRatioSlider) {
				double ratio = (double)placeRatioSlider.getValue() * 0.01;
				NodeDistanceCalculator.setPlacementRatio(ratio);
			}
			if(sliderChanged == clusteringRatioSlider) {
				double ratio = (double)clusteringRatioSlider.getValue() * 0.01;
				NodeDistanceCalculator.setClusteringRatio(ratio);
			}
			if(sliderChanged == clusterSizeSlider) {
				double ratio = (double)clusterSizeSlider.getValue() * 0.01;
				if(graph != null)
					graph.clustersizeRatio = ratio;
			}
			if(sliderChanged == keyEmphasisSlider) {
				double ratio = (double)keyEmphasisSlider.getValue() * 0.01;
				if(graph != null) {
					graph.mesh.keyEmphasis = ratio;
					canvas.display();			
				}
			}
		}
	}
	
}