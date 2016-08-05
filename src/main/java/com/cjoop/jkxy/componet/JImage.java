package com.cjoop.jkxy.componet;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * 图片容器，当前项目主要用于验证码的展示
 * @author chenjun
 *
 */
public class JImage extends JPanel {

	private static final long serialVersionUID = -7105011477016910202L;

	private ImageIcon icon;
	
	public JImage(String filename) {
		icon = new ImageIcon(filename);
	}
	
	public JImage(byte[]imageData){
		icon = new ImageIcon(imageData);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
	    Image img = icon.getImage();
	    g.drawImage(img, 0, 0, icon.getIconWidth(),
	    	      icon.getIconHeight(), icon.getImageObserver());
	}

}
