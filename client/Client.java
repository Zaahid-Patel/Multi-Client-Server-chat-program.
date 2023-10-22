package client;

import server.ServerCodes;
import client.ClientChannelThread;
import java.io.*;

/**
 * Main class for the entire client responsible for getting input from the
 * terminal, starting the ClientChannelThread and reading commands from the
 * InputStream.
 */
public class Client {

    /**
     * Main method for the Client class
     * 
     * @param args Input from when program starts. Used to get the IP address and
     *             port number the server will run
     *             on.
     */
    public static void main(String[] args) {
        // Prints help for user input
        System.out.println("Ignore [] when typing commands.\n"
                + "/name:[username]\t\t\t\tAdds [username] as this clients username\n"
                + "/whisper:[target_username];;[message]\t\tSends [message] to [target_username]'s client\n"
                + "/broadcast:[message]\t\t\t\tBroadcasts message to all users\n"
                + "/list\t\t\t\t\t\tAsks server for a list of currently connected users\n"
                + "/stop\t\t\t\t\t\tDisconnects user");


        // Gets IP address and Port Number from the command line
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Starts ClientChannelThread using IP and Port Number
        try {
            ClientChannelThread clientChannelThread = new ClientChannelThread(hostName, portNumber);
            clientChannelThread.start();
        } catch (Exception e) {
            System.out.println("Could not connect to server.");
        }


        // Searches for commands in InputStream
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String commands;
            while ((commands = reader.readLine()) != null) {
                if (commands.contains(ServerCodes.CODE_NAME)) {
                    String name = commands.substring(ServerCodes.CODE_NAME_LENGTH);
                    ClientChannelThread.addUsername(name);
                }

                if (commands.contains(ServerCodes.CODE_WHISPER)) {
                    ClientChannelThread.addOutput(commands);
                }

                if (commands.contains(ServerCodes.CODE_LIST_USERNAMES)) {
                    ClientChannelThread.addOutput(commands);
                }

                if (commands.contains(ServerCodes.CODE_BROADCAST)) {
                    ClientChannelThread.broadcastMessage(commands);
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot create an Input Stream.");
        }
    }
}