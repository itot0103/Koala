package ocha.itolab.koala.applet.koalaview;

import java.awt.event.*;

import javax.swing.*;

import ocha.itolab.koala.core.data.*;

/*
 * Plotter �̂��߂�MenuBar���\�z����
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
	 * @param withReadyMadeMenu �ʏ��true
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
	 * File�Ɋւ��郁�j���[���\�z����
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
	 * Help �Ɋւ��郁�j���[���\�z����
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
	 * Canvas ���Z�b�g����
	 */
	public void setCanvas(Canvas c) {
		canvas = c;
	}
	


	/**
	 * �I�����ꂽ���j���[�A�C�e����Ԃ�
	 * @param name �I�����ꂽ���j���[��
	 * @return JMenuItem �I�����ꂽ���j���[�A�C�e��
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
	 * ���j���[�Ɋւ���A�N�V�����̌��m��ݒ肷��
	 * @param actionListener ActionListener
	 */
	public void addMenuListener(ActionListener actionListener) {
		exitMenuItem.addActionListener(actionListener);
		helpMenuItem.addActionListener(actionListener);
	}
	
	

	/**
	 * ���j���[�̊e�C�x���g�����o���A����ɑΉ�����R�[���o�b�N�������Ăяo��
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
