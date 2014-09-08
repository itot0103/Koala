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
	 * applet �����������A�e��f�[�^�\��������������
	 */
	public void init() {
		setSize(new Dimension(1000,800));
		buildGUI();
	}

	/**
	 * applet �̊e�C�x���g�̎�t���X�^�[�g����
	 */
	public void start() {
	}

	/**
	 * applet �̊e�C�x���g�̎�t���X�g�b�v����
	 */
	public void stop() {
	}
	
	/**
	 * applet��������������
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
		
		// Canvas��subPanel�̃��C�A�E�g
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(glc, BorderLayout.CENTER);
		mainPanel.add(viewingPanel, BorderLayout.WEST);
		
		// �E�B���h�E��̃��C�A�E�g
		windowContainer = this.getContentPane();
		windowContainer.setLayout(new BorderLayout());
		windowContainer.add(mainPanel, BorderLayout.CENTER);
		windowContainer.add(menuBar, BorderLayout.NORTH);
		
	}
	
	
	/**
	 * main�֐�
	 * @param args ���s���̈���
	 */
	public static void main(String[] args) {
		ocha.itolab.koala.applet.Window window =
				new ocha.itolab.koala.applet.Window(
				"KoalaViewer", 800, 600, Color.lightGray); //Window���쐬
		KoalaViewer bv = new KoalaViewer(); //�V�X�e�����N��

		bv.init(); //�V�X�e��������
		window.getContentPane().add(bv); //window�ɃV�X�e����n��
		window.setVisible(true); //������悤�ɂ���

		bv.start(); //�V�X�e����������悤�ɂ��Ă���
		
	}

}
