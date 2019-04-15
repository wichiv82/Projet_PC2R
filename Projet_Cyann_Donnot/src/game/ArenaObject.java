package game;

import java.util.Random;

import javafx.scene.shape.Circle;

public abstract class ArenaObject extends Circle implements IArenaObject {

	public int MAX_SPEED = 6;
	public int TURNIT = 6;
	
	public Arena arena;
	public String name;
	public Random random;
	
	
	public ArenaObject(Arena arena, String name) {
		
		this.arena = arena;
		this.name = name;
		this.random = new Random();
	}
	
	@Override
	public void move() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void speed_up(int thrust) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void turn(int intensity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void collision() {
		// TODO Auto-generated method stub
		
	}

	public double square_of_distance(Circle o) {
		return (o.getCenterX() - this.getCenterX())
				*(o.getCenterX() - this.getCenterX())
				+(o.getCenterY() - this.getCenterY())
				*(o.getCenterY() - this.getCenterY());
		
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public abstract void refresh_from(String s);

}
