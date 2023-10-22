package client;

import java.net.*;
import java.util.*;
import server.ServerCodes;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import gui.GUI;
import java.awt.event.*;


/**
 * Class handles Socket for Client and User Input.
 */
public class ClientChannelThread extends Thread {
    String hostName;
    int portNumber;
    public static boolean running = true;
	boolean GUIMODE = true;				//temp
    public static List<String> output = new ArrayList<String>();
	GUI clientGUI = new GUI(this);

    static String username = "";

    public ClientChannelThread(String name, int port) {
        hostName = name;
        portNumber = port;
    }

    @Override
    public void run() {
        try {
            System.out.println("\nEnter a Username:");
            while (needUsername()) {
                // Waits for username to be selected
            }
            output.add(ServerCodes.CODE_NAME + username);
            System.out.println("Got Username");
            SocketChannel serverChannel = SocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.connect(new InetSocketAddress(hostName, portNumber));
            while (!serverChannel.finishConnect()) {
                System.out.println("waiting...");
            }

            // Same code as in ServerChannel thread, with a couple of tweaks
            while (running) {
                if (output.size() > 0) {
                    // Output
                    System.out.println("Output: " + username + " : " + output.get(0));
                    ByteBuffer buf = ByteBuffer.allocate(100);
                    buf.clear();
					
				//	if (GUIMODE) {
				//		buf.put(clientGUI.MessageText.getBytes());
				//	} else {
                    	buf.put(output.get(0).getBytes());
				//	}
					
					
                    buf.flip();
                    while (buf.hasRemaining()) {
                        serverChannel.write(buf);
                    }
					
                    output.remove(0);
                } else {
                    // Input
                    ByteBuffer buf = ByteBuffer.allocate(100);
                    int bytesRead = serverChannel.read(buf);

					if (bytesRead < 0) {
						running = false;
					}

                    if (bytesRead != 0) {
                        System.out.println("Getting input...");
                        buf.rewind();
                        String input = "";
                        while (buf.hasRemaining()) {
                            input = input + (char) buf.get();
                        }

                        if (input.contains(ServerCodes.ERR_DUPLICATE_USER)) {
                            clientGUI.DupNameLabel.setVisible(true);
							clientGUI.LoginFrame.setVisible(false);
							clientGUI.ChatFrame.setVisible(false);
							clientGUI.DupErrFrame.setVisible(true);
                        }
						
						if (input.contains(ServerCodes.CODE_LIST_USERNAMES)) {
							String UserList = input.substring(5, input.length());
							clientGUI.UserList.setText("Users:\n");
							clientGUI.UserList.append(UserList);
						}

						if (input.contains(ServerCodes.CODE_BROADCAST)) {
							String UserMessage = input.substring(11, input.length());
							clientGUI.Chat.append(UserMessage + "\n");
						}

						if (input.contains(ServerCodes.CODE_WHISPER)) {
							String WhisperMessage = input.substring(0, input.length());
							clientGUI.Chat.append(WhisperMessage + "\n");
						}

                        System.out.println(input);
                    }
                }
            }
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Done");
        clientGUI.ChatFrame.setVisible(false);
		clientGUI.DisconnectFrame.setVisible(true);
    }

    /**
     * Method gets the output ready for printing.
     * 
     * @param out Output that needs to be displayed.
     */
    public synchronized static void addOutput(String out) {
        output.add(out);
    }

    /**
     * Method sets username.
     * 
     * @param name Name that needs to be set.
     */
    public synchronized static void addUsername(String name) {
        username = name;

    }

    /**
     * Method checks if a username has been given.
     */
    public synchronized boolean needUsername() {
        return username.equals("");
    }

    /**
     * Broadcasts [message] to all clients
     * 
     * @param message Message string that is sent to all clients.
     */
    synchronized static void broadcastMessage(String message) {
        output.add(message);
    }

    /**
     * Kill current client
     * 
     * @param message Message string that is sent to all clients.
     */
    synchronized static void killClient() {
        running = false;
    }
}
