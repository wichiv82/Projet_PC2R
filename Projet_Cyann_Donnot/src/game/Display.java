package game;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Display extends Application{

	public static void main(String[] args) {
        launch(args);
    }
	
	String commande = "A0T0";
	boolean running = true;
	boolean Z = false;
	boolean Q = false;
	boolean S = false;
	boolean D = false;
	boolean B = false;
	
	public Player player;
	public Arena arena;
	
	public int PLAYERS_SIZE = 30;
	public int HEIGHT = 1000;
	public int WIDTH = 1000;
	public int REFRESH_TICKRATE = 33; // 30 ips
	public int SERVER_REFRESH_TICKRATE = 33; // 10 ips
	public boolean session = false;
	
	public Socket socket;
	public BufferedReader input;
	public DataOutputStream output;
	public String HOST = "localhost";
  	public int PORT = 10029;
  	
  	public Stage stage;
  	public Group root;
  	public Scene scene;
  	public Circle bombe;
  	
	
	private EventHandler<KeyEvent> keyPressed = new EventHandler<KeyEvent>() {
        @SuppressWarnings("incomplete-switch")
		public void handle(KeyEvent event) {
            switch (event.getCode()) {
                case Z:
                	Z = true;
                    break;
                case Q:
                	Q = true;
                    break;
                case D:
                	D = true;
                    break;
                case SPACE:
                	player.active_bombe();
                	try {
    					output.write(("CHAMPON/" + player.name + "/").getBytes("ASCII"));
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				} catch (IOException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
                	break;
                case ESCAPE:
					try {
						output.write(("EXIT/" + player.name + "/\n").getBytes("ASCII"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	running = false;
                	stage.close();
            }
        }
    };
    
    private EventHandler<KeyEvent> keyReleased = new EventHandler<KeyEvent>() {
        @SuppressWarnings("incomplete-switch")
		public void handle(KeyEvent event) {
        	//System.out.println(event.getCode() + "");
            switch (event.getCode()) {
	            case Z:
	            	Z = false;
	                break;
	            case Q:
	            	Q = false;
	                break;
	            case D:
	            	D = false;
	                break;
	            case SPACE:
	            	player.unactive_bombe();
	            	try {
						output.write(("CHAMPOFF/" + player.name + "/").getBytes("ASCII"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	break;
            }
        }
    };
	
    Service<Void> GameService = new Service<Void>(){
  	  protected Task<Void> createTask() {
  	    return new Task<Void>(){

  	     protected Void call() throws Exception {
  	    	String[] parse;
  	    	long time_to_send = System.currentTimeMillis();
  	    	
  	        while(true) {
  	        	long start = System.currentTimeMillis();
  	        	if(Z && !player.stun) {
  	        		player.speed_up(0.33);
  	        		parse = commande.split("A|T");
  	        		commande = "A" + parse[1] + "T" + (Integer.parseInt(parse[2]) + 1);
  	        	}
  	        	if(Q) {
  	        		player.turn(-1);
  	        		parse = commande.split("A|T");
  	        		commande = "A" + (Double.parseDouble(parse[1]) - 1) + "T" + parse[2];
  	        	}
  	        	if(D) {
  	        		player.turn(1);
  	        		parse = commande.split("A|T");
  	        		commande = "A" + (Double.parseDouble(parse[1]) + 1) + "T" + parse[2];
  	        	}
  	        	
  	        	//System.out.println("X" + player.getCenterX() + "Y" + player.getCenterY() + "VX" + player.vx + "VY" + player.vy);
  	        	player.stun = false;
  	        	for(ArenaObject o : arena.values())
  	        		o.collision();
  	        	arena.move();
  	        	if(System.currentTimeMillis() - time_to_send > SERVER_REFRESH_TICKRATE) {
  	        		output.write(("NEWCOM/" + commande + "/\n").getBytes("ASCII"));
  	        		commande = "A0T0";
  	        		time_to_send = System.currentTimeMillis();
  	        	}
  	        	
  	        	Thread.sleep(Integer.max(0, (int) (REFRESH_TICKRATE - (System.currentTimeMillis() - start))));
  	        }
  	      }
  	    };
  	  }
  	};
  	
  	
  	class ClientInput implements Runnable{

		@Override
		public void run() {
			while (running) {
					String line = "";
					try {
						line = input.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					String[] parse = line.split("/");
					//System.out.println(line);
					//System.out.println("X" + player.getCenterX() + "Y" + player.getCenterY());
					switch(parse[0]) {
						case "NEWPLAYER":
							if (parse.length == 3) {
								System.out.println("NEWPLAYER");
								System.out.println(parse[1] +" is connected");
								arena.add_player(new Player(arena,parse[1],PLAYERS_SIZE, Color.RED, Color.DARKGRAY));
							} continue;
						case "SESSION":
							if (parse.length == 4) {
								System.out.println("SESSION START");
								synchronized (arena) {
									arena.session(parse);
								}
								session = true;
							} continue;
						case "TICK":
							if(!session) continue;
							if (parse.length == 2) {
								//System.out.println("TICK");
								synchronized (arena) {
									arena.tick(parse[1].split("\\|"));
								}
							}continue;
						case "NEWOBJ":
							if (parse.length == 3) {
								System.out.println("OBJECTIF");
								synchronized (arena) {
									Objectif objectif = (Objectif)arena.get("objectif");
									objectif.refresh_from(parse[1]);
									arena.scores_from(parse[2].split("\\|"));
									
									continue;
								}
							}continue;
						case "WINNER": 
							System.out.println(parse[1]);
							continue;
						case "CHAMPON":
							Player on = (Player)arena.get(parse[1]);
							on.active_bombe();
							break;
						case "CHAMPOFF":
							Player off = (Player)arena.get(parse[1]);
							off.unactive_bombe();
							break;
						case "PLAYERLEFT":
							arena.get(parse[1]).setVisible(false);
							arena.dir.get(parse[1]).setVisible(false);
							Player byebye = (Player)arena.get(parse[1]);
							byebye.active = false;
							break;
					}
			}
			
		}
  	}
  	
  	Service<Void> ClientInputService = new Service<Void>(){
  		protected Task<Void> createTask() {
  			return new Task<Void>(){
  				protected Void call() {
  					ClientInput input_stream = new ClientInput();
  					Thread is = new Thread(input_stream);
  					is.start();
					return null;
  				}
  			};
  		}
  	};
  	
  	
  	
  	
  	
  	public void connect() throws UnknownHostException, IOException {
  		
		socket = new Socket (HOST,PORT);
		System.out.println("Connexion etablie : "+ socket.getInetAddress()+" port : "+socket.getPort());

		output = new DataOutputStream(socket.getOutputStream());
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
  	}

  	public void lobby() throws UnsupportedEncodingException, IOException {
  		char c;
  		
  		boolean connection_validated = false;
  		while(!connection_validated) {
  			String name = new String();
  			System.out.println("What's your name ?");
  			while ((c=(char)System.in.read()) != '\r') name = name + c;
			output.write(("CONNECT/" + name + "/").getBytes("ASCII"));
			output.flush();
			
			System.out.println("Send to server " + ("CONNECT/" + name + "/"));
	  		
			String line = input.readLine();
	  		String[] parse = line.split("/");
	  		if(parse[0].equals("WELCOME")) {
	  			connection_validated = true;
	  			System.out.println(line);
	  			player = new Player(arena,name,30,Color.AQUA,Color.DARKGREY);
	  			arena.add_player(player);
	  	        Objectif objectif = new Objectif(arena,10,1);
	  	        objectif.refresh_from(parse[3]);
	  	        arena.put(objectif.getName(), objectif);
	  	        String[] parse_obstacles = parse[4].split("\\|");
	  	        for(int i = 0; i < parse_obstacles.length; i++) {
	  	        	arena.put("obstacle" + i, new Obstacle(arena,70));
	  	        	arena.get("obstacle" + i).refresh_from	(parse_obstacles[i]);
	  	        }
	  		}
	  		System.out.println("Wait for starting session");
	  		line = input.readLine();
	  		System.out.println(line);
	  		arena.session(line.split("/"));
	  		session = true;
  		}
  }
  	
  	
  	
  	
  	public void start(Stage primaryStage) throws Exception {
		
  		
		connect();
		stage = primaryStage;
		root = new Group();
    	scene = new Scene(root,WIDTH,HEIGHT,Color.LIGHTGREY);
    	primaryStage.setScene(scene);
        primaryStage.show();
        
        arena = new Arena(WIDTH, HEIGHT);
        lobby();
        
        root.getChildren().addAll(arena.values());
        root.getChildren().addAll(arena.dir.values());
        root.getChildren().addAll(arena.bomb.values());
        
        scene.addEventHandler(KeyEvent.KEY_PRESSED , keyPressed);
    	scene.addEventHandler(KeyEvent.KEY_RELEASED, keyReleased);
    	
    	ClientInputService.start();
    	GameService.start();
    	
		
	}

}
