package game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class PlayerDirection extends Polygon{
	
	public Player player;
	public Double[] points;
	public int DIF = 140;
	
	public PlayerDirection(Player player) {
		super();
		this.player = player;
		this.setFill(Color.BLACK);
		points = new Double[6];
		points[0] = player.getCenterX() + player.getRadius() * Math.cos(Math.toRadians(player.t));
		points[1] = player.getCenterY() + player.getRadius() * Math.sin(Math.toRadians(player.t));
		points[2] = player.getCenterX() + player.getRadius() * Math.cos(Math.toRadians(player.t-DIF));
		points[3] = player.getCenterY() + player.getRadius() * Math.sin(Math.toRadians(player.t-DIF));
		points[4] = player.getCenterX() + player.getRadius() * Math.cos(Math.toRadians(player.t+DIF));
		points[5] = player.getCenterY() + player.getRadius() * Math.sin(Math.toRadians(player.t+DIF));	
	}
	
	public void move() {
		this.points[0] = this.player.getCenterX() + this.player.getRadius() * Math.cos(Math.toRadians(this.player.t));
		this.points[1] = this.player.getCenterY() + this.player.getRadius() * Math.sin(Math.toRadians(this.player.t));
		this.points[2] = this.player.getCenterX() + this.player.getRadius() * Math.cos(Math.toRadians(this.player.t-DIF));
		this.points[3] = this.player.getCenterY() + this.player.getRadius() * Math.sin(Math.toRadians(this.player.t-DIF));
		this.points[4] = this.player.getCenterX() + this.player.getRadius() * Math.cos(Math.toRadians(this.player.t+DIF));
		this.points[5] = this.player.getCenterY() + this.player.getRadius() * Math.sin(Math.toRadians(this.player.t+DIF));
		this.getPoints().clear();
		this.getPoints().addAll(this.points);
	}
	
	

}
