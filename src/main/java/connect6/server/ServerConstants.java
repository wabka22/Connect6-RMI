package connect6.server;

public class ServerConstants {
    public static final int PORT = 8888;

    // Команды протокола
    public static final String CMD_WELCOME = "WELCOME";
    public static final String CMD_ROLE = "ROLE";
    public static final String CMD_GAME_START = "GAME_START";
    public static final String CMD_TURN = "TURN";
    public static final String CMD_BOARD = "BOARD";
    public static final String CMD_GAME_OVER = "GAME_OVER";
    public static final String CMD_ERROR = "ERROR";
    public static final String CMD_MOVE = "MOVE";
    public static final String CMD_EXIT = "EXIT";

    private ServerConstants() {
        // Константный класс
    }
}