package ocha.itolab.koala.applet.koalaview;

import java.awt.event.*;

import javax.swing.*;

import ocha.itolab.koala.core.data.*;

/*
 * Plotter のためのMenuBarを構築する
 * @or itot
 */
public class MenuBar extends JMenuBar {

	/* var */
	// file menu 
	public JMenu fileMenu;
	public JMenuItem exitMenuItem;

	// help menu 
	public JMenu helpMenu;
	public JMenuItem helpMenuItem;

	// Listener
	MenuItemListener ml;
	
	// component
	Canvas canvas = null;

	
	/**
	 * Constructor
	 * @param withReadyMadeMenu 通常はtrue
	 */
	public MenuBar() {
		super();
		buildFileMenu();
		buildHelpMenu();
			
		ml = new MenuItemListener();
		this.addMenuListener(ml);
		
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}



	/**
	 * Fileに関するメニューを構築する
	 */
	public void buildFileMenu() {

		// create file menu
		fileMenu = new JMenu("File");
		add(fileMenu);

		// add menu item
		exitMenuItem = new JMenuItem("Exit");
		fileMenu.add(exitMenuItem);
	}



	
	
	/**
	 * Help に関するメニューを構築する
	 */
	public void buildHelpMenu() {

		// create help menu
		helpMenu = new JMenu("Help");
		add(helpMenu);

		// add menu item
		helpMenuItem = new JMenuItem("Help...");
		helpMenu.add(helpMenuItem);

	}

	
	/**
	 * Canvas をセットする
	 */
	public void setCanvas(Canvas c) {
		canvas = c;
	}
	


	/**
	 * 選択されたメニューアイテムを返す
	 * @param name 選択されたメニュー名
	 * @return JMenuItem 選択されたメニューアイテム
	 */
	public JMenuItem getMenuItem(String name) {
		//file
		if (exitMenuItem.getText().equals(name))
			return exitMenuItem;


		//help
		else if (helpMenuItem.getText().equals(name))
			return helpMenuItem;

		// other
		return null;
	}

	/**
	 * メニューに関するアクションの検知を設定する
	 * @param actionListener ActionListener
	 */
	public void addMenuListener(ActionListener actionListener) {
		exitMenuItem.addActionListener(actionListener);
		helpMenuItem.addActionListener(actionListener);
	}
	
	

	/**
	 * メニューの各イベントを検出し、それに対応するコールバック処理を呼び出す
	 * 
	 * @or itot
	 */
	class MenuItemListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JMenuItem menuItem = (JMenuItem) e.getSource();
			if(exitMenuItem == menuItem) 
				System.exit(0);
		}
	}

}
