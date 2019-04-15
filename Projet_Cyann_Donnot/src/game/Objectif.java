package game;

import java.util.Random;

import javafx.scene.paint.Color;


public class Objectif extends ArenaObject {

	public int value;
	
	public Objectif(Arena arena, int radius, int value) {
		super(arena,"objectif");
		random = new Random();
		this.setRadius(radius);
		this.setFill(Color.YELLOW);
		this.setStroke(Color.ORANGE);
		this.setCenterX(this.arena.width*random.nextDouble());
		this.setCenterY(this.arena.height*random.nextDouble());
		this.value = value;
	}
	
	public void collision() {
		/*
		boolean picked = false;
		for(IArenaObject o : this.arena.values()) {
			if (o.getName().contains("obstacle")) {
				Obstacle obstacle = (Obstacle)o;
				if ((obstacle.getRadius() + this.getRadius())*(obstacle.getRadius() + this.getRadius()) 
						> this.square_of_distance(obstacle)){
					picked = true;
				}
				continue;
			}
			if (!o.getName().contains("objectif")) {
				Player player = (Player)o;
				if ((player.getRadius() + this.getRadius())*(player.getRadius() + this.getRadius()) 
						> this.square_of_distance(player)){
					player.score += this.value;
					picked = true;
				}
			}
		}
		if (picked) this.picked();*/
	}
	
	public void picked() {
		this.setCenterX(this.arena.width*random.nextDouble());
		this.setCenterY(this.arena.height*random.nextDouble());
	}
	
	public void refresh_from(String s) {
		String[] parse = s.split("X|Y");
		this.setCenterX(arena.width/2 + Double.parseDouble(parse[1]));
		this.setCenterY(arena.height/2 + Double.parseDouble(parse[2]));
	}
}
