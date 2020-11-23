package dev.theturkey.twitterbattleship;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Direction
{
	UP(0, -1, Math.PI),
	DOWN(0, 1, 0),
	RIGHT(1, 0, -Math.PI / 2),
	LEFT(-1, 0, Math.PI / 2);

	private int xDiff;
	private int yDiff;
	private double angle;

	Direction(int xDiff, int yDiff, double angle)
	{
		this.xDiff = xDiff;
		this.yDiff = yDiff;
		this.angle = angle;
	}

	public int getXDiff()
	{
		return xDiff;
	}

	public int getYDiff()
	{
		return yDiff;
	}

	public double getAngle()
	{
		return angle;
	}

	public int[] getOffset(int x, int y, int amount)
	{
		return new int[]{x + (xDiff * amount), y + (yDiff * amount)};
	}

	public static Direction getNextDir(Direction curr)
	{
		List<Direction> dirs = Arrays.asList(Direction.values());
		return dirs.get((dirs.indexOf(curr) + 1) % dirs.size());
	}
}
