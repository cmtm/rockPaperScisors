import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;


public class Hands implements KeyListener, Runnable {
	
	
	private boolean areTwoPlayers;
	private boolean canSwitch;
	private String victor;
	//private boolean closedFist; redundant
	
	
	private HashMap<String, BufferedImage> gestures;
	private HashMap<String, String> choice;
	
	private int yPosHands;
	private final static float SPEED = 1;
	
	private Thread shaking = new Thread(this);
	private long timer;
	
	private RPSPanel rpsp;
	
	
	public Hands(HashMap<String, BufferedImage> gestures, boolean areTwoPlayers, RPSPanel rpsp)
	{
		this.areTwoPlayers = areTwoPlayers;
		canSwitch = true;
		victor = "tie";
		this.gestures = gestures;
		this.rpsp = rpsp;
		//closedFist = true;    redundant
		
		choice = new HashMap<String, String>(2);
		choice.put("left", "rock");
		choice.put("right", "rock");
		
		yPosHands = 625-gestures.get("rock").getHeight();
		
	}
	
	public void run()
	{
		while(victor.equals("tie"))
		{
			canSwitch = true;
			for(int i = 0; i<3; i++)
			{
				try {
					for(int j = 0; j < 165; j++)
					{
						yPosHands--;
						Thread.sleep(4);
					}
					Thread.sleep(200);
					for(int k = 0; k < 165; k+=5)
					{
						yPosHands+=5;
						Thread.sleep(4);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
			canSwitch = false;
			//closedFist = flase;    redundant
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			victor = checkForVictory();
		}
		rpsp.reportOutcome(victor);
	}
	
	public void startShaking()
	{
		shaking.start();		
	}
	
	private String checkForVictory()
	{
		if(choice.get("left").equals(choice.get("right")))
			return "tie";
		
		if(choice.get("left").equals("rock"))
		{
			if(choice.get("right").equals("scissors"))
				return "left";
			else
				return "right";
		}
		
		if(choice.get("left").equals("paper"))
		{
			if(choice.get("right").equals("rock"))
				return "left";
			else
				return "right";
		}
		
		if(choice.get("left").equals("scissors"))
		{
			if(choice.get("right").equals("paper"))
				return "left";
			else
				return "right";
		}
		
		return "error in choices";

	}
	
	public void draw(Graphics g)
	{
		if(canSwitch)
		{
			g.drawImage(gestures.get("rock"), 0, yPosHands, null);
			g.drawImage(gestures.get("rock"), 1000, yPosHands, 1000-400, yPosHands + 400, 0, 0, 400, 400, null);
		}
		else
		{
			g.drawImage(gestures.get(choice.get("left")), 0, yPosHands, null);
			g.drawImage(gestures.get(choice.get("right")), 1000, yPosHands, 1000-400, yPosHands + 400, 0, 0, 400 , 400, null);
		}
		
	}

	
	
///////////////  Key Press Listeners  //////////////////////////
	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (canSwitch) {
			//left player
			if (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q') {
				choice.remove("left");
				choice.put("left", "rock");
			}
			if (e.getKeyChar() == 'w' || e.getKeyChar() == 'W') {
				choice.remove("left");
				choice.put("left", "paper");
			}
			if (e.getKeyChar() == 'e' || e.getKeyChar() == 'E') {
				choice.remove("left");
				choice.put("left", "scissors");
			}
			if (areTwoPlayers) {
				//right player
				if (e.getKeyChar() == 'i' || e.getKeyChar() == 'I') {
					choice.remove("right");
					choice.put("right", "rock");
				}
				if (e.getKeyChar() == 'o' || e.getKeyChar() == 'O') {
					choice.remove("right");
					choice.put("right", "paper");
				}
				if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {
					choice.remove("right");
					choice.put("right", "scissors");
				}
			}
		}	

	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
	}

	@Override
	public void keyTyped(KeyEvent e) 
	{
	}
///////////////////////////////////////////////////////////////

}
