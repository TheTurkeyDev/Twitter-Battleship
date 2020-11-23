package dev.theturkey.twitterbattleship;

import java.util.ArrayList;
import java.util.List;

public class SimpleAI
{
	List<int[]> toGuess = new ArrayList<>();
	List<int[]> hits = new ArrayList<>();
	boolean dirFound = false;

	public void act(GameBoard gameBoard)
	{
		if(toGuess.size() > 0)
		{
			int[] guess;
			do
			{
				guess = toGuess.remove(0);
			} while(toGuess.size() > 0 && gameBoard.hasGuessed(guess[0], guess[1]));

			if(toGuess.size() == 0 && gameBoard.hasGuessed(guess[0], guess[1]))
			{
				makeRandGuess(gameBoard);
				dirFound = false;
			}
			else
			{
				ActionResult result = gameBoard.makeGuessAt(guess[0], guess[1]);
				if(result == ActionResult.HIT)
				{
					if(!dirFound)
					{
						dirFound = true;
						int xStep = guess[0] - hits.get(0)[0];
						int yStep = guess[1] - hits.get(0)[1];
						for(int i = 1; i < 5; i++)
						{
							int xx = guess[0] + (xStep * i);
							int yy = guess[1] + (yStep * i);
							if(xx < 0 || xx > 9 || yy < 0 || yy > 9)
								break;

							if(!gameBoard.hasGuessed(xx, yy))
								toGuess.add(0, new int[]{xx, yy});
						}
						for(int i = 1; i > -5; i--)
						{
							int xx = guess[0] + (xStep * i);
							int yy = guess[1] + (yStep * i);
							if(xx < 0 || xx > 9 || yy < 0 || yy > 9)
								break;

							if(!gameBoard.hasGuessed(xx, yy))
								toGuess.add(0, new int[]{xx, yy});
						}
					}
				}
				else if(result == ActionResult.MISS)
				{
					if(toGuess.size() == 0)
					{
						dirFound = false;
						return;
					}
					int tgX = toGuess.get(0)[0];
					int tgY = toGuess.get(0)[1];
					int xStep = Math.abs(guess[0] - tgX);
					int yStep = Math.abs(guess[1] - tgY);
					int lastX = tgX;
					int lastY = tgY;
					while(xStep == 1 ^ yStep == 1)
					{
						toGuess.remove(0);
						if(toGuess.size() == 0)
							break;
						tgX = toGuess.get(0)[0];
						tgY = toGuess.get(0)[1];
						xStep = Math.abs(lastX - tgX);
						yStep = Math.abs(lastY - tgY);
						lastX = tgX;
						lastY = tgY;
					}
				}
				else if(result == ActionResult.SUNK)
				{
					toGuess.clear();
					dirFound = false;
				}
			}
		}
		else
		{
			makeRandGuess(gameBoard);
		}
	}

	private void makeRandGuess(GameBoard gameBoard)
	{
		int x = Core.rand.nextInt(10);
		int y = Core.rand.nextInt(10);
		while(gameBoard.hasGuessed(x, y))
		{
			x = Core.rand.nextInt(10);
			y = Core.rand.nextInt(10);
		}

		ActionResult result = gameBoard.makeGuessAt(x, y);
		if(result == ActionResult.HIT)
		{
			hits.add(new int[]{x, y});
			for(Direction d : Direction.values())
			{
				int[] dirPos = d.getOffset(x, y, 1);
				if(dirPos[0] < 0 || dirPos[0] > 9 || dirPos[1] < 0 || dirPos[1] > 9)
					break;
				if(!gameBoard.hasGuessed(dirPos[0], dirPos[1]))
					toGuess.add(dirPos);
			}
		}
	}
}
