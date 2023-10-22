package server;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Thread that communicates with the client over a channel
 * 
 * There is one main loop where the thread waits for either Output from the server or Input from the
 * Client.
 * 
 * Output: Executes oldest command in {@link ServerChannelThread#output} Sends the output as is to
 * the client.
 * 
 * Input: Gets input from the client. Checks if command is valid, then executes accordingly. Sends
 * command to {@link MainServer} if its a valid command. Sends an error code to the client if it is
 * not a valid command.
 * 
 * Basic input and output obtained from https://jenkov.com/tutorials/java-nio/socketchannel.html
 */
public class ServerChannelThread extends Thread {
    SocketChannel clientChannel;
    // Output commands are put into a list to avoid any commands getting lost
    List<String> output = new ArrayList<String>();
    boolean running = true;

    boolean hasName = false;
    String username = "";

    ServerChannelThread(SocketChannel channel) {
        clientChannel = channel;
    }

    @Override
    public void run() {
        try {
            clientChannel.configureBlocking(false);
            while (!clientChannel.finishConnect()) {
                System.out.println("Waiting");
            }

            while (running) {
                if (output.size() > 0) {
                    // Output
                    System.out.println("Output: " + username + " : " + output.get(0));

                    if (output.get(0).contains(ServerCodes.ERR_DUPLICATE_USER)) {
                        username = "";
                    }

                    if (output.get(0).equals(ServerCodes.CODE_STOP_CLIENT)) {
                        stopRunning();
                    }

                    ByteBuffer buf = ByteBuffer.allocate(output.get(0).length() + 1);
                    buf.clear();

                    buf.put(output.get(0).getBytes());
                    buf.flip();

                    while (buf.hasRemaining()) {
                        clientChannel.write(buf);
                    }
                    output.remove(0);
                } else {
                    // Input
                    ByteBuffer buf = ByteBuffer.allocate(100);
                    int bytesRead = clientChannel.read(buf);

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
                        System.out.println(input);

                        // Removes empty bytes
                        String final_input = "";
                        for (int i = 0; i < 100; i++) {
                            if (input.getBytes()[i] != 0) {
                                final_input = final_input + input.charAt(i);
                            }
                        }
                        input = final_input;

                        if (!hasName) {
                            // Commands available before username is given
                            if (input.contains(ServerCodes.CODE_NAME)) {
                                String name = input.substring(ServerCodes.CODE_NAME_LENGTH);
                                MainServer.addUsername(this, name.strip());
                                username = name;

                                Thread currentThread = Thread.currentThread();
                                currentThread.setName("_client_" + username);

                                hasName = true;
                            } else {
                                System.out.println(ServerCodes.ERR_INVALID_INPUT);
                                output.add(ServerCodes.ERR_INVALID_INPUT);
                            }
                        } else {
                            // Commands available after username is given
                            if (input.contains(ServerCodes.CODE_WHISPER)) {
                                String name = input.substring(ServerCodes.CODE_WHISPER_LENGTH,
                                        input.indexOf(ServerCodes.BREAK));
                                String message = input.substring(input.indexOf(ServerCodes.BREAK)
                                        + ServerCodes.BREAK.length());

                                MainServer.whisperClient(username, name, message);
                            }

                            if (input.contains(ServerCodes.CODE_LIST_USERNAMES)) {
                                MainServer.getUsernameList(this);
                            }

                            if (input.contains(ServerCodes.CODE_STOP_CLIENT)) {
                                stopRunning();
                            }

                            if (input.contains(ServerCodes.CODE_BROADCAST)) {
                            int messageLength = ServerCodes.CODE_BROADCAST.length();
                                String message = input.substring(messageLength);
                                MainServer.broadcastMessage(ServerCodes.CODE_BROADCAST + username + ServerCodes.BREAK + message);
							}
                        }
                    }
                }
            }
            clientChannel.close();
            System.out.println(
                    "Client [" + username + "] socket properly closed:" + !clientChannel.isOpen());
        } catch (IOException e) {
            System.out.println("Client [" + username + "] disconnected");
        }
        System.out.println("Thread [" + username + "] ended");
        MainServer.removeClient(this);
    }

    /**
     * Adds the command to the Output list {@link ServerChannelThread#output}
     * 
     * @param out command to add to the Output list
     */
    public synchronized void addOutput(String out) {
        output.add(out);
    }

    /**
     * Stops this thread while loop found in {@link ServerChannelThread#run()}
     */
    synchronized void stopRunning() {
        running = false;
    }
}
