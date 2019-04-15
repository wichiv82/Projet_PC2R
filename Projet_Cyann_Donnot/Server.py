
# coding: utf-8

# In[1]:


from threading import Thread, Lock
import socket
import time
from random import random, choice
from math import cos, sin, pi, radians, degrees


# ## Utils

# In[2]:


def clean_message(message):
    if isinstance(message,bytes):
        message = message.decode("ascii")
   # message = message.replace('\x00', '')
   # message = message.replace('\n', '')
    message = message.replace('\r','')
    return message


# ## Client Management

# In[93]:


PROTOCOL = {
    #Connection
    "CLIENT CONNECTED" : "CONNECT",
    "CONNECTION VALIDATED" : "WELCOME",
    "CONNECTION REFUSED" : "DENTED",
    "SIGNAL CONNECTION" : "NEWPLAYER",
    
    #Disconnection
    "CLIENT DISCONNECTION" : "EXIT",
    "SIGNAL DISCONNECTION" : "PLAYERLEFT",
    
    #Start Session
    "START SESSION" : "SESSION",
    "END SESSION" : "WINNER",
    
    #Game
    "NEW COORDINATES" : "NEWPOS",
    "ALL COORDINATES" : "TICK",
    "NEW OBJECTIF" : "NEWOBJ",
    "NEW COMMANDES" : "NEWCOM",
    
    #Extensions
    "FORCE CHAMP ON" : "CHAMPON",
    "FORCE CHAMP OFF" : "CHAMPOFF"
}


# In[94]:


class ClientThread(Thread):
    
    def __init__(self, connection, address, repertory,game):
        Thread.__init__(self)
        self.connection = connection
        self.address = address
        self.lock = Lock()
        self.repertory = repertory
        self.game = game
        self.commande = "A0T0"
        self.name = None
        self.active = True
        
    def run(self):
        while self.active:
            message = clean_message(self.connection.recv(1024)).split('/')
            if message[0] == PROTOCOL["CLIENT CONNECTED"]:
                if message[1] in self.repertory.names():
                    self.connection.send(PROTOCOL["CONNECTION REFUSED"] + '/')
                else:
                    self.name = message[1]
                    if self.game.lobby:
                        self.connection.send((PROTOCOL["CONNECTION VALIDATED"] + "/attente/" 
                                            + self.game.arena.scores() + '/' 
                                            + str(self.game.arena.objectif) + '/' 
                                            + "|".join([str(obstacle) for obstacle in self.game.arena.obstacles])
                                            + "/\n").encode("ascii"))
                    else:
                        self.connection.send((PROTOCOL["CONNECTION VALIDATED"] + "/jeu/" 
                                            + self.game.arena.scores() + '/' 
                                            + str(self.game.arena.objectif) + '/'
                                            + "|".join([str(obstacle) for obstacle in self.game.arena.obstacles])
                                            + "/\n").encode("ascii"))
                    self.repertory.send_to_all(PROTOCOL["SIGNAL CONNECTION"] + '/' + self.name + "/\n")
            if message[0] == PROTOCOL["CLIENT DISCONNECTION"]  or message == ['']:
                self.game.arena[self.address].active = False; 
                self.name = self.repertory.remove(self)
                self.active = False
            if message[0] == PROTOCOL["NEW COMMANDES"]:
                self.lock.acquire()
                self.commande = ("A" + str(float(self.commande.split('T')[0][1:]) 
                                     + float(message[1].split('T')[0][1:]))
                                +"T" + str(float(self.commande.split('T')[1]) 
                                     + float(message[1].split('T')[1])))
                self.lock.release()
            if message[0] == PROTOCOL["NEW COORDINATES"]: None
            if message[0] == PROTOCOL["FORCE CHAMP ON"]:
                self.repertory.send_to_all("CHAMPON/" + self.name + "/\n")
            if message[0] == PROTOCOL["FORCE CHAMP OFF"]:
                self.repertory.send_to_all("CHAMPOFF/" + self.name + "/\n")


# In[107]:


class ClientRepertory(dict):
    
    def __init__(self):
        dict.__init__(self)
        
    def address(self):
        return self.keys()
    
    def clients(self):
        return list(self.values())
    
    def names(self):
        return [client.name for client in self.clients()]
    
    def send_to_all(self, String):
        if not isinstance(String,bytes):
            String = String.encode("ascii")
        for client in self.clients():
            if (client.name + ':' in String.decode("ascii") 
            or PROTOCOL["SIGNAL DISCONNECTION"] in String.decode("ascii")
            or PROTOCOL["FORCE CHAMP ON"] in String.decode("ascii")
            or PROTOCOL["FORCE CHAMP OFF"] in String.decode("ascii")):
                client.connection.send(String)
    
    
    def remove(self,client):
        print("REMOVE :",client)
        client.connection.close()
        name_client = client.name
        del self[client.address] 
        self.send_to_all(PROTOCOL["SIGNAL DISCONNECTION"] + "/" 
                    + name_client + "/\n")
        print(PROTOCOL["SIGNAL DISCONNECTION"] + "/" 
                    + name_client + "/\n")


# ## Game 

# In[108]:


SERVER_REFRESH_TICKRATE = 0.033
SERVER_TICKRATE = 0.1
MAX_SPEED = 6
VALUES_OF_OBJECTIF = [1]
SIZES_OF_OBJECTIF = [10]
SIZE_OF_PLAYERS = 30
SIZES_OF_OBSTACLES = [70]
NUMBER_OF_OBSTACLES = [3]
TURNIT = 6


# In[109]:


class vector:
    
    def __init__(self):
        self.x = 0
        self.y = 0
        
    def __str__(self):
        return "VX" + str(self.x) + "VY" + str(self.y)


# In[110]:


class Player:
    
    def __init__(self, arena, client):
        self.client = client
        self.arena = arena
        self.score = 0
        self.size = SIZE_OF_PLAYERS
        self.x = 2*arena.width*random() - arena.width
        self.y = 2*arena.height*random() - arena.width
        self.v = vector()
        self.t = 0
        self.stun = False;
        self.active = True;
        
    def apply_commande(self):
        self.client.lock.acquire()
        commande = self.client.commande
        self.client.commande = "A0T0"
        self.client.lock.release()
        somme = (abs(self.v.x + float(commande.split('T')[1])*cos(radians(self.t)))
                + abs(self.v.y + float(commande.split('T')[1])*sin(radians(self.t))))
        if (somme < MAX_SPEED):
            self.v.x = self.v.x + float(commande.split('T')[1])*cos(radians(self.t))
            self.v.y = self.v.y + float(commande.split('T')[1])*sin(radians(self.t))
        else:
            self.v.x = MAX_SPEED*(self.v.x + float(commande.split('T')[1])*cos(radians(self.t)))/somme
            self.v.y = MAX_SPEED*(self.v.y + float(commande.split('T')[1])*sin(radians(self.t)))/somme
        self.t = (self.t + float(commande.split('T')[0][1:])*TURNIT)%360

    def move(self):
        self.x = (self.x + self.v.x + self.arena.width)%(self.arena.width*2) - self.arena.width
        self.y = (self.y + self.v.y + self.arena.height)%(self.arena.height*2) - self.arena.height
        
    def collision(self):
        colli = False
        for other_player in self.arena.players():
            if other_player.active == False: continue 
            if other_player.x == self.x and other_player.y == self.y: continue
            if pow(self.size + other_player.size,2) > self.square_of_distance(other_player):
                self.v.x, self.v.y = -self.v.x, -self.v.y
                colli = True
        for obstacle in self.arena.obstacles:
            if pow(self.size + obstacle.size,2) > self.square_of_distance(obstacle):
                self.v.x, self.v.y = - self.v.x, - self.v.y
                colli = True
        return colli
        
    def square_of_distance(self,other):
        return (other.x - self.x)*(other.x - self.x) + (other.y - self.y)*(other.y - self.y)
        
    def pick_up(self, objectif):
        if pow(self.size + objectif.size,2) > self.square_of_distance(objectif):
            self.score += objectif.value
            return True
        return False
        
    def __str__(self):
        name = self.client.name
        if name != None:
            return (name + ":"
                    + "X" + str(self.x) + "Y" + str(self.y)
                    + str(self.v) + "T" + str(self.t))
        else: return ""


# In[111]:


class Objectif:
    
    def __init__(self, arena):
        self.arena = arena
        self.x = 2*arena.width*random() - arena.width
        self.y = 2*arena.height*random() - arena.height
        self.value = choice(VALUES_OF_OBJECTIF)
        self.size = choice(SIZES_OF_OBJECTIF)
        
    def collision(self):
        colli = False
        for player in self.arena.players():
            if player.x == self.x and player.y == self.y: continue
            if pow(self.size + player.size,2) > self.square_of_distance(player):
                colli = True
        for obstacle in self.arena.obstacles:
            if pow(self.size + obstacle.size,2) > self.square_of_distance(obstacle):
                    colli = True
        return colli
    def square_of_distance(self,other):
        return (other.x - self.x)*(other.x - self.x) + (other.y - self.y)*(other.y - self.y)
        
    def __str__(self):
        return 'X' + str(self.x) + 'Y' + str(self.y)


# In[112]:


class Obstacle:
    
    def __init__(self, arena):
        self.x = 2*arena.width*random() - arena.width
        self.y = 2*arena.height*random() - arena.height
        self.size = choice(SIZES_OF_OBSTACLES)
        
    def __str__(self):
        return 'X' + str(self.x) + 'Y' + str(self.y)


# In[113]:


class Bombe:
    
    def __init__(self, arena, x, y, player):
        self.x = x;
        self.y = y;
        self.player = player
        self.arena = arena
    


# In[114]:


class Arena(dict):
    
    def __init__(self, width, height):
        dict.__init__(self)
        self.width = width
        self.height = height
        self.objectif = Objectif(self)
        self.obstacles = [Obstacle(self) for i in range(choice(NUMBER_OF_OBSTACLES))]

       
    def address(self):
        return list(self.keys())
    
    def players(self):
        return list(self.values())
    
    def add_players_from(self, repertory):
        for address in repertory.address():
            print(not address in self)
            if not address in self:
                player = Player(self, repertory[address])
                while player.collision(): player = Player(self, repertory[address])
                self[address] = player
        for address in self.address():
            if not address in repertory.address():
                del self[address]
                
    def apply_commandes(self):
        for player in self.players():
            player.apply_commande()
      
    def move(self):
        for player in self.players():
            player.move()        
    
    def pick_up(self, repertory):
        picked = False
        for player in self.players():
            picked = player.pick_up(self.objectif)
            if picked: 
                self.objectif = Objectif(self)
                while self.objectif.collision(): self.objectif = Objectif(self)
                repertory.send_to_all(PROTOCOL["NEW OBJECTIF"] + '/' 
                                      + str(self.objectif) + '/' + self.scores() + "/\n")
                break
        return picked
    
    def scores(self):
        s = ""
        for player in self.players():
            s += player.client.name + ":" + str(player.score) + "|"
        return s[:-1]
    
    def __str__(self):
        s = ""
        for player in self.players():
            s += str(player) + "|"
        return s[:-1]
                


# In[115]:


class Game(Thread):
    
    def __init__(self, width, height, repertory):
        Thread.__init__(self)
        self.arena = Arena(width, height)
        self.arena.add_players_from(repertory)
        self.repertory = repertory
        self.running = True
        self.lobby = True
        
    def stop(self):
        self.running = False
        
    def run(self):
        time_to_send = time.time()
        while True:
            if self.lobby: continue
            if len(self.repertory) == 0 and not self.lobby: 
                break
                
            time_to_refresh = time.time()
            self.arena.apply_commandes()
            for player in self.arena.players():
                player.collision()
            self.arena.move()
            
            if not self.lobby:
                picked = self.arena.pick_up(self.repertory)
                if picked :
                    self.repertory.send_to_all(PROTOCOL["END SESSION"] + '/' + self.arena.scores() + "/\n")
            if time.time() - time_to_send > SERVER_TICKRATE and not self.lobby:
                self.repertory.send_to_all(PROTOCOL["ALL COORDINATES"] + '/' + str(self.arena) + "/\n")
                time_to_send = time.time()
            time.sleep(max(0, SERVER_REFRESH_TICKRATE - (time.time() - time_to_refresh)))
            
        


# ## Server

# In[116]:


LOBBY_DURATION = 20
WIDTH = 500
HEIGHT = 500
TIMEOUT = 300


# In[117]:


def server(port,host = ''):

    main_connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    main_connection.bind((host, port))
    
    repertory = ClientRepertory()
    game = Game(WIDTH, HEIGHT, repertory)
    game.start()
    
    #Lobby
    print("LOBBY STARTED")
    start = time.time()
    while time.time() - start < LOBBY_DURATION:
        try:
            main_connection.settimeout(max(0,LOBBY_DURATION - (time.time() - start)))
            main_connection.listen(1)
            connection, address = main_connection.accept()
        except socket.timeout:
            if not len(repertory):
                start = time.time()
                repertory = ClientRepertory()
                print("INFINITY LOOP")
                continue
            else: break
        except: raise
        repertory[address] = ClientThread(connection, address, repertory, game)
        repertory[address].start()
        game.repertory = repertory
        game.arena.add_players_from(repertory)
        if len(repertory) == 1: start = time.time()
    print("LOBBY CLOSED")
    print(repertory.address())
    print(game.arena.address())

    print("GAME STARTED")  
    main_connection.settimeout(TIMEOUT)
    repertory.send_to_all(PROTOCOL["START SESSION"] + '/'
                                   + str(game.arena) + '/' + str(game.arena.objectif) + '/'
                                   + "|".join([str(obstacle) for obstacle in game.arena.obstacles])
                                   + "/\n")
    game.lobby = False
    
    game.join()    
    print("GAME OVER")
            
    print("CLOSE ALL CONNECTIONS")
    while len(repertory) != 0:
        address = list(repertory.address())[0]
        repertory[address].active = False
        repertory.remove(repertory[address])
        
    main_connection.close()
    print("END")


# In[ ]:


server(5012,"localhost")

