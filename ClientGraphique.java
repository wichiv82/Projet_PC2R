import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ClientGraphique extends Application {
	private final static int tailleX = 1000;
	private final static int tailleY = 1000;
	
	private static int refresh_tickrate = 50;
	private static int serveur_tickrate = 40;

	private static Client client;
	private static String nom;
	
	private static Color[] couleurs = {Color.RED, Color.BLUE, Color.YELLOW, Color.BLACK, Color.GREEN};
	private static Color[] couleurs_ligne = {Color.GREEN, Color.YELLOW, Color.BLUE, Color.WHITE, Color.RED};
	
	private static ArrayList<Voiture> voitures;
	private static ArrayList<Text> zone_nom;
	
	private static ArrayList<Circle> cercles;
	private static ArrayList<Line> avant_vehicule;
	private static ArrayList<Color> couleurs_voitures;
	
	private static ArrayList<Objet> objets;
	private static ArrayList<Circle> ronds;
	
	private static ArrayList<String> inputAEnvoyer;
	
	public static void init(String n, ArrayList<Voiture> v, ArrayList<Objet> o) {
		nom = n;
		voitures = new ArrayList<Voiture>();
		objets = new ArrayList<Objet>();
		
		zone_nom = new ArrayList<Text>();
		cercles = new ArrayList<Circle>();
		avant_vehicule = new ArrayList<Line>();
		couleurs_voitures = new ArrayList<Color>();
		ronds = new ArrayList<Circle>();
		
		inputAEnvoyer = new ArrayList<String>();
		
		for (int i=0; i<v.size(); i++){
			addVoiture(v.get(i));
		}
		
		for (int i=0; i<o.size(); i++) {
			addObjet(o.get(i));
		}
	}
	
	public static void addVoiture(Voiture v) {
		voitures.add(v);
		double cercle_radius = v.radius;
		double pX = v.position.getX()+tailleX/2;
		double pY = v.position.getY()+tailleY/2;
		
		Line l = new Line(pX, pY, pX + Math.cos(v.direction) * cercle_radius , pY - Math.sin(v.direction) * cercle_radius);
		l.setStroke(couleurs_ligne[voitures.size()%couleurs_ligne.length]);
  		l.setStrokeWidth(7);
  		avant_vehicule.add(l);
  		
  		couleurs_voitures.add(couleurs[voitures.size()%couleurs.length]);
  		
  		Circle cercle = new Circle();
  		cercle.setCenterX(pX);
    	cercle.setCenterY(pY);
    	cercle.setRadius(cercle_radius);
    	cercle.setFill(couleurs_voitures.get(voitures.size()-1));
  		cercle.setStroke(Color.DARKGRAY);
    	cercle.setStrokeWidth(4);
    	
  		cercles.add(cercle);
  		
  		Text nom_area = new Text();
  		nom_area.setText(v.nom +" : "+v.score); 
        nom_area.setX(cercle.getCenterX() - 30); 
        nom_area.setY(cercle.getCenterY() - 10 - cercle.getRadius());
        nom_area.setFill(Color.DARKVIOLET); 
        nom_area.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 15));
        zone_nom.add(nom_area);
	}
	
	public static void addObjet(Objet o) {
		objets.add(o);
		double cercle_radius = o.getRadius();
		double pX = o.getPosition().getX()+tailleX/2;
		double pY = o.getPosition().getY()+tailleY/2;
		
		Circle cercle = new Circle();
  		cercle.setCenterX(pX);
    	cercle.setCenterY(pY);
    	cercle.setRadius(cercle_radius);
    	if (o.isObjectif()) {
    		cercle.setFill(Color.YELLOW);
    		cercle.setStroke(Color.ORANGE);
    	}else {
    		cercle.setFill(Color.DARKGRAY);
    		cercle.setStroke(Color.DARKGRAY);
    	}
    	cercle.setStrokeWidth(4);
    	
  		ronds.add(cercle);
	}
	
	public Voiture getVoiture(String nom) {
		for (int i=0; i<voitures.size(); i++) 
			if (voitures.get(i).nom.equals(nom)) 
				return voitures.get(i);
		
		return null;
	}
	
	
	public double distance(Point2D p, Point2D q) {
		double d = (p.getX()-q.getX()) * (p.getX()-q.getX()) + (p.getY()-q.getY()) * (p.getY()-q.getY());
		return d;
	}
	
	public boolean collision(Voiture v, Objet o) {
		return distance(nextPosition(v), o.getPosition()) <= v.radius + o.getRadius() * (v.radius + o.getRadius());
	}
	
	public boolean collision(Voiture v1, Voiture v2) {
		Point2D p = nextPosition(v1);
		Point2D q = nextPosition(v2);
		return distance(p, q) <= (v1.radius + v2.radius) * (v1.radius + v2.radius);		
	}
	
	public Point2D relocate (Point2D p) {
		double pX = p.getX()+tailleX/2;
		double pY = p.getY()+tailleY/2;
		
		return new Point2D(pX, pY);
	}
	
	public Point2D nextPosition(Voiture v) {
		double posX = v.position.getX() + v.vitesse.getX();
		double posY = v.position.getY() + v.vitesse.getY();
		
		if (posX > tailleX/2)
			posX = posX - tailleX;
		else if (posX < -tailleX/2)
			posX = posX + tailleX;
		
		if (posY > tailleY/2)
			posY = posY - tailleY;
		else if (posY < -tailleY/2)
			posY = posY +tailleY;
		
		Point2D res = new Point2D(posX, posY);
		return res;
	}
	
	public void move() {
		boolean [] vaSeCogner = new boolean [voitures.size()];
		
		for (int i=0; i<vaSeCogner.length; i++) 
			vaSeCogner[i] = false;
		
		for (int i=0; i<voitures.size(); i++) {
			for (Objet o : objets) {
				if (!o.isObjectif() && collision(voitures.get(i), o))
					vaSeCogner[i] = true;
			}
		}
		
		for (int i=0; i<voitures.size()-1; i++) {
			for (int j=i+1; j<voitures.size(); j++) {
				if (collision(voitures.get(i), voitures.get(j)))
					vaSeCogner[i] = true;
					vaSeCogner[j] = true;
			}
		}
		
		for (int i=0; i<vaSeCogner.length; i++) {
			if (vaSeCogner[i]) {
				voitures.get(i).reverseVitesse();
			}
			
			Point2D	new_pos_v = nextPosition(voitures.get(i));
			Point2D new_pos_c = relocate(voitures.get(i).position);
			
			voitures.get(i).position = new_pos_v;
			
			double posX = new_pos_c.getX();
			double posY = new_pos_c.getY();
			
			cercles.get(i).setCenterX(posX);
			cercles.get(i).setCenterY(posY);
			
			avant_vehicule.get(i).setStartX(posX);
    		avant_vehicule.get(i).setStartY(posY);
    		avant_vehicule.get(i).setEndX(posX + Math.cos(voitures.get(i).direction) * cercles.get(i).getRadius());
    		avant_vehicule.get(i).setEndY(posY - Math.sin(voitures.get(i).direction) * cercles.get(i).getRadius());
    		
    		zone_nom.get(i).setX(posX - 30);
    		zone_nom.get(i).setY(posY - 10 - cercles.get(i).getRadius());
    		zone_nom.get(i).setText(voitures.get(i).nom +" : "+ voitures.get(i).score);
    		
		}
		
	}
	
	private EventHandler<KeyEvent> keyPressed = new EventHandler<KeyEvent>() {
    	@Override 
        public void handle(KeyEvent event) {
            switch (event.getCode()) {
                case Z:
                	getVoiture(nom).thrust();
                	client.addInputs("z");
                    break;
                case Q:
                	getVoiture(nom).clock();
                	client.addInputs("q");
                	break;
                case D:
                	getVoiture(nom).anticlock();
                	client.addInputs("d");
                    break;
                case ESCAPE:
                	client.addInputs("EXIT");
                	try {
                		Thread.sleep(500);
                		Platform.exit();
                	} catch (InterruptedException e) {
                		// TODO Auto-generated catch block
                		e.printStackTrace();
                	}
                default:
                	break;
            }
        }
    };
	
    private AnimationTimer timer = new AnimationTimer() {
		@Override
	    public void handle(long now) {
	        move();
	        getVoitures();
	        getObjets();
	        try {
				Thread.sleep(1000/refresh_tickrate);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
    
	@Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, tailleX, tailleY, Color.LIGHTBLUE);
		
		primaryStage.setTitle("Asteroids");
        primaryStage.setScene(scene);
        primaryStage.show();
         
        for (Circle c : cercles)
        	root.getChildren().add(c);
        
        for (Circle o : ronds)
        	root.getChildren().add(o);
        
        for (Line l : avant_vehicule)
        	root.getChildren().add(l);
        
        for (Text t : zone_nom)
        	root.getChildren().add(t);
        
       	timer.start();
       	scene.addEventHandler(KeyEvent.KEY_PRESSED , keyPressed);
       	
    }
	
	public static void sendInputs() {
		synchronized(inputAEnvoyer) {
			client.addInputs(inputAEnvoyer);
			inputAEnvoyer.clear();
		}
	}
	
	public static void getVoitures() {
		ArrayList<Voiture> liste = client.getVoitures();
		synchronized(voitures) {
			for (Voiture v : voitures) {
				for(Voiture l: liste) {
					if (v.nom.equals(l.nom)){
						v = l;
					}
				}
			}
		}
	}
	
	public static void getObjets() {
		ArrayList<Objet> liste = client.getObjets();
		synchronized(objets) {
			objets = liste;
		}
	}
	
	public static void main(String[] args) {
		Object demarrer = new Object();
		
		client = new Client(args, demarrer, serveur_tickrate);
		Thread t_client = new Thread(client);
		t_client.start();
		try {
			synchronized(demarrer) {
				demarrer.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		init(client.getNom(), client.getVoitures(), client.getObjets());
        launch(args);
    }

}
