import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;


public class RoundsSelector implements MouseListener {
	
	private int roundsToWin;
	private BufferedImage  numberList, selectionCircle, smallRedCircle, smallGreenCircle;
	
	final static String IMS_TEXT = "imsList2.txt";
	private ImagesLoader imsLoader;
	
	private int leftWins, rightWins;
	private boolean inSecondPart;
	
	public RoundsSelector()
	{
		roundsToWin = 1;
		leftWins = 0;
		rightWins = 0;
		inSecondPart = false;
		
		imsLoader = new ImagesLoader(IMS_TEXT);
		initImages();		
	}
	
	private void initImages()
	{
		numberList = imsLoader.getImage("numberList");
		selectionCircle = imsLoader.getImage("selectionCircle");
		smallRedCircle = imsLoader.getImage("smallRedCircle");
		smallGreenCircle = imsLoader.getImage("smallGreenCircle");
	}
	
	public void addWin(String side)
	{
		if(side.equals("left"))
			leftWins++;
		if(side.equals("right"))
			rightWins++;
		else
			System.out.println("Error: addWin() recieved a bad side");
	}
	

	
	
	
////////////////////Event Listeners//////////////////////////	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
///////////////////////////////////////////////////////////////

}
