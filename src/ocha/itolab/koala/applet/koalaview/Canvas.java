package ocha.itolab.koala.applet.koalaview;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;

import ocha.itolab.koala.core.data.*;

/**
 * HeianView のための描画領域を管理する
 * @author itot
 */
public class Canvas extends JPanel {
	static int EDGE_DENSITY_DISSIMILARITY = 1;
	static int EDGE_DENSITY_DEGREE = 2;
	static int COLOR_TOPIC = 1;
	static int COLOR_DEGREE = 2;
	
	
	/* var */
	Transformer trans;
	Drawer drawer;
	BufferedImage image = null;
	ViewingPanel vp = null;
	GLCanvas glc;
	Graph graph = null;
	
	boolean isMousePressed = false, isAnnotation = true;
	int dragMode, dimMode, condenceLevel;
	int width, height, mouseX, mouseY;
	double linewidth = 1.0, bgR = 0.0, bgG = 0.0, bgB = 0.0;


	/**
	 * Constructor
	 * @param width 画面の幅
	 * @param height 画面の高さ
	 * @param foregroundColor 画面の前面色
	 * @param backgroundColor 画面の背景色
	 */
	public Canvas(
		int width,
		int height,
		Color foregroundColor,
		Color backgroundColor) {

		super(true);
		this.width = width;
		this.height = height;
		setSize(width, height);
		setColors(foregroundColor, backgroundColor);
		dragMode = 1;
		dimMode = 2;
	
		glc = new GLCanvas();
		
		drawer = new Drawer(width, height, glc);
		trans = new Transformer();
		trans.viewReset();
		drawer.setTransformer(trans);
		glc.addGLEventListener(drawer);
		
	}

	/**
	 * Constructor
	 * @param width 画面の幅
	 * @param height 画面の高さ
	 */
	public Canvas(int width, int height) {
		this(width, height, Color.black, Color.white);
	}

	public GLCanvas getGLCanvas(){
		return this.glc;
	}
	
	/**
	 * Drawer をセットする
	 * @param d Drawer
	 */
   public void setDrawer(Drawer d) {
		drawer = d;
	}

	/**
	 * Transformer をセットする
	 * @param t Transformer
	 */
	public void setTransformer(Transformer t) {
		trans = t;
	}

	public void setGraph(Graph g) {
		graph = g;
		drawer.setGraph(g);
	}


	/**
	 * ViewingPanelをセットする
	 */
	public void setViewingPanel(ViewingPanel v) {
		vp = v;
		drawer.setViewingPanel(vp);
	}
	
	public void setEdgeDensityThreshold(double ratio) {
		drawer.setEdgeThreshold(ratio);
	}
	
	public void setBundleShape(double ratio) {
		drawer.setBundleShape(ratio);
	}
	
	public void setEdgeDensityMode(int mode) {
		drawer.setEdgeDensityMode(mode);
	}
	
	public void setBackgroundTransparency(double t) {
		drawer.setBackgroundTransparency(t);
	}
	
	public void setColorSwitch(boolean colorSwitch[]) {
		drawer.setColorSwitch(colorSwitch);
	}
	
	public void setColorMode(int mode) {
		drawer.setColorMode(mode);
	}
	
	/**
	 * 再描画
	 */
	public void display() {
		if (drawer == null) return;

		GLAutoDrawable glAD = null;
		width = (int) getSize().getWidth();
		height = (int) getSize().getHeight();

		glAD = drawer.getGLAutoDrawable();
		if (glAD == null) return;
				
		drawer.getGLAutoDrawable();
		drawer.setWindowSize(width, height);
		glAD.display();

	}

	/**
	 * 画像ファイルに出力する
	 */
	public void saveImageFile(File file) {

		width = (int) getSize().getWidth();
		height = (int) getSize().getHeight();
		image = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_BGR);
		
		/*
		Graphics2D gg2 = image.createGraphics();
		gg2.clearRect(0, 0, width, height);
		b_drawer.draw(gg2);
		d_drawer.draw(gg2);
		try {
			ImageIO.write(image, "bmp", file);
		} catch(Exception e) {
			e.printStackTrace();
		}	
		*/	
	}
	
	
	/**
	 * 前面色と背景色をセットする
	 * @param foregroundColor 前面色
	 * @param backgroundColor 背景色
	 */
	public void setColors(Color foregroundColor, Color backgroundColor) {
		setForeground(foregroundColor);
		setBackground(backgroundColor);
	}


	/**
	 * マウスボタンが押されたモードを設定する
	 */
	public void mousePressed() {
		isMousePressed = true;
		trans.mousePressed();
		drawer.setMousePressSwitch(isMousePressed);
	}

	/**
	 * マウスボタンが離されたモードを設定する
	 */
	public void mouseReleased() {
		isMousePressed = false;
		drawer.setMousePressSwitch(isMousePressed);
	}

	/**
	 * マウスがドラッグされたモードを設定する
	 * @param xStart 直前のX座標値
	 * @param xNow 現在のX座標値
	 * @param yStart 直前のY座標値
	 * @param yNow 現在のY座標値
	 */
	public void drag(int xStart, int xNow, int yStart, int yNow) {
		int x = xNow - xStart;
		int y = yNow - yStart;

		trans.drag(x, y, width, height, dragMode);
	}


	/**
	 * 線の太さをセットする
	 * @param linewidth 線の太さ（画素数）
	 */
	public void setLinewidth(double linewidth) {
		this.linewidth = linewidth;
		drawer.setLinewidth(linewidth);
	}


	/**
	 * 背景色をr,g,bの3値で設定する
	 * @param r 赤（0～1）
	 * @param g 緑（0～1）
	 * @param b 青（0～1）
	 */
	public void setBackground(double r, double g, double b) {
		bgR = r;
		bgG = g;
		bgB = b;
		setBackground(
			new Color((int) (r * 255), (int) (g * 255), (int) (b * 255)));
	}

	/**
	 * マウスドラッグのモードを設定する
	 * @param dragMode (1:ZOOM  2:SHIFT  3:ROTATE)
	 */
	public void setDragMode(int newMode) {
		dragMode = newMode;
		drawer.setDragMode(dragMode);
	}
	
	/**
	 * マウスドラッグのモードを返す
	 * @param dragMode (1:ZOOM  2:SHIFT  3:ROTATE)
	 */
	public int getDragMode() {
		return dragMode;
	}


	
	/**
	 * 画面表示の拡大縮小・回転・平行移動の各状態をリセットする
	 */
	public void viewReset() {
		trans.viewReset();
	}

	/**
	 * 画面表示の拡大縮小・回転・平行移動の各状態を初期設定にあわせる
	 */
	public void viewDefault() {
		trans.setDefaultValue();
	}

	
	/**
	 * 物体をピックする
	 */
	public Object pick(int x, int y) {
		return drawer.pick(x, y);
	}
	
	
	/**
	 * マウスカーソルのイベントを検知する設定を行う
	 * @param eventListener EventListner
	 */
	public void addCursorListener(EventListener eventListener) {
		addMouseListener((MouseListener) eventListener);
		addMouseMotionListener((MouseMotionListener) eventListener);
	}
	
}
