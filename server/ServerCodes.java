package server;

/**
 * Fixed codes that are shared by both server and client Used to have consistency between server and
 * client in case message format changes
 */
public class ServerCodes {
    // Add name to client
    // Same format when sent to server
    // FORMAT: /name:[username]
    public static String CODE_NAME = "/name:";
    public static int CODE_NAME_LENGTH = 6;

    // Whisper command used to send a message to a spesific user
    // FORMAT: Send /whisper:[receiver_username];;[message]
    // FORMAT: Received /whisper:[sender_username];;[message]
    public static String CODE_WHISPER = "/whisper:";
    public static int CODE_WHISPER_LENGTH = 9;

    // Command to receive list of connected clients
    public static String CODE_LIST_USERNAMES = "/list";

    // Sent from server to client if the server kills client
    // Sent from client to server when client is done
    public static String CODE_STOP_CLIENT = "/stop";

    // Broadcast command to send message to all clients
    // FORMAT: /broadcast:[message]
    public static String CODE_BROADCAST = "/broadcast:";

    // Break used for between important information. See: /whisper
    public static String BREAK = ";;";

    // Codes returned from server upon successfull command
    public static String CODE_SUCCESSFULLY_ADDED_USER = "/SUCC_user";
    public static String CODE_SUCCESSFULLY_WHISPERED = "/SUCC_whisper";

    // Codes returned from server upon failed command
    public static String ERR_DUPLICATE_USER = "/ERR_Duplicate";
    public static String ERR_INVALID_INPUT = "/ERR_Invalid_Input";
    public static String ERR_USER_DOES_NOT_EXIST = "/ERR_User_Not_Exist";
}
