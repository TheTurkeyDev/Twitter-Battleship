package dev.theturkey.twitterbattleship;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameBoard
{
	private static final Color WATER = Color.decode("#5656f0");
	public static final Color BLACK = Color.decode("#000000");

	public static BufferedImage carrier;
	public static BufferedImage battleship;
	public static BufferedImage sub;
	public static BufferedImage destroyer;
	public static BufferedImage boat;
	public static BufferedImage hit;
	public static BufferedImage miss;

	static
	{
		try
		{
			carrier = ImageIO.read(GameBoard.class.getResource("/carrier.png"));
			battleship = ImageIO.read(GameBoard.class.getResource("/battleship.png"));
			sub = ImageIO.read(GameBoard.class.getResource("/sub.png"));
			destroyer = ImageIO.read(GameBoard.class.getResource("/destroyer.png"));
			boat = ImageIO.read(GameBoard.class.getResource("/boat.png"));
			hit = ImageIO.read(GameBoard.class.getResource("/hit.png"));
			miss = ImageIO.read(GameBoard.class.getResource("/miss.png"));
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}


	private List<BoatInfo> boats = new ArrayList<>();
	private List<int[]> guessedPos = new ArrayList<>();

	public void setup()
	{
		placeBoat(5, carrier);
		placeBoat(4, battleship);
		placeBoat(3, sub);
		placeBoat(3, destroyer);
		placeBoat(2, boat);
	}

	public void placeBoat(int length, BufferedImage img)
	{
		boolean valid = false;
		int x;
		int y;
		Direction dir = Direction.DOWN;
		do
		{
			x = Core.rand.nextInt(10);
			y = Core.rand.nextInt(10);
			for(Direction d : getDirections())
			{
				dir = d;
				valid = true;
				for(int i = 0; i < length; i++)
				{
					int[] loc = d.getOffset(x, y, i);
					if(loc[0] < 0 || loc[0] > 9 || loc[1] < 0 || loc[1] > 9 || getBoatAt(loc[0], loc[1]) != null)
					{
						valid = false;
						break;
					}
				}

				if(valid)
					break;
			}
		} while(!valid);
		boats.add(new BoatInfo(length, x, y, dir, img));
	}

	public Direction[] getDirections()
	{
		Direction[] returnArray = Direction.values();
		for(int i = returnArray.length - 1; i > 0; i--)
		{
			int index = Core.rand.nextInt(i + 1);
			Direction a = returnArray[index];
			returnArray[index] = returnArray[i];
			returnArray[i] = a;
		}
		return returnArray;
	}

	public ActionResult makeGuessAt(int x, int y)
	{
		guessedPos.add(new int[]{x, y});
		BoatInfo hit = getBoatAt(x, y);
		return hit == null ? ActionResult.MISS : (isShipSunk(hit) ? ActionResult.SUNK : ActionResult.HIT);
	}

	public boolean hasGuessed(int x, int y)
	{
		for(int[] pos : guessedPos)
			if(pos[0] == x && pos[1] == y)
				return true;
		return false;
	}

	public BoatInfo getBoatAt(int x, int y)
	{
		for(BoatInfo boatInfo : boats)
		{
			for(int i = 0; i < boatInfo.length; i++)
			{
				int[] offsetLoc = boatInfo.direction.getOffset(boatInfo.x, boatInfo.y, i);
				if(offsetLoc[0] == x && offsetLoc[1] == y)
					return boatInfo;
			}
		}
		return null;
	}

	public boolean isShipSunk(BoatInfo info)
	{
		for(int i = 0; i < info.length; i++)
		{
			int[] offsetLoc = info.direction.getOffset(info.x, info.y, i);
			boolean hit = false;
			for(int[] guess : guessedPos)
			{
				if(offsetLoc[0] == guess[0] && offsetLoc[1] == guess[1])
				{
					hit = true;
					break;
				}
			}

			if(!hit)
				return false;
		}
		return true;
	}

	public boolean hasLost()
	{
		int hits = 0;
		for(BoatInfo boatInfo : boats)
		{
			for(int i = 0; i < boatInfo.length; i++)
			{
				int[] pos = boatInfo.direction.getOffset(boatInfo.x, boatInfo.y, i);
				if(hasGuessed(pos[0], pos[1]))
					hits++;
			}

		}
		return hits == 17;
	}

	public void getImage(Graphics2D g, boolean playerBoard, boolean gameOver)
	{
		int parentXOff = (playerBoard ? 0 : 600);

		g.setColor(BLACK);
		g.setFont(new Font(g.getFont().getName(), Font.BOLD, 64));
		if(playerBoard)
		{
			g.drawString("PLAYER", 175, 60);
		}
		else
		{
			g.drawString("COMPUTER", 715, 60);
		}

		for(int y = 0; y < 10; y++)
		{
			for(int x = 0; x < 10; x++)
			{
				int xOff = parentXOff + 50 + (x * 50);
				int yOff = 75 + (y * 50);
				g.setColor(WATER);
				g.fillRect(xOff, yOff, 50, 50);
				Stroke oldStroke = g.getStroke();
				g.setStroke(new BasicStroke(4f));
				g.setColor(BLACK);
				g.drawRect(xOff, yOff, 50, 50);
				g.setStroke(oldStroke);
			}
		}

		g.setColor(Color.BLACK);
		g.setFont(new Font(g.getFont().getName(), g.getFont().getStyle(), 24));
		for(int y = 0; y < 10; y++)
		{
			g.drawString(String.valueOf(y + 1), parentXOff + 20, 110 + (y * 50));
		}

		for(int x = 0; x < 10; x++)
		{
			g.drawString(String.valueOf((char) (65 + x)), parentXOff + 70 + (x * 50), 600);
		}

		for(BoatInfo boatInfo : boats)
			if(playerBoard || gameOver || isShipSunk(boatInfo))
				boatInfo.draw(g, parentXOff);

		for(int[] guess : guessedPos)
		{
			AffineTransform at = new AffineTransform();

			if(getBoatAt(guess[0], guess[1]) != null)
			{
				at.translate(65 + parentXOff + (50 * guess[0]), 80 + (50 * guess[1]));
				g.drawImage(hit, at, null);
			}
			else
			{
				at.translate(60 + parentXOff + (50 * guess[0]), 85 + (50 * guess[1]));
				g.drawImage(miss, at, null);
			}
		}
	}
}
