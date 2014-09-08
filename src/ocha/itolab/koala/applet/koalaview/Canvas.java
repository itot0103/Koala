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
 * HeianView �̂��߂̕`��̈���Ǘ�����
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
	 * @param width ��ʂ̕�
	 * @param height ��ʂ̍���
	 * @param foregroundColor ��ʂ̑O�ʐF
	 * @param backgroundColor ��ʂ̔w�i�F
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
	 * @param width ��ʂ̕�
	 * @param height ��ʂ̍���
	 */
	public Canvas(int width, int height) {
		this(width, height, Color.black, Color.white);
	}

	public GLCanvas getGLCanvas(){
		return this.glc;
	}
	
	/**
	 * Drawer ���Z�b�g����
	 * @param d Drawer
	 */
   public void setDrawer(Drawer d) {
		drawer = d;
	}

	/**
	 * Transformer ���Z�b�g����
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
	 * ViewingPanel���Z�b�g����
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
	 * �ĕ`��
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
	 * �摜�t�@�C���ɏo�͂���
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
	 * �O�ʐF�Ɣw�i�F���Z�b�g����
	 * @param foregroundColor �O�ʐF
	 * @param backgroundColor �w�i�F
	 */
	public void setColors(Color foregroundColor, Color backgroundColor) {
		setForeground(foregroundColor);
		setBackground(backgroundColor);
	}


	/**
	 * �}�E�X�{�^���������ꂽ���[�h��ݒ肷��
	 */
	public void mousePressed() {
		isMousePressed = true;
		trans.mousePressed();
		drawer.setMousePressSwitch(isMousePressed);
	}

	/**
	 * �}�E�X�{�^���������ꂽ���[�h��ݒ肷��
	 */
	public void mouseReleased() {
		isMousePressed = false;
		drawer.setMousePressSwitch(isMousePressed);
	}

	/**
	 * �}�E�X���h���b�O���ꂽ���[�h��ݒ肷��
	 * @param xStart ���O��X���W�l
	 * @param xNow ���݂�X���W�l
	 * @param yStart ���O��Y���W�l
	 * @param yNow ���݂�Y���W�l
	 */
	public void drag(int xStart, int xNow, int yStart, int yNow) {
		int x = xNow - xStart;
		int y = yNow - yStart;

		trans.drag(x, y, width, height, dragMode);
	}


	/**
	 * ���̑������Z�b�g����
	 * @param linewidth ���̑����i��f���j
	 */
	public void setLinewidth(double linewidth) {
		this.linewidth = linewidth;
		drawer.setLinewidth(linewidth);
	}


	/**
	 * �w�i�F��r,g,b��3�l�Őݒ肷��
	 * @param r �ԁi0�`1�j
	 * @param g �΁i0�`1�j
	 * @param b �i0�`1�j
	 */
	public void setBackground(double r, double g, double b) {
		bgR = r;
		bgG = g;
		bgB = b;
		setBackground(
			new Color((int) (r * 255), (int) (g * 255), (int) (b * 255)));
	}

	/**
	 * �}�E�X�h���b�O�̃��[�h��ݒ肷��
	 * @param dragMode (1:ZOOM  2:SHIFT  3:ROTATE)
	 */
	public void setDragMode(int newMode) {
		dragMode = newMode;
		drawer.setDragMode(dragMode);
	}
	
	/**
	 * �}�E�X�h���b�O�̃��[�h��Ԃ�
	 * @param dragMode (1:ZOOM  2:SHIFT  3:ROTATE)
	 */
	public int getDragMode() {
		return dragMode;
	}


	
	/**
	 * ��ʕ\���̊g��k���E��]�E���s�ړ��̊e��Ԃ����Z�b�g����
	 */
	public void viewReset() {
		trans.viewReset();
	}

	/**
	 * ��ʕ\���̊g��k���E��]�E���s�ړ��̊e��Ԃ������ݒ�ɂ��킹��
	 */
	public void viewDefault() {
		trans.setDefaultValue();
	}

	
	/**
	 * ���̂��s�b�N����
	 */
	public Object pick(int x, int y) {
		return drawer.pick(x, y);
	}
	
	
	/**
	 * �}�E�X�J�[�\���̃C�x���g�����m����ݒ���s��
	 * @param eventListener EventListner
	 */
	public void addCursorListener(EventListener eventListener) {
		addMouseListener((MouseListener) eventListener);
		addMouseMotionListener((MouseMotionListener) eventListener);
	}
	
}
