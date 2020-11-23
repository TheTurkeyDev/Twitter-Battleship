package dev.theturkey.twitterbattleship;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class BoatInfo
{
	public int length;
	public int x;
	public int y;
	public Direction direction;
	public BufferedImage img;

	public BoatInfo(int length, int x, int y, Direction direction, BufferedImage img)
	{
		this.length = length;
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.img = img;
	}

	public void draw(Graphics2D g, int parentXOff)
	{
		AffineTransform at = new AffineTransform();
		at.translate(50 + parentXOff + (50 * x), 75 + (50 * y));
		at.rotate(direction.getAngle(), 25, 25);
		g.drawImage(img, at, null);
	}
}
