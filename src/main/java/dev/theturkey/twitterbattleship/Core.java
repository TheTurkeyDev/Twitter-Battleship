package dev.theturkey.twitterbattleship;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class Core
{
	public static final Random rand = new Random();

	public static final Font FONT_LARGE = new Font("DejaVu Serif", Font.BOLD, 64);
	public static final Font FONT_MEDIUM = new Font("DejaVu Serif", Font.BOLD, 52);
	public static final Font FONT_SMALL = new Font("DejaVu Serif", Font.BOLD, 24);

	private GameBoard playerBoard;
	private GameBoard cpuBoard;
	private SimpleAI computerAI;
	private Twitter twitter;
	private long lastStatus;

	private boolean gameOver = false;
	private boolean cpuWon = false;

	private GameStateSave gameSave;

	public Core()
	{
		twitter = TwitterFactory.getSingleton();
		gameSave = new GameStateSave();
	}

	public void initGame()
	{
		int playerWins = playerBoard.getScore();
		playerBoard = new GameBoard(playerWins);
		int cpuWins = cpuBoard.getScore();
		cpuBoard = new GameBoard(cpuWins);
		computerAI = new SimpleAI();
		playerBoard.setup();
		cpuBoard.setup();
	}

	public void start()
	{
		GameStateWrapper stateWrapper = gameSave.loadGameState();
		if(stateWrapper != null)
		{
			playerBoard = stateWrapper.playerBoard;
			cpuBoard = stateWrapper.cpuBoard;
			lastStatus = stateWrapper.lastTweetID;
			computerAI = new SimpleAI();
			System.out.println("Game loaded from save");
		}
		else
		{
			gameOver = true;
			System.out.println("No game to load in!");
		}

		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1);

		System.out.println("Bot will run next at " + calendar.getTime().toString());

		Timer t = new Timer();
		t.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if(gameOver)
				{
					gameOver = false;
					initGame();
					step();
				}
				else
				{
					try
					{
						Query query = new Query("to:BattleshipGBot");
						query.setSinceId(lastStatus);
						query.resultType(Query.ResultType.recent);
						query.count(100);
						QueryResult result = twitter.search(query);
						List<Status> replies = result.getTweets().stream().filter(t -> isValidComment(t)).collect(Collectors.toList());
						while(result.hasNext())
						{
							result = twitter.search(result.nextQuery());
							replies.addAll(result.getTweets().stream().filter(t -> isValidComment(t)).collect(Collectors.toList()));
						}

						Optional<Status> winner = replies.stream().max(Comparator.comparing(Status::getFavoriteCount));
						int x;
						int y;
						if(winner.isPresent())
						{
							String guess = winner.get().getText().toLowerCase().replace("@battleshipgbot", "").trim();
							x = guess.charAt(0) - 97;
							y = Integer.parseInt(guess.substring(1)) - 1;
						}
						else
						{
							x = rand.nextInt(10);
							y = rand.nextInt(10);
							while(cpuBoard.hasGuessed(x, y))
							{
								x = rand.nextInt(10);
								y = rand.nextInt(10);
							}
						}
						cpuBoard.makeGuessAt(x, y);

						if(cpuBoard.hasLost())
						{
							gameOver = true;
							cpuWon = false;
							playerBoard.win();
						}
						else
						{
							computerAI.act(playerBoard);
							if(playerBoard.hasLost())
							{
								gameOver = true;
								cpuWon = true;
								cpuBoard.win();
							}
						}

						step();
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}, calendar.getTime(), 1000 * 60 * 60);
	}

	public boolean isValidComment(Status t)
	{
		if(t.getInReplyToStatusId() != lastStatus)
			return false;

		String lowerCased = t.getText().toLowerCase().replace("@battleshipgbot", "").trim();
		if(!lowerCased.matches("^[a-j]([1-9]|10)$"))
			return false;

		int x = lowerCased.charAt(0) - 97;
		int y = Integer.parseInt(lowerCased.substring(1)) - 1;

		return !cpuBoard.hasGuessed(x, y);
	}

	private void step()
	{
		gameSave.saveGame(playerBoard, cpuBoard, lastStatus, gameOver);
		try
		{
			StatusUpdate statusUpdate = new StatusUpdate("");
			statusUpdate.setMedia(genImage());
			Status status = twitter.updateStatus(statusUpdate);
			lastStatus = status.getId();
		} catch(TwitterException e)
		{
			e.printStackTrace();
		}
	}

	private File genImage()
	{
		BufferedImage bufferedImage = new BufferedImage(1200, 675, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 1200, 657);

		playerBoard.getImage(g, true, gameOver);
		cpuBoard.getImage(g, false, gameOver);

		g.setColor(GameBoard.BLACK);
		g.setFont(FONT_MEDIUM);

		if(gameOver)
			g.drawString("GAME OVER! " + (cpuWon ? " The Computer has won!" : " Twitter has won!"), 25, 650);
		else
			g.drawString("Comment or vote for your guess below! ", 5, 650);

		File outputFile = new File("image.png");
		try
		{
			ImageIO.write(bufferedImage, "png", outputFile);
		} catch(IOException e)
		{
			e.printStackTrace();
		}

		return outputFile;
	}

	public static void main(String[] args)
	{
		Core core = new Core();
		core.start();
	}
}
