package game;

import javafx.scene.shape.Circle;

public interface IArenaObject {

	
	
	public void move();
	public void speed_up(int thrust);
	public void turn(int intensity);
	public void collision();
	public double square_of_distance(Circle o);
	public String getName();
	public void refresh_from(String s);
}
