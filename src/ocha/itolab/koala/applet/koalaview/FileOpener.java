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
	 * Container をセットする
	 * @param c Component
	 */
	public void setContainer(Component c) {
		windowContainer = c;
	}
	
	
	/**
	 * Canvas をセットする
	 * @param c Canvas
	 */
	public void setCanvas(Canvas c) {
		canvas = c;
	}
	
	/**
	 * ファイルダイアログにイベントがあったときに、対応するファイルを特定する
	 * @return ファイル
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
	 * カレントディレクトリを返す
	 */
	public File getCurrentDirectory() {
		return currentDirectory;
	}
	
	
	/**
	 * csvファイルを読み込む
	 */
	public Graph readFile(File file) {
		Graph graph = null;
		graph = GraphFileReader.readConnectivity(file.getAbsolutePath());
		return graph;
	}
	
	
}
