import java.net.*;

public class User
{
    private Socket socket;
    private String username;

    public User(Socket socket,
                String username)
    {
        this.socket = socket;
        this.username = username;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public String getName()
    {
        return username;
    }
}