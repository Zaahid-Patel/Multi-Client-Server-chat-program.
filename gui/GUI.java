package gui;

/**
 *	@author 22591214-Dirk Stanley de Waal
 *	@version 1.0
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import java.util.EventObject;
import client.ClientChannelThread;
import java.util.*;

/**
 *	
 * Main class	
 *	
 */

public class GUI implements ActionListener {

		public JFrame LoginFrame;
		public JButton LoginButton;
		public JLabel LoginLabel;
		public JLabel LoginFieldLabel;
		public JTextField LoginField;
		public JFrame ChatFrame;
		public JLabel ChatLabel;
		public JButton SendButton;
		public JButton LogoutButton;
		public JTextArea UserList;
		public JTextArea Chat;
		public JTextArea MessageField;
		public JScrollPane Scroll;
		public String MessageText;
		public ClientChannelThread client;
		public JFrame DisconnectFrame;
		public JLabel DisconnectLabel;
		public JButton DisconnectButton;
		public JLabel DupNameLabel;
		public JFrame DupErrFrame;
		public JLabel DupErrLabel;
		public JButton DupErrButton;
/**
 *	
 *	@params args - default, not used.
 *	
 *	Main method left empty. Not used.	
 *
 */


	public static void main(String[] args) {
		
	}

/**
 * 
 *	@params c - of type ClientChannelThead
 *
 *	Method initialises all th GUI components and sets each components
 *	properties. 
 *	
 *
 */

	public GUI(ClientChannelThread c) {

		client = c;

		//LoginScene
		LoginButton = new JButton("Login");
		LoginButton.addActionListener(this);	
		LoginFrame = new JFrame();									//init components
		LoginLabel = new JLabel("Back2Basics");
		LoginFieldLabel = new JLabel("Enter username:");
		LoginField = new JTextField("");
		DupNameLabel = new JLabel("Username already exists!");
		
		LoginButton.setBounds(200,325,100,50);
		LoginButton.addActionListener(this);	

		LoginLabel.setBounds(150,100,250,25);					
		LoginLabel.setFont(new Font("Arial", Font.BOLD, 30));	

		DupNameLabel.setVisible(false);
		DupNameLabel.setBounds(150,225,200,25);
		DupNameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		DupNameLabel.setForeground(Color.RED);

		LoginField.setFont(new Font("Arial", Font.PLAIN, 16));
		LoginField.setBounds(150,200,200,25);

		LoginFieldLabel.setBounds(150,150,200,25);

		LoginFrame.add(DupNameLabel);
		LoginFrame.add(LoginFieldLabel);
		LoginFrame.add(LoginButton);								//Add components to frame
		LoginFrame.add(LoginLabel);
		LoginFrame.add(LoginField);
		LoginFrame.setSize(500,500);							
		LoginFrame.setLayout(null);
		LoginFrame.setVisible(true);

		//ChatScene
		ChatFrame = new JFrame();									//init components				
		ChatLabel = new JLabel("Chat");					
		SendButton = new JButton("Send");				
		LogoutButton = new JButton("Logout");			
		UserList = new JTextArea("Users:\n");				
		Chat = new JTextArea("Welcome to the chatroom:\n");						
		MessageField = new JTextArea("");				
		Scroll = new JScrollPane(Chat);

		ChatFrame.setVisible(false);

		ChatLabel.setBounds(225,50,100,50);	
		ChatLabel.setFont(new Font("Arial", Font.PLAIN, 30));

		UserList.setBounds(325,100,140,650);
		UserList.setLineWrap(true);
		UserList.setWrapStyleWord(true);
		UserList.setEditable(false);

		Chat.setBounds(25,100,250,650);
		Chat.setLineWrap(true);
		Chat.setWrapStyleWord(true);
		Chat.setEditable(false);

		Scroll.setAutoscrolls(true);
		Scroll.validate();

		MessageField.setBounds(25,775,250,50);
		MessageField.setLineWrap(true);
		MessageField.setWrapStyleWord(true);
		
		SendButton.setBounds(300,775,75,25);
		SendButton.addActionListener(this);
		
		LogoutButton.setBounds(400,775,90,25);
		LogoutButton.addActionListener(this);
		
		ChatFrame.add(Scroll);										//Add components to frame
		ChatFrame.add(LogoutButton);
		ChatFrame.add(SendButton);
		ChatFrame.add(MessageField);
		ChatFrame.add(Chat);
		ChatFrame.add(UserList);
		ChatFrame.add(ChatLabel);
		ChatFrame.setSize(500,900);
		ChatFrame.setLayout(null);

		//Server Disconnect

		DisconnectFrame = new JFrame();								//init components
        DisconnectLabel = new JLabel("Server has been terminated");
		DisconnectButton = new JButton("Ok");

		DisconnectLabel.setBounds(150,75,200,50);

		DisconnectButton.setBounds(200,125,100,25);
		DisconnectButton.addActionListener(this);

		DisconnectFrame.add(DisconnectLabel);						//add components to frame
		DisconnectFrame.add(DisconnectButton);
		DisconnectFrame.setSize(500,300);
		DisconnectFrame.setLayout(null);
		
		//Duplicate err

		DupErrFrame = new JFrame();									//init components
        DupErrLabel = new JLabel("Username already exists!");
		DupErrButton = new JButton("Ok");

		DupErrLabel.setBounds(150,75,200,50);

		DupErrButton.setBounds(200,125,100,25);
		DupErrButton.addActionListener(this);

		DupErrFrame.add(DupErrLabel);								//add components to frame
		DupErrFrame.add(DupErrButton);
		DupErrFrame.setSize(500,300);
		DupErrFrame.setLayout(null);

	}
/**
 *
 *	@params e - of type ActionEvent - 	Works with ActionListener so that
 *										components have functionality.
 *
 *	Method overrides default actionPerformed method to give components
 *	functiobality.
 *
 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == LoginButton) {							//LoginButtonEvent
				
				client.addUsername(LoginField.getText());
				LoginFrame.setVisible(false);
				ChatFrame.setVisible(true);
		}


        if (e.getSource() == SendButton) {							//SendButtonEvent

			client.addOutput(MessageField.getText());
			
    	}	

		if (e.getSource() == DisconnectButton) {					//DisconnectButtonEvent

			System.exit(0);

		}


		if (e.getSource() == LogoutButton) {						//LogoutButtonEvent
					
			System.exit(0);
		
		}

		if (e.getSource() == DupErrButton) {						//DuplicationErrorEvent
			System.exit(0);
		}

	}


}
