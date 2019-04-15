package game;

import javafx.scene.paint.Color;

public class Obstacle extends ArenaObject {

	public Obstacle(Arena arena, int radius) {
		super(arena, "obstacle");
		this.setRadius(radius);
		this.setFill(Color.BLACK);
		this.setStroke(Color.DARKRED);
		this.setStrokeWidth((int)radius*0.1);
		this.setCenterX(this.arena.width*random.nextDouble());
    	this.setCenterY(this.arena.height*random.nextDouble());
	}

	public void refresh_from(String s) {
		String[] parse = s.split("X|Y");
		this.setCenterX(arena.width/2 + Double.parseDouble(parse[1]));
		this.setCenterY(arena.height/2 + Double.parseDouble(parse[2]));
	}
	
	
	

}
