package ocha.itolab.koala.applet.koalaview;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import ocha.itolab.koala.core.data.*;
import ocha.itolab.koala.core.mesh.*;

public class ViewingPanel extends JPanel {
	static int SMOOTHING_ITERATION = 100;
	
	public JButton  fileOpenButton, placeAgainButton, viewResetButton, imageSaveButton;
	public JSlider  bundleDensitySlider, bundleShapeSlider, transparencySlider,
		placeRatioSlider, clusteringRatioSlider, clusterSizeSlider, keyEmphasisSlider;
	public JRadioButton edgeDissimilarityButton, edgeDegreeButton, clickButton, moveButton, 
		colorTopicButton, colorDegreeButton, saveAIButton, saveULButton, saveURButton, saveLLButton, saveLRButton;
	public JTextField filenameField;
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
	
	int saveflag = Drawer.SAVE_AS_IS;
	
	
	public ViewingPanel() {
		// super class init
		super();
		setSize(300, 800);
		
		//
		// ファイル入力のパネル
		//
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(7,1));
		fileOpenButton = new JButton("File Open");
		filenameField = new JTextField("");
		viewResetButton = new JButton("View Reset");
		p1.add(fileOpenButton);
		p1.add(filenameField);
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
		
		// スライダのパネル
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
		
		JPanel p4 = new JPanel();
		p4.setLayout(new GridLayout(6, 1));
		imageSaveButton = new JButton("Image Save");
		p4.add(imageSaveButton);
		ButtonGroup group4 = new ButtonGroup();
		saveAIButton = new JRadioButton("As Is");
		saveULButton = new JRadioButton("Upper Left");
		saveURButton = new JRadioButton("Upper Right");
		saveLLButton = new JRadioButton("Lower Left");
		saveLRButton = new JRadioButton("Lower Right");
		p4.add(saveAIButton);
		p4.add(saveULButton);
		p4.add(saveURButton);
		p4.add(saveLLButton);
		p4.add(saveLRButton);
		group4.add(saveAIButton);
		group4.add(saveULButton);
		group4.add(saveURButton);
		group4.add(saveLLButton);
		group4.add(saveLRButton);
		
		//
		// パネル群のレイアウト
		//
		JPanel pp = new JPanel();
		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		pp.add(p1);
		pp.add(p2);
		pp.add(p3);
		pp.add(p4);
		
		pane = new JTabbedPane();
		pane.add(pp);
		pane.setTabComponentAt(0, new JLabel("Main"));
		this.add(pane);
		
		
		//
		// リスナーの追加
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
	 * Canvasをセットする
	 * @param c Canvas
	 */
	public void setCanvas(Object c) {
		canvas = (Canvas) c;
		canvas.setViewingPanel(this);
	}
	
	/**
	 * FileOpenerをセットする
	 */
	public void setFileOpener(FileOpener fo) {
		fileOpener = fo;
	}
	
	public void setCursorListener(CursorListener l) {
		listener = l;
	}

	/**
	 * タブで区切られた別のパネルを作る
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
	 * ラジオボタンのアクションの検出を設定する
	 * @param actionListener ActionListener
	 */
	public void addRadioButtonListener(ActionListener actionListener) {
		edgeDissimilarityButton.addActionListener(actionListener);
		edgeDegreeButton.addActionListener(actionListener);
		clickButton.addActionListener(actionListener);
		moveButton.addActionListener(actionListener);
		colorDegreeButton.addActionListener(actionListener);
		colorTopicButton.addActionListener(actionListener);
		saveAIButton.addActionListener(actionListener);
		saveULButton.addActionListener(actionListener);
		saveURButton.addActionListener(actionListener);
		saveLLButton.addActionListener(actionListener);
		saveLRButton.addActionListener(actionListener);
	}

	/**
	 * ボタンのアクションの検出を設定する
	 * @param actionListener ActionListener
	 */
	public void addButtonListener(ActionListener actionListener) {
		fileOpenButton.addActionListener(actionListener);
		placeAgainButton.addActionListener(actionListener);
		imageSaveButton.addActionListener(actionListener);
		viewResetButton.addActionListener(actionListener);
	}
	
	
	/**
	 *CheckBoxのアクションの検出を設定する
	 * @param actionListener ActionListener
	 */
	public void addCheckBoxListener(ActionListener actionListener) {
	}
	
	
	/**
	 * スライダのアクションの検出を設定する
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
	 * ボタンのアクションを検知するActionListener
	 * @or itot
	 */
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			JButton buttonPushed = (JButton) e.getSource();
			if(buttonPushed == fileOpenButton) {	
				// ファイルを指定する
				fileOpener.setCanvas(canvas);
				File datafile = fileOpener.getFile();
				String filename = datafile.getName();
				currentDirectory = fileOpener.getCurrentDirectory();
				filenameField.setText(filename);
				graph = fileOpener.readFile(datafile);
				canvas.setGraph(graph);
				generatePanels();
				canvas.viewReset();
				canvas.display();			
				
				long t1 = System.currentTimeMillis();
				
				for(int i = 0; i < SMOOTHING_ITERATION; i++) {
					MeshTriangulator.triangulate(graph.mesh);
					MeshSmoother.smooth(graph.mesh, graph.maxDegree);
					canvas.display();
				}
				
				
				long t2 = System.currentTimeMillis();
				System.out.println("[TIME] for mesh operation: " + (t2-t1));
				
				long t3 = System.currentTimeMillis();
				
				graph.mesh.finalizePosition();
				
				long t4 = System.currentTimeMillis();
				System.out.println("[TIME] for final node&edge operation: " + (t4-t3));
				
				//TulipFileWriter.write(graph);
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
				//TulipFileWriter.write(graph);
				canvas.display();			
			}
			
			
			if(buttonPushed == imageSaveButton) {
				canvas.saveImageFile(saveflag);
				canvas.display();
				canvas.display();
			}
			
			
			if(buttonPushed == viewResetButton) {
				canvas.viewReset();
				canvas.display();
			}
		}
	}

	/**
	 * ラジオボタンのアクションを検知するActionListener
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
			if(buttonPushed == saveAIButton) {
				saveflag = Drawer.SAVE_AS_IS;
			}
			if(buttonPushed == saveULButton) {
				saveflag = Drawer.SAVE_UPPER_LEFT;
			}
			if(buttonPushed == saveURButton) {
				saveflag = Drawer.SAVE_UPPER_RIGHT;
			}
			if(buttonPushed == saveLLButton) {
				saveflag = Drawer.SAVE_LOWER_LEFT;
			}
			if(buttonPushed == saveLRButton) {
				saveflag = Drawer.SAVE_LOWER_RIGHT;
			}
		}
	}
	

	/**
	 * チェックボックスのアクションを検知するActionListener
	 * @or itot
	 */
    class CheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

		}
	}
    
	/**
	 * スライダのアクションを検知するActionListener
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
