package dev.theturkey.twitterbattleship;

import java.util.ArrayList;
import java.util.List;

public class SimpleAI
{
	private static Direction[] checkDirs = new Direction[]{Direction.DOWN, Direction.RIGHT};

	public void act(GameBoard gameBoard)
	{
		int[][] probabilities = new int[GameBoard.BOARD_SIZE][GameBoard.BOARD_SIZE];

		List<BoatInfo> notSunk = new ArrayList<>();
		for(BoatInfo boatInfo : gameBoard.getBoats())
			if(!gameBoard.isShipSunk(boatInfo))
				notSunk.add(boatInfo);

		for(int y = 0; y < GameBoard.BOARD_SIZE; y++)
		{
			for(int x = 0; x < GameBoard.BOARD_SIZE; x++)
			{
				for(BoatInfo boatInfo : notSunk)
				{
					for(Direction dir : checkDirs)
					{
						boolean containsHit = false;
						boolean fits = true;
						for(int i = 0; i < boatInfo.length; i++)
						{
							int[] checkPos = dir.getOffset(x, y, i);
							if(checkPos[0] < 0 || checkPos[0] > 9 || checkPos[1] < 0 || checkPos[1] > 9)
							{
								fits = false;
								break;
							}

							boolean wasGuessed = gameBoard.hasGuessed(checkPos[0], checkPos[1]);
							BoatInfo boatAt = gameBoard.getBoatAt(checkPos[0], checkPos[1]);
							if(wasGuessed && (boatAt == null || !notSunk.contains(boatAt)))
							{
								fits = false;
								break;
							}
							else if(wasGuessed)
							{
								containsHit = true;
							}
						}

						if(!fits)
							continue;

						for(int i = 0; i < boatInfo.length; i++)
						{
							int[] checkPos = dir.getOffset(x, y, i);
							if(!gameBoard.hasGuessed(checkPos[0], checkPos[1]))
								probabilities[checkPos[0]][checkPos[1]] += containsHit ? 5 : 1;
						}
					}
				}
			}
		}

		int highestProb = 0;
		List<int[]> highestProbList = new ArrayList<>();
		for(int x = 0; x < GameBoard.BOARD_SIZE; x++)
		{
			for(int y = 0; y < GameBoard.BOARD_SIZE; y++)
			{
				int prob = probabilities[x][y];
				if(prob > highestProb)
				{
					highestProbList.clear();
					highestProb = prob;
					highestProbList.add(new int[]{x, y});
				}
				else if(prob == highestProb)
				{
					highestProbList.add(new int[]{x, y});
				}
			}
		}

		int[] chosenPoint = highestProbList.get(Core.rand.nextInt(highestProbList.size()));
		gameBoard.makeGuessAt(chosenPoint[0], chosenPoint[1]);
	}
}
