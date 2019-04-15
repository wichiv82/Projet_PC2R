package game;

import java.util.HashMap;

import javafx.scene.paint.Color;

@SuppressWarnings("serial")
public class Arena extends HashMap<String,ArenaObject> {
	
	public int width, height;
	HashMap<String, PlayerDirection> dir;
	HashMap<String, PlayerBomb> bomb;
	
	
	public Arena(int width, int height){
		super();
		this.width = width;
		this.height = height;
		dir = new HashMap<String, PlayerDirection>();
		bomb = new HashMap<String, PlayerBomb>();
	}
	
	public void add_player(Player player) {
		PlayerDirection direction = new PlayerDirection(player);
		direction.getPoints().addAll(direction.points);
		PlayerBomb bombe = new PlayerBomb(player);
		this.put(player.getName(), player);
		this.dir.put(player.getName(), direction);
		this.bomb.put(player.getName(), bombe);
	}
	
	public void move() {
		for (ArenaObject o : this.values()) {
			if (!o.getName().contains("objectif") && !o.getName().contains("obstacle")) {
				Player player = (Player)o;
				player.move();
				dir.get(player.getName()).move();
				bomb.get(player.getName()).move();
			}
		}
	}

	public void tick(String[] parse) {
		for(String s : parse) {
			//System.out.println(s);
			String name = s.split(":")[0];
			if (!this.containsKey(name) && name != "None")
				this.add_player(new Player(this,name,30,Color.RED, Color.DARKGRAY));
			Player player = (Player)this.get(name);
			player.refresh_from(s);
			
		}
	}
	
	public void session(String[] parse) {
		String[] parse_player = parse[1].split("\\|");
		this.tick(parse_player);
		Objectif objectif = (Objectif) this.get("objectif");
		objectif.refresh_from(parse[2]);
		String[] parse_obstacle = parse[3].split("\\|");
		for(int i = 0; i < parse_obstacle.length; i++) {
			Obstacle obstacle = (Obstacle) this.get("obstacle" + i);
			obstacle.refresh_from(parse_obstacle[i]);
		}
		
	}

	public void scores_from(String[] parse) {
		for(String s : parse) {
			Player player = (Player)this.get(s.split(":")[0]);
			player.score = Integer.parseInt(s.split(":")[1]);
		}
	}
}