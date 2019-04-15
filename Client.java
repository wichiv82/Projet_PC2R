import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import javafx.geometry.Point2D;

class CommunicationEnvoiServeur implements Runnable {
	private DataOutputStream writer;
	private ArrayList<String> inputs;
	private int serveur_tickrate;
	private String nom;
	
	CommunicationEnvoiServeur(DataOutputStream output, String name, int ST){
		writer = output;
		nom = name;
		serveur_tickrate = ST;
		inputs = new ArrayList<String>();
	}
	
	public void addInputs(ArrayList<String> touches) {
		synchronized(inputs){
			inputs.addAll(touches);
		}
	}
	
	public void addInputs(String touche) {
		synchronized(inputs){
			inputs.add(touche);
		}
	}
	
	public void run(){
		try {
			int rotation = 0;
			int thrust = 0;
			String messageAEnvoyer;
			boolean messageDeplacement = true;;
			
			while(messageDeplacement) {
				messageAEnvoyer = "A0T0";
				try {
					Thread.sleep(1000/serveur_tickrate);
				}catch(InterruptedException ex){
    				Thread.currentThread().interrupt();
				}
				
				synchronized(inputs){
					for (String cmd : inputs){
						switch(cmd){
							case "q" :
								rotation--;
								break;
							case "d" :
								rotation++;
								break;
							case "z" :
								thrust++;
								break;
							case "EXIT":
								messageDeplacement = false;
								messageAEnvoyer = "EXIT/"+ nom +"/";
								break;
							default :
								break;
						}
					}
					inputs.clear();
				}					
				
				if (!messageDeplacement) {
					writer.write((messageAEnvoyer+"\n").getBytes("ASCII"));
					writer.flush();
					System.out.println("TERMINUS !!!!!");
				}else{
					messageAEnvoyer = "A" + rotation + "T"+ thrust;
					writer.write(("NEWCOM/" + messageAEnvoyer + "/\n").getBytes("ASCII"));
					writer.flush();
					System.out.println("Message envoyé : " + messageAEnvoyer);
				}
				 
				rotation = 0;
				thrust = 0;
				
			}
		}catch (IOException e) {
			System.err.println(e);
		}
	}

}


class CommunicationReceptionServeur implements Runnable {
	private Object demarrer;
	private BufferedReader reader;
	
	private ArrayList<Voiture> voitures;
	private ArrayList<Objet> objets;
	
	CommunicationReceptionServeur(BufferedReader read, Object demarrer){
		reader = read;
		voitures = new ArrayList<Voiture>();
		objets =  new ArrayList<Objet>();
		this.demarrer = demarrer;
	}
	
	public Voiture getVoiture(String n) {
		for (int i=0; i<voitures.size(); i++) 
			if (voitures.get(i).nom.equals(n)) 
				return voitures.get(i);
		
		return null;
	}
	
	
	public synchronized ArrayList<Voiture> getVoitures() {
		return voitures;
	}
	
	public Objet getObjectif() {
		for (Objet o : objets)
			if (o.isObjectif())
				return o;
		return null;
	}
	
	public synchronized ArrayList<Objet> getObjets() {
		return objets;
	}
	
	public void removeObjet(Objet o){
		objets.remove(o);
	}
	
	public void run(){
		try {
			String ligne = new String();
						
			while(true) {
				
				ligne = reader.readLine();
				System.out.println("Serveur : "+ligne);
				
				String [] parts = ligne.split("/");
				
				switch(parts[0]) {
					case "NEWPLAYER":
						if (parts.length == 3)
							System.out.println(parts[1] +" s'est connecté");
						break;
					case "SESSION":
						if (parts.length == 4) {
							synchronized(voitures) {
								synchronized(objets) {
									String [] coords = parts[1].split("\\|");
									for (int i=0; i<coords.length; i++) {
										String nom_v = coords[i].split(":X")[0];
										String[] infos = coords[i].split(":X")[1].split("Y|VX|VY|T");
										Point2D pos = new Point2D(Double.parseDouble(infos[0]), Double.parseDouble(infos[1]));
										Point2D vit = new Point2D(Double.parseDouble(infos[2]), Double.parseDouble(infos[3]));
										
										voitures.add(new Voiture(nom_v, pos, vit));
										
									}
									
									String[] coord = parts[2].substring(1).split("Y");
									Point2D pos = new Point2D(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
									Piece p = new Piece(pos, 20);
									objets.add(p);
									
									String[] obstacles = parts[3].split("\\|");
									for (String obs : obstacles) {
										String[] coord_obs = obs.substring(1).split("Y");
										Point2D position_obs = new Point2D(Double.parseDouble(coord_obs[0]), Double.parseDouble(coord_obs[1]));
										Obstacle o = new Obstacle(position_obs, 70);
										objets.add(o);
									}
								}
							}
							
							synchronized(demarrer) {
								demarrer.notifyAll();
							}
						}
						break;
					case "WINNER":
						if (parts.length == 2) {
							String[] scores = parts[1].split("\\|");
							for (String s : scores) {
								String[] maj_score = s.split(":");
								getVoiture(maj_score[0]).score = Integer.parseInt(maj_score[1]); 
								System.out.print(s+" |");
							}
							System.out.println();
						}
						break;
					case "TICK":
						if (parts.length == 2) {
							synchronized(voitures) {
								String [] coords = parts[1].split("\\|");
								for (int i=0; i<coords.length; i++) {
									String nom_v = coords[i].split(":X")[0];
									String[] infos = coords[i].split(":X")[1].split("Y|VX|VY|T");
									Point2D pos = new Point2D(Double.parseDouble(infos[0]), Double.parseDouble(infos[1]));
									Point2D vit = new Point2D(Double.parseDouble(infos[2]), Double.parseDouble(infos[3]));
									
									getVoiture(nom_v).position = pos;
									getVoiture(nom_v).vitesse = vit;
									getVoiture(nom_v).direction = Double.parseDouble(infos[4]);
								}
							}
						}
						break;
					case "NEWOBJ":
						if (parts.length == 3) {
							synchronized(objets) {
								removeObjet(getObjectif());
								String[] coord = parts[1].substring(1).split("Y");
								
								Point2D pos = new Point2D(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
								Piece p = new Piece(pos, 20);
								objets.add(p);
								
								String[] scores = parts[2].split("\\|");
								for (String s : scores) {
									String[] maj_score = s.split(":");
									getVoiture(maj_score[0]).score = Integer.parseInt(maj_score[1]); 
									System.out.print(s+" |");
								}
								System.out.println();
							
							}
						}
				}
			}
			
		}catch (IOException e) {
			System.err.println(e);
		}
	}
}

public class Client implements Runnable{
	private int PORT;
	private String nom;
	private Object demarrer;
	private Socket s=null;
	private char ENTER = '\n';
	
	private CommunicationEnvoiServeur envoi;
	private CommunicationReceptionServeur reception;
	private int serveur_tickrate;
	
	public Client(String[] args, Object demarrer, int serveur_tickrate) {
		if (args.length < 1) {
			System.err.println("Usage: java Client <hote>");
		    System.exit(1); 
		}
		
		if(args.length >= 2)
			PORT = Integer.parseInt(args[1]);

		try { 
			s = new Socket (args[0],PORT);
			System.out.println("Connexion etablie : "+ s.getInetAddress()+" port : "+s.getPort());
			
			this.demarrer = demarrer;
			this.serveur_tickrate = serveur_tickrate;
			
		}catch (IOException e) {
			System.err.println(e);
		}
		
			
	}
	
	public synchronized ArrayList<Voiture> getVoitures(){
		return reception.getVoitures();
	}
	
	public synchronized ArrayList<Objet> getObjets(){
		return reception.getObjets();
	}
	
	public synchronized String getNom(){
		return nom;
	}
	
	public void addInputs(ArrayList<String> touches) {
		envoi.addInputs(touches);
	}
	
	public void addInputs(String touche) {
		envoi.addInputs(touche);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			DataOutputStream writer = new DataOutputStream(s.getOutputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		
			char c;
			String ligne = new String();
			boolean nom_valide = false;
			while(!nom_valide) {
				System.out.println("Entrez votre pseudo : ");
				while ((c=(char)System.in.read()) != ENTER)
						ligne=ligne+c;
				nom = new String(ligne);
				ligne = "CONNECT/"+ ligne +"/";
				
				writer.write((ligne).getBytes("ASCII"));
				writer.flush();
				
				System.out.println("Envoi au serveur " + ligne);
				
				ligne = reader.readLine();
				System.out.println(ligne);
				String [] parts = ligne.split("/");
				if(parts[0].equals("WELCOME")) {
					nom_valide = true;
					String[] all_names = parts[2].split("\\|");
					nom = all_names[all_names.length-1].split(":")[0];
					System.out.println("NOM = " + nom);
				}	
			}
		
		
		
			synchronized(nom) {
				envoi = new CommunicationEnvoiServeur(writer, nom, serveur_tickrate);
				synchronized(demarrer) {
					reception = new CommunicationReceptionServeur(reader, demarrer);
				}
			}
		
			Thread t_envoi = new Thread(envoi);
			Thread t_reception = new Thread(reception);
			t_reception.start();
			
			try {
				synchronized(demarrer) {
					demarrer.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			t_envoi.start();
			
			try {
				t_envoi.join();
				t_reception.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}catch (IOException e) {
			System.err.println(e);
		}finally {
			try {
				s.close();
				System.out.println("Disconnected !");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	
}