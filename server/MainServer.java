package server;

import java.net.*;
import java.util.*;

import java.io.*;
import java.nio.channels.*;

/**
 * Main Class for the entire server Responsible for Getting input from the terminal, Starting the
 * ServerListener thread, Rerouting input from a ServerChannelThread thread to the ServerListener
 * thread, Properly closing each thread when program ends.
 */
public class MainServer {
    static ServerListener listen;

    /**
     * Main thread for the MainServer class
     * 
     * @param args Input from when program starts. Used to get the port number the server will run
     *        on.
     */
    public static void main(String[] args) {
        Thread currentThread = Thread.currentThread();
        currentThread.setName("_MainServer");
        System.out.println("Type /help for more commands");

        if (args.length < 1) {
            System.err.println("No port given. FORMAT: java server/MainServer port");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        listen = new ServerListener(portNumber);
        listen.start();

        // Reads input from terminal
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String commands;
            while ((commands = reader.readLine()) != null) {
                if (commands.equals("/stop")) {
                    listen.stopRunning();
                    break;
                }

                if (commands.equals("/kill")) {
                    listen.killAllClients();
                }

                if (commands.equals("/print")) {
                    printRunningThreads();
                }

                if (commands.contains("/broadcast:")) {
                    
					int messageLength = ServerCodes.CODE_BROADCAST.length();
                    String message = commands.substring(messageLength);
                    listen.broadcastMessage(ServerCodes.CODE_BROADCAST + "SERVER" + ServerCodes.BREAK + message);

                }

                if (commands.contains("/list")) {
                    listen.printUsernameList();
                }

                if (commands.contains("/help")) {
                    System.out.println("Ignore [] when typing commands.");
                    System.out.println("/stop                    Stops server");
                    System.out.println(
                            "/broadcast:[message]     Broadcasts [message] to all clients");
                    System.out.println(
                            "/list                    Shows list of currently connected users");
                    System.out.println(
                            "/kill                    Kills all currently connected users");
                    System.out
                            .println("/print                   Prints list of all marked threads");

                    System.out.println("/help                    Prints out list of commands");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Thread MainServer ended");
    }

    /**
     * Code given by ChatGPT, modified. https://chat.openai.com/chat
     * 
     * Used to print out spesific threads that are currently running. Only used for
     * testing/experiments.
     */
    static void printRunningThreads() {
        // Get the root thread group
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup().getParent();

        // Create a buffer to hold the list of threads
        Thread[] threads = new Thread[rootGroup.activeCount()];

        // Fill the buffer with the list of threads
        int count = rootGroup.enumerate(threads, true);

        // Print out the list of threads
        System.out.println("List of running threads:");
        for (int i = 0; i < count; i++) {
            String name = threads[i].getName();
            // Only prints out designated threads
            if (name.contains("_")) {
                System.out.println(name);
            }
        }
    }

    /**
     * Rerouts input from a unique ServerChannelThread thread to ServerListener.
     * {@link ServerListener#addUsername(ServerChannelThread thread, String name)}
     * 
     * @param thread The ServerChannelThread that sent the command. Used for identification and
     *        returning a responce.
     * @param name Username that will be used to identify the client.
     */
    static void addUsername(ServerChannelThread thread, String name) {
        listen.addUsername(thread, name);
    }

    /**
     * Rerouts input from a unique ServerChannelThread thread to ServerListener,
     * {@link ServerListener#removeClient(ServerChannelThread thread)}
     * 
     * @param thread The ServerChannelThread that sent the command. Used for identification and
     *        returning a responce.
     */
    static void removeClient(ServerChannelThread thread) {
        listen.removeClient(thread);
    }

    /**
     * Rerouts input from a unique ServerChannelThread thread to ServerListener,
     * {@link ServerListener#whisperClient(String sender, String user, String message)}
     * 
     * @param sender Username of the client that sent the whisper
     * @param user Username of the client that needs to receive the whisper
     * @param message Message string that was sent for user from sender
     */
    static void whisperClient(String sender, String user, String message) {
        listen.whisperClient(sender, user, message);
    }

    /**
     * Rerouts input from a unique ServerChannelThread thread to ServerListener,
     * {@link ServerListener#getUsernameList(ServerChannelThread thread)}
     * 
     * @param thread The ServerChannelThread that sent the command. Used for identification and
     *        returning a responce.
     */
    static void getUsernameList(ServerChannelThread thread) {
        listen.getUsernameList(thread);
    }

    /**
     * Rerouts input from a unique ServerChannelThread thread to ServerListener,
     * {@link ServerListener#broadcastMessage(ServerChannelThread thread)}
     * 
     * @param message Message string that is broadcasted to all clients
     */
    static void broadcastMessage(String message) {
        listen.broadcastMessage(message);
    }

    /**
     * Listens for client connections on [portNumber].
     * Manages functions and commands relevant to communication between clients.
     * 
     * Used basic code from: https://jenkov.com/tutorials/java-nio/server-socket-channel.html
     * https://jenkov.com/tutorials/java-nio/non-blocking-server.html
     */
    static class ServerListener extends Thread {
        List<ClientThread> clientThreads = new ArrayList<ClientThread>();
        List<String> existingUsernames = new ArrayList<String>();
        int portNumber;
        boolean running = true;

        /**
         * Creates a new instance of ServerListener with a unique port number
         * 
         * @param port Port number that ServerListener listens for client connections
         */
        ServerListener(int port) {
            portNumber = port;
        }

        /**
         * Main loop for the ServerListener thread.
         */
        @Override
        public void run() {
            Thread currentThread = Thread.currentThread();
            currentThread.setName("_ServerListener");
            // No functional use. Purely to obtain server ip address used by clients.
            try {
                System.out.println(InetAddress.getLocalHost());
            } catch (UnknownHostException e) {
                System.out.println("Could not find host address");
            }

            // Opens a ServerSocketChannel that listens for client connections on [port]
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                serverSocketChannel.socket().bind(new InetSocketAddress(portNumber));
                serverSocketChannel.configureBlocking(false);
                while (running) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // Creates a ServerChannelThread thread for a client when a client connects
                    if (socketChannel != null) {
                        System.out.println("Client Connected");

                        ServerChannelThread serverChannelThread =
                                new ServerChannelThread(socketChannel);
                        serverChannelThread.start();

                        clientThreads.add(new ClientThread(serverChannelThread));
                    }
                }
                serverSocketChannel.close();
                System.out.println("Server socket properly closed:" + !serverSocketChannel.isOpen());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Thread ServerListener ended");
        }

        /**
         * Used to: Call a funtion that properly kills client threads and stopping this thread's
         * while loop {@link ServerListener#killAllClients()} {@link ServerListener#run()}
         */
        synchronized void stopRunning() {
            killAllClients();
            running = false;
        }

        /**
         * Broadcasts a list of users to all clients.
         * 
         * It does this by first creating a list of usernames of currently connected clients, then
         * sending that list to every client
         * 
         * Used mostly when a client connects/disconnects so that all clients lists are up to date.
         */
        synchronized void broadcastUsernameList() {
            List<String> clientUsernames = new ArrayList<String>();
            for (ClientThread clientThread : clientThreads) {
                clientUsernames.add(clientThread.getUsername());
            }

            for (ClientThread clientThread : clientThreads) {
                clientThread
                        .addOutput(ServerCodes.CODE_LIST_USERNAMES + clientUsernames.toString());
            }
        }

        /**
         * Sends List of all users to a spesific user that requested it
         * 
         * It does this by first creating a list of usernames of currently connected clients, then
         * sending the list only to the spesific client that requested it.
         * 
         * Used mostly only if a client requests a list update
         * 
         * @param thread The ServerChannelThread that sent the command. Used for returning the list
         *        to the correct client.
         */
        synchronized void getUsernameList(ServerChannelThread thread) {
            List<String> clientUsernames = new ArrayList<String>();
            for (ClientThread clientThread : clientThreads) {
                clientUsernames.add(clientThread.getUsername());
            }

            thread.addOutput(ServerCodes.CODE_LIST_USERNAMES + clientUsernames.toString());
        }

        /**
         * Prints list of connected users to server terminal Only used by the server for
         * testing/experiments.
         */
        synchronized void printUsernameList() {
            for (ClientThread clientThread : clientThreads) {
                System.out.println(clientThread.getUsername());
            }
        }

        /**
         * Broadcasts [message] to all clients
         * 
         * @param message Message string that is sent to all clients.
         */
        synchronized void broadcastMessage(String message) {
            for (ClientThread clientThread : clientThreads) {
                clientThread.addOutput(message);
            }
        }

        /**
         * Adds ServerChannelThread and username to server cataloge if its not a duplicate.
         * 
         * First the program checks if the username is unique.
         * 
         * If it is not, then an error code {@link ServerCodes#ERR_DUPLICATE_USER} is sent back to
         * the ServerChannelThread that made the request.
         * 
         * Otherwise it adds the username to its corresponding ClientThread {@link ClientThread}
         * returns a success code back to the ServerChannelThread that made the request
         * {@link ServerCodes#CODE_SUCCESSFULLY_ADDED_USER} and broadcasts the newly updated list to
         * all clients {@link ServerListener#broadcastUsernameList()}
         * 
         * @param thread The ServerChannelThread that sent the command. Used for identification and
         *        returning a responce.
         * @param name Username that needs to be verified. If successfull, is made the username of
         *        the client.
         */
        synchronized void addUsername(ServerChannelThread thread, String name) {
            if (!existingUsernames.contains(name)) {
                for (ClientThread clientThread : clientThreads) {
                    if (clientThread.serverChannelThread.equals(thread)) {
                        existingUsernames.add(name);
                        clientThread.addUsername(name);

                        thread.addOutput(ServerCodes.CODE_SUCCESSFULLY_ADDED_USER);
                        broadcastUsernameList();
                    }
                }
            } else {
                System.out.println(ServerCodes.ERR_DUPLICATE_USER);
                thread.addOutput(ServerCodes.ERR_DUPLICATE_USER);
            }
        }

        /**
         * Removes client from server list. Broadcasts the newly updated list to all clients
         * {@link ServerListener#broadcastUsernameList()} Happens usualy when client disconnects.
         * 
         * @param thread The ServerChannelThread that sent the command. Used for identification.
         */
        synchronized void removeClient(ServerChannelThread thread) {
            for (ClientThread clientThread : clientThreads) {
                if (clientThread.serverChannelThread.equals(thread)) {
                    existingUsernames.remove(clientThread.getUsername());
                    clientThreads.remove(clientThread);
                    broadcastUsernameList();
                    break;
                }
            }
        }

        /**
         * Whispers a message to a spesific client Firsts identifies the user to which the message
         * needs to be sent If the user exists, it sends the message with the client sender to the
         * client. If the user does not exist, it returns an error message to the sender client.
         * 
         * @param sender Username of the client that sent the message
         * @param user Username of the client that should receive the message
         * @param message Message string that should be sent to the client
         */
        synchronized void whisperClient(String sender, String user, String message) {
            boolean userExists = false;
            for (ClientThread clientThread : clientThreads) {
                if (clientThread.getUsername().equals(user)) {
                    clientThread.addOutput(ServerCodes.CODE_WHISPER + sender + ";;" + message);
                    userExists = true;
                }
            }

            for (ClientThread clientThread : clientThreads) {
                if (clientThread.getUsername().contains(sender)) {
                    if (userExists) {
                        clientThread.addOutput(ServerCodes.CODE_WHISPER + sender + ";;" + message);
                    } else {
                        clientThread.addOutput(ServerCodes.ERR_USER_DOES_NOT_EXIST);
                    }
                }
            }
        }

        /**
         * Kills all client threads It does this by sending a command to all ServerChannelThreads,
         * which then ends the thread there
         */
        synchronized void killAllClients() {
            for (ClientThread clientThread : clientThreads) {
                clientThread.serverChannelThread.addOutput(ServerCodes.CODE_STOP_CLIENT);
            }
        }
    }

    /**
     * Client object that holds the client's ServerChannelThread thread and username Usefull for
     * keeping both username and threads connected at the same place
     */
    static private class ClientThread {
        String clientUsername = "";
        ServerChannelThread serverChannelThread;

        ClientThread(ServerChannelThread thread) {
            serverChannelThread = thread;
        }

        /**
         * Adds username to the ClientThread
         * 
         * @param name New Username for the client
         */
        public void addUsername(String name) {
            clientUsername = name;
            System.out.println("Added user: " + name);
        }

        /**
         * Returns the client's username
         * 
         * @return Username of the client
         */
        public String getUsername() {
            return clientUsername;
        }

        /**
         * Sends command to ServerChannelThread
         * 
         * @param message command that is sent
         */
        public void addOutput(String message) {
            serverChannelThread.addOutput(message);
        }
    }
}


