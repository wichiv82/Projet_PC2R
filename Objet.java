import javafx.geometry.Point2D;

public interface Objet {
	public Point2D getPosition();
	public void setPosition(Point2D p);
	
	public double getRadius();
	public void setRadius(double r);
	
	public boolean isObjectif();
}
