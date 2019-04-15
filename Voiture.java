import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class Voiture {
	String nom;
	Point2D position;
	Point2D vitesse;
	double direction;
	Color couleur;
	double radius;
	double vitesse_MAX;
	double turnit;
	int score;
	
	public Voiture(String nom, Point2D position){
		this.nom = nom;
		this.position = position;
		this.direction = Math.PI/(int)(Math.random() * 8);
		this.couleur = null;
		this.vitesse = new Point2D(0,0);
		this.radius = 40;
		this.vitesse_MAX = 6;
		this.turnit = Math.PI/4;
		this.score = 0;
	}

	public Voiture(String nom, Point2D position, Point2D vitesse){
		this.nom = nom;
		this.position = position;
		this.direction = Math.PI/(int)(Math.random() * 8);
		this.couleur = null;
		this.vitesse = vitesse;
		this.radius = 40;
		this.vitesse_MAX = 6;
		this.turnit = Math.PI/4;
		this.score = 0;
	}
	
	public void setCouleur(Color c) {
		this.couleur = c;
	}
	
	public void thrust(){
		double x = Math.max(Math.min(vitesse.getX() + Math.cos(direction), vitesse_MAX), -vitesse_MAX);
		double y = Math.max(Math.min(vitesse.getY() - Math.sin(direction), vitesse_MAX), -vitesse_MAX);
		vitesse = new Point2D(x, y);
	}
	
	public void anticlock(){
		direction -= turnit;
	}
	
	public void clock(){
		direction += turnit;
	}
	
	public void rotation(int r) {
		if (r < 0)
			for (int i = 0; i<-r; i++)
				anticlock();
		else
			for (int i = 0; i<r; i++)
				clock();
	}
	
	public void reverseVitesse(){
		double vx = -vitesse.getX();
		double vy= -vitesse.getY();
		vitesse = new Point2D(vx, vy);
	}

}
