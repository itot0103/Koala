package ocha.itolab.koala.applet.koalaview;

import ocha.itolab.koala.core.data.*;

import java.awt.*;
import java.util.*;
import java.io.*;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;

public class KoalaViewer extends JApplet {

	// GUI element
	MenuBar menuBar;
	ViewingPanel viewingPanel = null; 
	CursorListener cl;
	FileOpener fileOpener;
	Canvas canvas;
	Container windowContainer;
	
	
	
	/**
	 * applet を初期化し、各種データ構造を初期化する
	 */
	public void init() {
		setSize(new Dimension(1000,800));
		buildGUI();
	}

	/**
	 * applet の各イベントの受付をスタートする
	 */
	public void start() {
	}

	/**
	 * applet の各イベントの受付をストップする
	 */
	public void stop() {
	}
	
	/**
	 * applet等を初期化する
	 */
	private void buildGUI() {

		// Canvas
		canvas = new Canvas(512, 512);
		canvas.requestFocus();
		GLCanvas glc = canvas.getGLCanvas();
		
		// ViewingPanel
		fileOpener = new FileOpener();
		viewingPanel = new ViewingPanel();
		viewingPanel.setCanvas(canvas);
		viewingPanel.setFileOpener(fileOpener);
	
		// MenuBar
		menuBar = new MenuBar();
		menuBar.setCanvas(canvas);
		
		// CursorListener
		cl = new CursorListener();
		cl.setCanvas(canvas, glc);
		cl.setViewingPanel(viewingPanel);
		canvas.addCursorListener(cl);
		
		// CanvasとsubPanelのレイアウト
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(glc, BorderLayout.CENTER);
		mainPanel.add(viewingPanel, BorderLayout.WEST);
		
		// ウィンドウ上のレイアウト
		windowContainer = this.getContentPane();
		windowContainer.setLayout(new BorderLayout());
		windowContainer.add(mainPanel, BorderLayout.CENTER);
		windowContainer.add(menuBar, BorderLayout.NORTH);
		
	}
	
	
	/**
	 * main関数
	 * @param args 実行時の引数
	 */
	public static void main(String[] args) {
		ocha.itolab.koala.applet.Window window =
				new ocha.itolab.koala.applet.Window(
				"KoalaViewer", 800, 600, Color.lightGray); //Windowを作成
		KoalaViewer bv = new KoalaViewer(); //システムを起動

		bv.init(); //システム初期化
		window.getContentPane().add(bv); //windowにシステムを渡す
		window.setVisible(true); //見えるようにする

		bv.start(); //システムを扱えるようにしている
		
	}

}
