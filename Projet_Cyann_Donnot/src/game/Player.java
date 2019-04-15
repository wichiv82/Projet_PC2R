package game;


import javafx.scene.paint.Paint;


public class Player extends ArenaObject{
	
	public double vx, vy;
	public int score, t;
	public boolean stun;
	public boolean active = true;
	
	
	public Player(Arena arena, String name, int radius,Paint fill, Paint stroke) {
		super(arena,name);
		this.setRadius(radius);
		this.setFill(fill);
		this.setStroke(stroke);
		this.setStrokeWidth((int)radius*0.1);
		this.setCenterX(this.arena.width*random.nextDouble());
    	this.setCenterY(this.arena.height*random.nextDouble());
		this.vx = 0;
		this.vy = 0;
		this.t = 0;	
		this.stun = false;
	}
	
	public void active_bombe() {
		arena.bomb.get(name).setVisible(true);
	}
	
	public void unactive_bombe() {
		arena.bomb.get(name).setVisible(false);
	}
	
	public void collision() {
		boolean hurt = false;
		for(IArenaObject o : this.arena.values()) {
			if (o.getName().contains("obstacle")) {
				Obstacle obstacle = (Obstacle)o;
				if ((obstacle.getRadius() + this.getRadius())*(obstacle.getRadius() + this.getRadius()) 
						> this.square_of_distance(obstacle)){
					hurt = true;
					this.stun = true;
				}
				continue;
			}
			if (!o.getName().contains("objectif")) {
				if (o.getName().equals(this.name)) continue;
				Player player = (Player)o;
				if (!player.active) continue;
				if ((player.getRadius() + this.getRadius())*(player.getRadius() + this.getRadius()) 
						> this.square_of_distance(player)){
					hurt = true;
					this.stun = true;
				}
			}
		}
		for(PlayerBomb b : this.arena.bomb.values()) {
			if (b.isVisible() && b != this.arena.bomb.get(name) && !stun) {
				if ((b.getRadius() + this.getRadius())*(b.getRadius() + this.getRadius()) 
						> this.square_of_distance(b)){
					hurt = true;
					this.stun = true;
					System.out.println("HURT");
				}
			}
		}
		if (hurt) {
			this.vx = -this.vx*2;
			this.vy = -this.vy*2;
		}
	}
	
	public void refresh_from(String s) {
		String[] parse = s.split(":X|VX|VY|Y|T");
		this.setCenterX(arena.width/2 + Double.parseDouble(parse[1]));
		this.setCenterY(arena.height/2 + Double.parseDouble(parse[2]));
		this.vx = Double.parseDouble(parse[3]);
		this.vy = Double.parseDouble(parse[4]);
		if (parse[5].contains("."))
			this.t = (int)Double.parseDouble(parse[5]);
		else
			this.t = Integer.parseInt(parse[5]);
	}
	
	
	public void move() {
		this.setCenterX((this.getCenterX() + this.vx)%(this.arena.width));
		this.setCenterY((this.getCenterY() + this.vy)%(this.arena.height));
		if (this.getCenterX() < 0)
			this.setCenterX(this.arena.width + this.getCenterX());
		if (this.getCenterY() < 0)
			this.setCenterY(this.arena.height + this.getCenterY());
		
	}
	
	public void speed_up(double thrust) {
		double sum = Math.abs(this.vx + thrust*Math.cos(Math.toRadians(t))) + Math.abs(this.vy + thrust*Math.sin(Math.toRadians(t)));
		if (sum < MAX_SPEED) {
			this.vx = this.vx + thrust*Math.cos(Math.toRadians(t));
			this.vy = this.vy + thrust*Math.sin(Math.toRadians(t));
		} else {
			this.vx = MAX_SPEED * (this.vx + thrust*Math.cos(Math.toRadians(t)))/sum;
			this.vy = MAX_SPEED * (this.vy + thrust*Math.sin(Math.toRadians(t)))/sum;
		}
	}
	
	public void turn(int intensity) {
		this.t = (this.t + intensity*TURNIT)%360;
	}
}
