package game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlayerBomb extends Circle{

	public Player player;
	
	
	public PlayerBomb(Player player) {
		super();
		this.player = player;
		this.setCenterX(player.getCenterX());
		this.setCenterY(player.getCenterY());
		this.setRadius(70);
		this.setOpacity(0.2);
		this.setStroke(Color.GREEN);
		this.setVisible(false);
	}
	
	public void move() {
		this.setCenterX(player.getCenterX());
		this.setCenterY(player.getCenterY());
		
	}
	
}
