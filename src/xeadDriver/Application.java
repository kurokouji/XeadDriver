package xeadDriver;

/*
 * Copyright (c) 2011 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class Application {
	private JWindow splashScreen;
	private JLabel  splashIcon;
	private JLabel  splashLabel;

	public Application(String[] args) {
		ImageIcon image = new ImageIcon(xeadDriver.Application.class.getResource("splash.png"));
		splashIcon = new JLabel(image);
		splashIcon.setLayout(null);
		splashLabel = new JLabel();
		splashLabel.setFont(new java.awt.Font("Dialog", 0, 12));
		splashLabel.setForeground(Color.cyan);
		splashLabel.setOpaque(false);
		splashLabel.setBounds(0, 92, 500, 15);
		splashLabel.setHorizontalAlignment(SwingConstants.CENTER);
		splashLabel.setText(XFUtility.RESOURCE.getString("SplashMessage0"));
		splashIcon.add(splashLabel);
		splashScreen = new JWindow();
		splashScreen.getContentPane().add(splashIcon);
		splashScreen.pack();
		splashScreen.setLocationRelativeTo(null);
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				showSplash();
			}
		});
		new Session(args, this);
	}

	public static void main(String[] args) {
		try {
			UIManager.getInstalledLookAndFeels(); 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		new Application(args);
	}
	
	public void showSplash() {
		splashScreen.setVisible(true);
	}
	
	public void setTextOnSplash(String text) {
		splashLabel.setText(text);
	}

	public void hideSplash() {
		if (splashScreen != null) {
			splashScreen.setVisible(false);
			splashScreen = null;
			splashLabel  = null;
		}
	}
}
