import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;
import javax.media.*;
import java.net.URL;

public class Client extends JFrame
                    implements ActionListener
{
    private static Connection link;
	private JPanel loginPanel, signUpPanel, inputPanel, chatPanel, buttonPanel;
	private static JButton signupButton, loginButton, quitButton,
	privateSubmitButton, submitButton, audioButton, imageButton,
	videoButton, startPrivateButton;
	private static JTextField userField, passField, addName, addPassword, privateMessageField;
	private static JTextArea chatArea, userArea, inputArea;
	private static JScrollPane inputScroll;
	private static boolean end = false;
	private static String username;
	private static AudioClip clip;

    private static InetAddress host = null;
    private final static int PORT = 1234;
    private static Socket socket;
    private static Scanner networkInput;
    private static ObjectInputStream inStream;
    private static PrintWriter output;
	//public static JFrame MediaPlayer = new JFrame();
	public static JFrame PrivateMessage = new JFrame();
    private static File file;
    private static Player player;


	public static void main(String[] args)
								throws IOException
	{
	    try
	    {
	        link = DriverManager.getConnection(
	                "jdbc:mysql://homepages.shu.ac.uk:3306/b5015647_db3",
	                                                "b5015647","Good3438");
	    }
	    catch(SQLException e)
	    {
	        System.out.println("* Cannot connect to database! *");
	        System.exit(1);
	    }

        chatArea = new JTextArea(25,25);
        chatArea.setSize(25, 25);
        userArea = new JTextArea(25,15);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        userArea.setEditable(false);
        userArea.setLineWrap(true);
        userArea.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane (chatArea,
        		   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane listScroll = new JScrollPane (userArea,
     		   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
     		   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        chatScroll.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Chat Log"),
					BorderFactory.createEmptyBorder(5,5,5,5)),
					chatScroll.getBorder()));

        listScroll.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Current Users"),
					BorderFactory.createEmptyBorder(5,5,5,5)),
					listScroll.getBorder()));

        Client frame = new Client(chatScroll, listScroll);

        frame.setTitle("Group Chat");
        frame.setSize(600,600);
        frame.setLocation(250,100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        try
        {
            host = InetAddress.getLocalHost();
        }
        catch(UnknownHostException uhEx)
        {
            System.out.println("\nHost ID not found!\n");
        }

        socket = new Socket(host, PORT);
        networkInput = new Scanner(socket.getInputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
        output = new PrintWriter(
        			socket.getOutputStream(),true);

        String response;
        while (!end)
        {
        	while (networkInput.hasNextLine())
        	{
        		response = networkInput.nextLine();
        		if (response.equals("updateUserList"))
        		{
        			userArea.setText("");
        		}
        		else if (response.equals("MediaNotFound"))
        		{
    	            JOptionPane.showMessageDialog(
    	            		null, "MEDIA NOT FOUND", "ERROR!",
    	            		JOptionPane.ERROR_MESSAGE);
        		}
        		else if (response.contains(":") ||
        		        response.contains("has connected to the chat") ||
        		        response.contains("has left the chat"))
        		{
        			chatArea.append(response + "\n");
        		}
        		else if (response.equals("imageSent"))
				{
		    		try
					{
						getFile(response);
					}
					catch (ClassNotFoundException e)
					{
						e.printStackTrace();
					}
				}
        		else if (response.equals("audioSent"))
        		{
					try
					{
						getFile(response);
					}
					catch (ClassNotFoundException e)
					{
						e.printStackTrace();
					}
        		}
        		else if (response.equals("videoSent"))
        		{
					try
					{
						getFile(response);
					}
					catch (ClassNotFoundException e)
					{
						e.printStackTrace();
					}
        		}
        		else if (response.equals("showImage"))
				{
				    ImageIcon image = new ImageIcon("image.jpg");
					 JOptionPane.showMessageDialog(null,
	    			"", "Sent to " + username, JOptionPane.INFORMATION_MESSAGE, image);
        		}
        		else if (response.equals("playAudio"))
				{
        			try
        			{
        				clip = Applet.newAudioClip(
        					new URL("file:audio.au"));
        			}
        			catch(MalformedURLException muEx)
        			{
        				System.out.println("*** Invalid URL! ***");
        				System.exit(1);
        			}
        			clip.play();
				}
        		else if (response.equals("playVideo"))
				{
					MediaPlayer.main("video.mpeg");
				}
        		else if (response.equals("PRIVATE"))
        		{
        			BuildPrivateMessage();
        		}
        		else if (response.equals("!EXIST"))
        		{
        			 JOptionPane.showMessageDialog(
     	            		null, "USER NOT FOUND", "ERROR!",
     	            		JOptionPane.ERROR_MESSAGE);
        		}
        		else
        		{
        			userArea.append(response + "\n");
        		}
        	}
        }

	}

	public Client (JScrollPane chatScroll, JScrollPane listScroll)
	{
        loginPanel = new JPanel();
        signUpPanel = new JPanel();
        chatPanel = new JPanel();
        inputPanel = new JPanel();
        buttonPanel = new JPanel();

        loginButton = new JButton("Login");
        userField = new JTextField(20);
        userField.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Input Username"),
					BorderFactory.createEmptyBorder(5,5,5,5)),
					userField.getBorder()));

        passField = new JPasswordField(20);
        passField.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Input Password"),
					BorderFactory.createEmptyBorder(5,5,5,5)),
					passField.getBorder()));

        signupButton = new JButton("Sign Up");
        addName = new JTextField(20);
        addName.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Set Your Username"),
					BorderFactory.createEmptyBorder(5,5,5,5)),
					addName.getBorder()));

        addPassword = new JPasswordField(20);
        addPassword.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Set Your Password"),
					BorderFactory.createEmptyBorder(5,5,5,5)),
					addPassword.getBorder()));

	  	privateMessageField = new JTextField(20);
        inputArea = new JTextArea(2,30);
        inputScroll = new JScrollPane (inputArea,
     		   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
     		   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        submitButton = new JButton("Submit");
        privateSubmitButton = new JButton("Submit");
        quitButton = new  JButton("Quit");
        quitButton.setBackground(Color.red);
        quitButton.setForeground(Color.WHITE);

        imageButton = new JButton("Image");
		audioButton = new JButton("Audio");
        videoButton = new JButton("Video");
        startPrivateButton = new JButton("Send Private Message");


        loginButton.addActionListener(this);
        signupButton.addActionListener(this);

        quitButton.addActionListener(this);
        submitButton.addActionListener(this);
        privateSubmitButton.addActionListener(this);

        imageButton.addActionListener(this);
		audioButton.addActionListener(this);
        videoButton.addActionListener(this);
        startPrivateButton.addActionListener(this);

        loginPanel.add(userField);
        loginPanel.add(passField);
        loginPanel.add(loginButton);

        signUpPanel.add(addName);
        signUpPanel.add(addPassword);
        signUpPanel.add(signupButton);

        chatPanel.add(chatScroll);
        chatPanel.add(listScroll);

        inputPanel.add(inputScroll);
        inputPanel.add(submitButton);
        inputPanel.add(quitButton);

        buttonPanel.add(imageButton);
		buttonPanel.add(audioButton);
        buttonPanel.add(videoButton);
        buttonPanel.add(startPrivateButton);
        buttonPanel.setLayout(new FlowLayout());

        add(loginPanel, BorderLayout.NORTH);
        add(signUpPanel, BorderLayout.CENTER);
	}

    public void actionPerformed(ActionEvent e)
    {
		String message;
    	if(e.getSource() == submitButton)
    	{
    		if (!inputArea.getText().isEmpty())
    		{
    	    	message = inputArea.getText();
    			output.println(message);
    			inputArea.setText("");
    		}
    		else if (inputArea.getText().equals("QUIT"))
    		{
    		    end = true;
    		    output.println("QUIT");
    		    System.exit(1);
    		}
    		else
    			 JOptionPane.showMessageDialog(
 	            		null, "Message cannot be empty",
 	            		"MESSAGE REQUIRED!",
 	            		JOptionPane.ERROR_MESSAGE);
    	}
    	if(e.getSource() == privateSubmitButton)
    	{
    		if (!privateMessageField.getText().isEmpty())
    		{
    	    	message = privateMessageField.getText();
    			output.println(message);
    			privateMessageField.setText("");
    			PrivateMessage.setVisible(false);
    		}
    		else
    			 JOptionPane.showMessageDialog(
 	            		null, "Message cannot be empty",
 	            		"MESSAGE REQUIRED!",
 	            		JOptionPane.ERROR_MESSAGE);
    	}

    	if (e.getSource() == imageButton)
			output.println("sendimage");

		if (e.getSource() == audioButton)
			output.println("sendaudio");

		if (e.getSource() == videoButton)
			output.println("sendvideo");

		if (e.getSource() == startPrivateButton)
		{
			inputArea.setText("PRIVATE:");
    		JOptionPane.showMessageDialog(
				null, "Now Enter the name of the user!",
				"PRIVATE MESSAGE!",
				JOptionPane.INFORMATION_MESSAGE);
		}

    	if (e.getSource() == loginButton)
    	{
            username = userField.getText();
    	    String password = passField.getText();
            boolean login = false;
            Statement statement = null;
            ResultSet results = null;
            try
            {
                statement = link.createStatement();
                results = statement.executeQuery(
                        "SELECT * FROM Users where userName = '" + username
                            + "' AND password = '" + password +"'");
                if (results.next())
                    login = true;
                if (login){
                    output.println(username);
                    inputScroll.setBorder(
						BorderFactory.createCompoundBorder(
							BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(username + ":"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
								inputScroll.getBorder()));

                    loginPanel.setVisible(false);
                    signUpPanel.setVisible(false);
                    userField.setText("");

                    add(chatPanel, BorderLayout.NORTH);
                    add(inputPanel, BorderLayout.CENTER);
                    add(buttonPanel, BorderLayout.SOUTH);

                    chatPanel.setVisible(true);
                    inputPanel.setVisible(true);
                    buttonPanel.setVisible(true);
                }
                else if (!login)
                {
                    JOptionPane.showMessageDialog(
                            null, "Incorrect Login Details",
                            "LOGIN ERROR!",
                            JOptionPane.ERROR_MESSAGE);
                    passField.setText("");
                }
            }
            catch(SQLException e1)
            {
                System.out.println("* Cannot execute query! *");
                e1.printStackTrace();
                System.exit(1);
            }
    	}
    	if (e.getSource() == quitButton)
    	{
    	    end = true;
    	    output.println("QUIT");
            try
            {
                System.out.println("\nClosing down connection...\n");
                socket.close();
            }
            catch(IOException ioEx)
            {
                System.out.println("\n* Disconnection problem! *\n");
            }
            System.exit(0);
    	}
        if (e.getSource() == signupButton)
        {
            boolean validInsertion = true;
            String addUsername = addName.getText();
            String addPass = addPassword.getText();
            try
            {
                Statement statement = null;
                ResultSet results = null;
                statement = link.createStatement();
                results = statement.executeQuery(
                        "SELECT * FROM Users where userName = '"
                        + addUsername + "' ");
                if (results.next())
                    validInsertion = false;

                if (validInsertion)
                {
                    String insert = "INSERT INTO Users"
                            + " VALUES('"
                            + addUsername + "','"
                            + addPass + "')";
                    statement.executeUpdate(insert);

                    JOptionPane.showMessageDialog(
                            null,
                            "You have succesfully signed up for an account",
                            "SIGNUP",
                            JOptionPane.INFORMATION_MESSAGE);

                    addName.setText("");
                    addPassword.setText("");
                }
                else if (!validInsertion)
                {
                    JOptionPane.showMessageDialog(
                            null, "That username is taken","SIGNUP ERROR!",
                            JOptionPane.ERROR_MESSAGE);

                    addName.setText("");
                    addPassword.setText("");
                }
            }
            catch(SQLException e1)
            {
                System.out.println("* Cannot execute query! *");
                e1.printStackTrace();
                System.exit(1);
            }
        }

    }
	private static void getFile(String response)
						throws IOException, ClassNotFoundException
	{
		byte[] byteArray = (byte[]) inStream.readObject();

		FileOutputStream mediaStream;
		if (response.equals("imageSent"))
			mediaStream = new FileOutputStream("image.jpg");
		else if(response.equals("audioSent"))
			mediaStream = new FileOutputStream("audio.au");
		else
			mediaStream = new FileOutputStream("video.mpeg");

		mediaStream.write(byteArray);
		mediaStream.close();

		if (response.equals("imageSent"))
        	output.println("displayImage");
		else if(response.equals("audioSent"))
            output.println("playAudio");
		else
            output.println("playVideo");
    }

    public static void BuildPrivateMessage()
    {
        PrivateMessage.setTitle("Send Private Message");
        PrivateMessage.setSize(300, 100);
        PrivateMessage.setLocation(250, 200);
        PrivateMessage.setVisible(true);
        PrivateMessage.setLayout(new FlowLayout());
        PrivateMessage.add(privateMessageField);
        PrivateMessage.add(privateSubmitButton);
    }
}