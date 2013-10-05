
// WormChase.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* A worm moves around the screen and the user must
   click (press) on its head to complete the game.

   If the user misses the worm's head then a blue box
   will be added to the screen (if the worm's body was
   not clicked upon).

   A worm cannot move over a box, so the added obstacles
   *may* make it easier to catch the worm.

   A worm starts at 0 length and increases to a maximum
   length which it keeps from then on.

   A score is displayed on screen at the end, calculated
   from the number of boxes used and the time taken. Less
   boxes and less time mean a higher score.

   -------------
   
   The display includes two textfields for showing the current time
   and number of boxes. The average FPS/UPS values are drawn in
   the game's JPanel.

   Pausing/Resuming/Quiting are controlled via the frame's window
   listener methods.

   Uses active rendering to update the JPanel.

   Using Java's timer: System.currentTimeMillis()
     *  millisecs for the period

   Average FPS / UPS
					20			50			80			100
   Win 98 (work):  19/20       43/50       54/83       57/100
   Win 98 (home):  19/20       39/49       49/82       52/98
   Win 2000:       20/20       50/50       57/83       58/100  // slow machine
   Win XP:         20/20       50/50       83/83      100/100
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class RPS extends JFrame implements WindowListener
{
  private static int DEFAULT_FPS = 80;

  private RPSPanel rpsp;        // where the worm is drawn


  public RPS(int period)
  { 
	super("Rock Paper Scissors");
    makeGUI(period);

    addWindowListener( this );
    pack();
    setResizable(false);
    setVisible(true);
  }  // end of WormChase() constructor


  private void makeGUI(int period)
  {
    rpsp = new RPSPanel(this, period);
    add(rpsp, "Center");
  }  // end of makeGUI()
  

  // ----------------- window listener methods -------------

  public void windowActivated(WindowEvent e) 
  { rpsp.resumeGame();  }

  public void windowDeactivated(WindowEvent e) 
  {  rpsp.pauseGame();  }


  public void windowDeiconified(WindowEvent e) 
  {  rpsp.resumeGame();  }

  public void windowIconified(WindowEvent e) 
  {  rpsp.pauseGame(); }


  public void windowClosing(WindowEvent e)
  {  rpsp.stopGame();  }

  public void windowClosed(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}

  // ----------------------------------------------------

  public static void main(String args[])
  { 
    int fps = DEFAULT_FPS;
    if (args.length != 0)
      fps = Integer.parseInt(args[0]);

    int period = (int) 1000.0/fps;
    System.out.println("fps: " + fps + "; period: " + period + " ms");

    new RPS(period);    // ms
  }

} // end of WormChase class


