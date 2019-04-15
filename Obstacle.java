import javafx.geometry.Point2D;

public class Obstacle implements Objet{

	private Point2D position;
	private double radius;
	
	public Obstacle(Point2D p, double r) {
		position = p;
		radius = r;
	}
	
	
	@Override
	public Point2D getPosition() {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public void setPosition(Point2D p) {
		// TODO Auto-generated method stub
		position = p;
	}

	@Override
	public double getRadius() {
		// TODO Auto-generated method stub
		return radius;
	}

	@Override
	public void setRadius(double r) {
		// TODO Auto-generated method stub
		radius = r;
	}

	@Override
	public boolean isObjectif() {
		// TODO Auto-generated method stub
		return false;
	}

}
