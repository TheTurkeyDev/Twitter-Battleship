package dev.theturkey.twitterbattleship;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameStateSave
{
	private File saveData;

	public GameStateSave()
	{
		saveData = new File("save.tpbs");
		if(!saveData.exists())
		{
			try
			{
				saveData.createNewFile();
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public GameStateWrapper loadGameState()
	{
		try
		{
			FileInputStream fis = new FileInputStream(saveData);
			byte[] data = new byte[(int) saveData.length()];
			fis.read(data);
			fis.close();
			String fileInput = new String(data, StandardCharsets.UTF_8);
			String[] lines = fileInput.replaceAll("\r", "").split("\n");
			if(lines.length == 5)
			{
				GameStateWrapper wrapper = new GameStateWrapper();
				wrapper.playerBoard = loadGameBoard(lines[0], lines[2]);
				wrapper.cpuBoard = loadGameBoard(lines[1], lines[3]);
				wrapper.lastTweetID = Long.parseLong(lines[4]);
				return wrapper;
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public GameBoard loadGameBoard(String boats, String guesses)
	{
		List<BoatInfo> boatsList = new ArrayList<>();
		String[] lineBoats = boats.split(";");
		for(String boatString : lineBoats)
		{
			String[] boatParts = boatString.split(",");
			int id = Integer.parseInt(boatParts[0]);
			int x = Integer.parseInt(boatParts[1]);
			int y = Integer.parseInt(boatParts[2]);
			Direction dir = Direction.valueOf(boatParts[3]);
			boatsList.add(getBoatInfoForID(id, x, y, dir));
		}

		List<int[]> guessesList = new ArrayList<>();
		String[] lineGuesses = guesses.split(";");
		for(String guessString : lineGuesses)
		{
			String[] boatParts = guessString.split(",");
			int x = Integer.parseInt(boatParts[0]);
			int y = Integer.parseInt(boatParts[1]);
			guessesList.add(new int[]{x, y});
		}

		return new GameBoard(boatsList, guessesList);
	}

	private BoatInfo getBoatInfoForID(int id, int x, int y, Direction direction)
	{
		switch(id)
		{
			case 0:
				return new BoatInfo(5, x, y, direction, GameBoard.carrier);
			case 1:
				return new BoatInfo(4, x, y, direction, GameBoard.battleship);
			case 2:
				return new BoatInfo(3, x, y, direction, GameBoard.sub);
			case 3:
				return new BoatInfo(3, x, y, direction, GameBoard.destroyer);
			case 4:
				return new BoatInfo(2, x, y, direction, GameBoard.boat);
		}
		return null;
	}

	private int getIDFromBoatIMG(BufferedImage img)
	{
		if(GameBoard.carrier.equals(img))
			return 0;
		else if(GameBoard.battleship.equals(img))
			return 1;
		else if(GameBoard.sub.equals(img))
			return 2;
		else if(GameBoard.destroyer.equals(img))
			return 3;
		else
			return 4;
	}

	public void saveGame(GameBoard playerBoard, GameBoard cpuBoard, long lastTweetID, boolean gameOver)
	{
		StringBuilder saveBuffer = new StringBuilder();
		if(!gameOver)
		{
			saveBoatsToBuffer(saveBuffer, playerBoard.getBoats());
			saveBuffer.deleteCharAt(saveBuffer.length() - 1).append("\n");
			saveBoatsToBuffer(saveBuffer, cpuBoard.getBoats());
			saveBuffer.deleteCharAt(saveBuffer.length() - 1).append("\n");
			saveGuessesToBuffer(saveBuffer, playerBoard.getGuessedPos());
			saveBuffer.deleteCharAt(saveBuffer.length() - 1).append("\n");
			saveGuessesToBuffer(saveBuffer, cpuBoard.getGuessedPos());
			saveBuffer.deleteCharAt(saveBuffer.length() - 1).append("\n");
			saveBuffer.append(lastTweetID);
		}
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveData));
			writer.write(saveBuffer.toString());
			writer.close();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void saveBoatsToBuffer(StringBuilder saveBuffer, List<BoatInfo> boats)
	{
		for(BoatInfo boatInfo : boats)
		{
			saveBuffer.append(getIDFromBoatIMG(boatInfo.img)).append(",");
			saveBuffer.append(boatInfo.x).append(",").append(boatInfo.y).append(",");
			saveBuffer.append(boatInfo.direction.name()).append(";");
		}
	}

	public void saveGuessesToBuffer(StringBuilder saveBuffer, List<int[]> guesses)
	{
		for(int[] guess : guesses)
			saveBuffer.append(guess[0]).append(",").append(guess[1]).append(";");
	}
}
