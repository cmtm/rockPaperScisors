
// WormPanel.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* The game's drawing surface. It shows:
     - the moving worm
     - the obstacles (blue boxes)
     - the current average FPS and UPS

   The System timer is used to drive the animation loop.
*/


import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;

// import com.sun.j3d.utils.timer.J3DTimer;


public class RPSPanel extends JPanel implements Runnable
{
  private static final int PWIDTH = 1000;   // size of panel
  private static final int PHEIGHT = 625; 
  
  private static String IMS_TEXT = "imgList.txt";

  // private static long MAX_STATS_INTERVAL = 1000000000L;
  private static long MAX_STATS_INTERVAL = 1000L;
    // record stats every 1 second (roughly)

  private static final int NO_DELAYS_PER_YIELD = 16;
  /* Number of frames with a delay of 0 ms before the animation thread yields
     to other running threads. */

  private static int MAX_FRAME_SKIPS = 5;   // was 2;
    // no. of frames that can be skipped in any one animation loop
    // i.e the games state is updated but not rendered

  private static int NUM_FPS = 10;
     // number of FPS values stored to get an average
  
  //Game states
  int gameState;
  private static final int INTRO_SCREEN = 0;
  private static final int WAITING_TO_START = 1;
  private static final int GAME_TIME = 2;
  
  private boolean areTwoPlayers;


  // used for gathering statistics
  private long statsInterval = 0L;    // in ms
  private long prevStatsTime;   
  private long totalElapsedTime = 0L;
  private long gameStartTime;
  private int timeSpentInGame = 0;    // in seconds

  private long frameCount = 0;
  private double fpsStore[];
  private long statsCount = 0;
  private double averageFPS = 0.0;

  private long framesSkipped = 0L;
  private long totalFramesSkipped = 0L;
  private double upsStore[];
  private double averageUPS = 0.0;


  private DecimalFormat df = new DecimalFormat("0.##");  // 2 dp
  private DecimalFormat timedf = new DecimalFormat("0.####");  // 4 dp


  private Thread animator;           // the thread that performs the animation
  private volatile boolean running = false;   // used to stop the animation thread
  private volatile boolean isPaused = false;

  private int period;                // period between drawing in _ms_


  private RPS RPSTop;
  private Hands hands;       
  private ImagesLoader imsLoader;
  
  //Images
  BufferedImage iconsLeft, iconsRight, titleText, playerSelect, rockHand, paperHand, scissorsHand, Background1; 
  HashMap<String, BufferedImage> gestures;
 

  //Timer(s)
  private long timeAtWTS;

  // used at game termination
  private volatile boolean gameOver = false;
  private Font font;
  private FontMetrics metrics;

  // off screen rendering
  private Graphics dbg; 
  private Image dbImage = null;
  
  private String msg;



  public RPSPanel(RPS wc, int period)
  {
    RPSTop = wc;
    this.period = period;

    setBackground(Color.white);
    setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

    setFocusable(true);
    requestFocus();    // the JPanel now has focus, so receives key events
	readyForTermination();

    // create game components
    imsLoader = new ImagesLoader(IMS_TEXT);
    initImages();
    
    /////make array of gestures to pass to Hands Object///////
    gestures = new HashMap<String, BufferedImage>(3);
    gestures.put("rock", rockHand);
    gestures.put("paper", paperHand);
    gestures.put("scissors", scissorsHand);
    /////////////////////////////////////////////////
    
    gameState = INTRO_SCREEN;
    timeAtWTS = 0;
    
    addMouseListener( new MouseAdapter() {
      public void mousePressed(MouseEvent e)
      { 
    	  if(gameState == INTRO_SCREEN)
    	  playerSelect(e.getX(), e.getY()); 
      }
    });
    

    // set up message font
    font = new Font("SansSerif", Font.BOLD, 34);
    metrics = this.getFontMetrics(font);

    // initialise timing elements
    fpsStore = new double[NUM_FPS];
    upsStore = new double[NUM_FPS];
    for (int i=0; i < NUM_FPS; i++) {
      fpsStore[i] = 0.0;
      upsStore[i] = 0.0;
    }
  }  // end of WormPanel()
  
  public void addNotify()
  // wait for the JPanel to be added to the JFrame before starting
  { super.addNotify();   // creates the peer
    startGame();    // start the thread
  }
  
  private void initImages()
  {
    // initialize the 'o' image variables
    iconsLeft = imsLoader.getImage("iconsLeft");
    iconsRight = imsLoader.getImage("iconsRight");
    titleText = imsLoader.getImage("titleText");
    playerSelect = imsLoader.getImage("playerSelect");
    rockHand = imsLoader.getImage("rockHand");
    paperHand = imsLoader.getImage("paperHand");
    scissorsHand = imsLoader.getImage("scissorsHand");
    Background1 = imsLoader.getImage("Background1");
  }  // end of initImages()

  private void readyForTermination()
  {
	addKeyListener( new KeyAdapter() {
	// listen for esc, q, end, ctrl-c on the canvas to
	// allow a convenient exit from the full screen configuration
       public void keyPressed(KeyEvent e)
       { int keyCode = e.getKeyCode();
         if ((keyCode == KeyEvent.VK_ESCAPE) ||
             (keyCode == KeyEvent.VK_END) ||
             ((keyCode == KeyEvent.VK_C) && e.isControlDown()) ) {
           running = false;
         }
       }
     });
  }  // end of readyForTermination()



  private void startGame()
  // initialise and start the thread 
  { 
    if (animator == null || !running) {
      animator = new Thread(this);
	  animator.start();
    }
  } // end of startGame()
    

  // ------------- game life cycle methods ------------
  // called by the JFrame's window listener methods


  public void resumeGame()
  // called when the JFrame is activated / deiconified
  {  isPaused = false;  } 


  public void pauseGame()
  // called when the JFrame is deactivated / iconified
  { isPaused = true;   } 


  public void stopGame() 
  // called when the JFrame is closing
  {  running = false;   }

  // ----------------------------------------------


  private void playerSelect(int x, int y)
  // is (x,y) near the head or should an obstacle be added?
  {
    if((x-433)*(x-433)+(y-409)*(x-409) < 20*20)
    {
    	areTwoPlayers = false;
    	hands = new Hands(gestures, areTwoPlayers, this);
    	gameState = WAITING_TO_START;
    	addKeyListener(hands);
    }
    if((x-567)*(x-567)+(y-409)*(y-409) < 20*20)
    {
    	areTwoPlayers = true;
    	hands = new Hands(gestures, areTwoPlayers, this);
    	gameState = WAITING_TO_START;
    	addKeyListener(hands);
    }
  }  // end of playerSelect()


  public void run()
  /* The frames of the animation are drawn inside the while loop. */
  {
    long beforeTime, afterTime, timeDiff, sleepTime;
    int overSleepTime = 0;
    int noDelays = 0;
    int excess = 0;
    Graphics g;

    gameStartTime = System.currentTimeMillis();
    prevStatsTime = gameStartTime;
    beforeTime = gameStartTime;

	running = true;

	while(running) {
	  gameUpdate(); 
      gameRender();   // render the game to a buffer
      paintScreen();  // draw the buffer on-screen

      afterTime = System.currentTimeMillis();
      timeDiff = afterTime - beforeTime;
      sleepTime = (period - timeDiff) - overSleepTime;  

      if (sleepTime > 0) {   // some time left in this cycle
        try {
          Thread.sleep(sleepTime);  // already in ms
        }
        catch(InterruptedException ex){}
        overSleepTime = (int)((System.currentTimeMillis() - afterTime) - sleepTime);
      }
      else {    // sleepTime <= 0; the frame took longer than the period
        excess -= sleepTime;  // store excess time value
        overSleepTime = 0;

        if (++noDelays >= NO_DELAYS_PER_YIELD) {
          Thread.yield();   // give another thread a chance to run
          noDelays = 0;
        }
      }

      beforeTime = System.currentTimeMillis();

      /* If frame animation is taking too long, update the game state
         without rendering it, to get the updates/sec nearer to
         the required FPS. */
      int skips = 0;
      while((excess > period) && (skips < MAX_FRAME_SKIPS)) {
        excess -= period;
	    gameUpdate();    // update state but don't render
        skips++;
      }
      framesSkipped += skips;

      storeStats();
	}

    printStats();
    System.exit(0);   // so window disappears
  } // end of run()


  private void gameUpdate() 
  {
	  if(gameState == WAITING_TO_START)
	  {
		  if(timeAtWTS == 0)
			  timeAtWTS = System.currentTimeMillis();
		  if(System.currentTimeMillis() - timeAtWTS > 2*1000)
		  {
			  hands.startShaking();
			  gameState = GAME_TIME;
		  }
	  }
	  
  }  // end of gameUpdate()


  private void gameRender()
  {
    if (dbImage == null){
      dbImage = createImage(PWIDTH, PHEIGHT);
      if (dbImage == null) {
        System.out.println("dbImage is null");
        return;
      }
      else
        dbg = dbImage.getGraphics();
    }

    // clear the background
    dbg.drawImage(Background1, 0, 0, null);
    
    /* commented out cause I won't be using it
	dbg.setColor(Color.blue);
    dbg.setFont(font);

    // report frame count & average FPS and UPS at top left
	// dbg.drawString("Frame Count " + frameCount, 10, 25);
	dbg.drawString("Average FPS/UPS: " + df.format(averageFPS) + ", " +
                                df.format(averageUPS), 20, 25);  // was (10,55)
    */

	dbg.drawImage(iconsLeft, 10, 10, null);
	dbg.drawImage(iconsRight, 740, 10, null);
	
	if(gameState == INTRO_SCREEN)
		dbg.drawImage(playerSelect, 405, 382, null);
	
    // draw game elements: the obstacles and the worm
	if(gameState ==GAME_TIME)
		hands.draw(dbg);


    if (gameOver)
      gameOverMessage(dbg);
  }  // end of gameRender()
  
  public void reportOutcome(String victor)
  {
	  if(victor.equals("left"))
		  msg = "PLAYER ONE WINS";
	  else
		  msg = "PLAYER TWO WINS";
	  
	  gameOver = true;
	 
  }


  private void gameOverMessage(Graphics g)
  // center the game-over message in the panel
  {
	int x = (PWIDTH - metrics.stringWidth(msg))/2; 
	int y = (PHEIGHT - metrics.getHeight())/2;
	g.setColor(Color.red);
    g.setFont(font);
	g.drawString(msg, x, y);
  }  // end of gameOverMessage()


  private void paintScreen()
  // use active rendering to put the buffered image on-screen
  { 
    Graphics g;
    try {
      g = this.getGraphics();
      if ((g != null) && (dbImage != null))
        g.drawImage(dbImage, 0, 0, null);
      Toolkit.getDefaultToolkit().sync();  // sync the display on some systems
      g.dispose();
    }
    catch (Exception e)
    { System.out.println("Graphics error: " + e);  }
  } // end of paintScreen()


  private void storeStats()
  /* The statistics:
       - the summed periods for all the iterations in this interval
         (period is the amount of time a single frame iteration should take), 
         the actual elapsed time in this interval, 
         the error between these two numbers;

       - the total frame count, which is the total number of calls to run();

       - the frames skipped in this interval, the total number of frames
         skipped. A frame skip is a game update without a corresponding render;

       - the FPS (frames/sec) and UPS (updates/sec) for this interval, 
         the average FPS & UPS over the last NUM_FPSs intervals.

     The data is collected every MAX_STATS_INTERVAL  (1 sec).
  */
  { 
    frameCount++;
    statsInterval += period;

    if (statsInterval >= MAX_STATS_INTERVAL) {     // record stats every MAX_STATS_INTERVAL
      long timeNow = System.currentTimeMillis();
      timeSpentInGame = (int) ((timeNow - gameStartTime)/1000L);  // ms --> secs

      long realElapsedTime = timeNow - prevStatsTime;   // time since last stats collection
      totalElapsedTime += realElapsedTime;

      double timingError = 
         ((double)(realElapsedTime - statsInterval) / statsInterval) * 100.0;

      totalFramesSkipped += framesSkipped;

      double actualFPS = 0;     // calculate the latest FPS and UPS
      double actualUPS = 0;
      if (totalElapsedTime > 0) {
        actualFPS = (((double)frameCount / totalElapsedTime) * 1000L);
        actualUPS = (((double)(frameCount + totalFramesSkipped) / totalElapsedTime) 
                                                             * 1000L);
      }

      // store the latest FPS and UPS
      fpsStore[ (int)statsCount%NUM_FPS ] = actualFPS;
      upsStore[ (int)statsCount%NUM_FPS ] = actualUPS;
      statsCount = statsCount+1;

      double totalFPS = 0.0;     // total the stored FPSs and UPSs
      double totalUPS = 0.0;
      for (int i=0; i < NUM_FPS; i++) {
        totalFPS += fpsStore[i];
        totalUPS += upsStore[i];
      }

      if (statsCount < NUM_FPS) { // obtain the average FPS and UPS
        averageFPS = totalFPS/statsCount;
        averageUPS = totalUPS/statsCount;
      }
      else {
        averageFPS = totalFPS/NUM_FPS;
        averageUPS = totalUPS/NUM_FPS;
      }
/*
      System.out.println(timedf.format( (double) statsInterval/1000L) + " " + 
                    timedf.format((double) realElapsedTime/1000L) + "s " + 
			        df.format(timingError) + "% " + 
                    frameCount + "c " +
                    framesSkipped + "/" + totalFramesSkipped + " skip; " +
                    df.format(actualFPS) + " " + df.format(averageFPS) + " afps; " + 
                    df.format(actualUPS) + " " + df.format(averageUPS) + " aups" );
*/
      framesSkipped = 0;
      prevStatsTime = timeNow;
      statsInterval = 0L;   // reset
    }
  }  // end of storeStats()


  private void printStats()
  {
    System.out.println("Frame Count/Loss: " + frameCount + " / " + totalFramesSkipped);
	System.out.println("Average FPS: " + df.format(averageFPS));
	System.out.println("Average UPS: " + df.format(averageUPS));
    System.out.println("Time Spent: " + timeSpentInGame + " secs");
  }  // end of printStats()

}  // end of WormPanel class
