import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server
{
    public static void main(String[] args)
							throws IOException
	{
        ArrayList<User> userList = new ArrayList<User>();
        ServerSocket serverSocket = null;
		final int PORT = 1234;
		Socket client;
		ClientHandler handler;

		try
		{
			serverSocket = new ServerSocket(PORT);
		}
		catch (IOException ioEx)
		{
			System.out.println("\nUnable to set up port!");
			System.exit(1);
		}

		System.out.println("\nServer running...\n");

		do
		{
			client = serverSocket.accept();
			handler = new ClientHandler(client,userList);
			handler.start();
		}while (true);
	}
}

class ClientHandler extends Thread
{
    ArrayList<User> list = new ArrayList<User>();
	private Socket client;
	private Scanner input;
	private PrintWriter output;
	private ObjectOutputStream outStream;
	String username;
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
	private LocalDateTime now = LocalDateTime.now();

	public ClientHandler(Socket socket, ArrayList<User> userList) throws IOException
	{
		client = socket;
	    list = userList;
	    input = new Scanner(client.getInputStream());
	    outStream = new ObjectOutputStream(client.getOutputStream());

	    username = input.nextLine();
		User newUser = new User(socket, username);
		userList.add(newUser);

        System.out.println("New client " + username + " accepted at " + dtf.format(now));
        sendMessage(list, username +  " has connected to the chat at " + dtf.format(now));
        sendMessage(list, "updateUserList");
        for (User user: list)
            sendMessage(list, user.getName());
	}

	public void run()
	{
		String received;
		received = input.nextLine();

        while (!received.equals("QUIT"))
        {
             try
             {
             	if (received.equals("sendimage"))
             	{
					try
					{
						sendFile(list,"beesting.jpg");
					}
					catch (IOException e)
					{
					 	e.printStackTrace();
					}
                }
             	else if (received.equals("sendaudio"))
             	{
					try
					{
						sendFile(list, "cuckoo.au");
					}
					catch (IOException e)
					{
					 	e.printStackTrace();
					}
				}
             	else if (received.equals("sendvideo"))
             	{
					try
					{
						sendFile(list, "MoonWalk.mpeg");
					}
					catch (IOException e)
					{
					 	e.printStackTrace();
					}
				}
             	else if (received.contains("PRIVATE:"))
             	{
                    output = new PrintWriter(client.getOutputStream(),true);
                    output.println("PRIVATE");
             		String messageTail = received.substring(8);
            		received = input.nextLine();
             		sendPrivateMessage(list, messageTail, received);
             	}
				else if (received.equals("displayImage"))
					sendMessage(list, "showImage");
				else if (received.equals("playAudio"))
					sendMessage(list, "playAudio");
				else if (received.equals("playVideo"))
					sendMessage(list, "playVideo");
             	else
             		sendMessage(list, username + "(" + dtf.format(now) +  "): " + received);
             }
             catch(IOException ioEx)
             {
                 ioEx.printStackTrace();
             }
             received = input.nextLine();
        }

        list.remove(searchPerson(list, username));

		try
		{
	        sendMessage(list, "updateUserList");
	        for (User user: list)
	            sendMessage(list, user.getName());
	        sendMessage(list, username +  " has left the chat at " + dtf.format(now));
			System.out.println(username + " left the chat at " + dtf.format(now));
			client.close();
		}
		catch(IOException ioEx)
		{
			System.out.println("* Disconnection problem! *");
		}
	}

    public void sendPrivateMessage(ArrayList<User> userList,
    				String messageTail, String message) throws IOException
    {
    	boolean userExists = false;
        for (User user: list)
        {
            if (messageTail.equals(user.getName()))
            {
                userExists = true;
            }
        }
        if (userExists)
        {
            try
            {
                for (User user: list)
                {
                    if (messageTail.equals(user.getName()))
                    {
                        userExists = true;
                        output = new PrintWriter(
								user.getSocket().getOutputStream(),true);
                        output.println("[Private From " + username + "]: " + message);
                    }
                }
            }
            catch(IOException ioEx)
            {
                ioEx.printStackTrace();
            }
        }
        else
        {
            output = new PrintWriter(client.getOutputStream(),true);
            output.println("!EXIST");
        }
    }
    public void sendMessage(ArrayList<User> userList,
    								String message) throws IOException
    {
        try
        {
            for (User user: list)
            {
                output = new PrintWriter(
					user.getSocket().getOutputStream(),true);
                output.println(message);
            }
        }
        catch(IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }
    public int searchPerson(ArrayList<User> userList, String username)
    {
        int deleteNumber = 0;
        for (User user: list)
        {
           if (username == user.getName())
               deleteNumber = list.indexOf(user);
        }
        return deleteNumber;
    }
    private void sendFile(ArrayList<User> userList,
    						String fileName) throws IOException
	{
		FileInputStream fileIn = new FileInputStream(fileName);

		long fileLen =  (new File(fileName)).length();

		int intFileLen = (int)fileLen;
		byte[] byteArray = new byte[intFileLen];

		fileIn.read(byteArray);

		fileIn.close();

		if (fileName.equals("beesting.jpg"))
			output.println("imageSent");
		else if (fileName.equals("cuckoo.au"))
			output.println("audioSent");
		else if (fileName.equals("MoonWalk.mpeg"))
			output.println("videoSent");

		outStream.writeObject(byteArray);
		outStream.flush();
	}
}

