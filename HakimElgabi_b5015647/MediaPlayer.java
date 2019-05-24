import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.*;
import java.net.URI;

public class MediaPlayer extends JFrame
        implements  ControllerListener
{
    private File file;
    private Player player;

    public static void main(String filename)
    {
        MediaPlayer frame = new MediaPlayer(filename);

        frame.setTitle( "Media Player" );
        frame.setSize(600, 400);
        frame.setVisible(true);
   }

   public MediaPlayer (String filename)
   {
	   try
	   {
	   		createPlayer(filename);
   		}
   		catch (Exception e)
							{
							 	e.printStackTrace();
					}
	}
   private void createPlayer(String filename) throws Exception
   {
		file = new File(filename);
        URI uri = file.toURI();

        player = Manager.createPlayer(uri.toURL());
        player.addControllerListener(this);
        player.start();
    }

    public void controllerUpdate(ControllerEvent e)
    {
        Container pane = getContentPane(); //Note!!!

        if (e instanceof RealizeCompleteEvent)
        {
            Component visualComponent =
                        player.getVisualComponent();

            if (visualComponent != null)
                pane.add(visualComponent, BorderLayout.CENTER);

            Component controlsComponent =
                player.getControlPanelComponent();

            if (controlsComponent != null)
                pane.add(controlsComponent, BorderLayout.SOUTH);

            pane.doLayout();
        }
    }
}

