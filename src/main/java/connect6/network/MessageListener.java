package connect6.network;

public interface MessageListener {
    void onMessageReceived(String message);
    void onConnectionStatusChanged(boolean connected);
}