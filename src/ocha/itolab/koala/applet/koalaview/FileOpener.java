package ocha.itolab.koala.applet.koalaview;

import java.io.File;
import java.util.Vector;
import java.awt.*;
import javax.swing.*;

import ocha.itolab.koala.core.data.*;


public class FileOpener {

	File currentDirectory, inputFile, outputFile;
	Component windowContainer;
	Canvas canvas;
	
	

	
	/**
	 * Container ���Z�b�g����
	 * @param c Component
	 */
	public void setContainer(Component c) {
		windowContainer = c;
	}
	
	
	/**
	 * Canvas ���Z�b�g����
	 * @param c Canvas
	 */
	public void setCanvas(Canvas c) {
		canvas = c;
	}
	
	/**
	 * �t�@�C���_�C�A���O�ɃC�x���g���������Ƃ��ɁA�Ή�����t�@�C������肷��
	 * @return �t�@�C��
	 */
	public File getFile() {
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		int selected = fileChooser.showOpenDialog(windowContainer);
		if (selected == JFileChooser.APPROVE_OPTION) { // open selected
			currentDirectory = fileChooser.getCurrentDirectory();
			return fileChooser.getSelectedFile();
		} else if (selected == JFileChooser.CANCEL_OPTION) { // cancel selected
			return null;
		} 
		
		return null;
	}
	
	
	/**
	 * �J�����g�f�B���N�g����Ԃ�
	 */
	public File getCurrentDirectory() {
		return currentDirectory;
	}
	
	
	/**
	 * csv�t�@�C����ǂݍ���
	 */
	public Graph readFile(File file) {
		Graph graph = null;
		graph = GraphFileReader.readConnectivity(file.getAbsolutePath());
		return graph;
	}
	
	
}
