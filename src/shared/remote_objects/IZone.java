package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Player;


public interface IZone extends Remote{
	//Register client and place in zone
	void register(IClient client, int row , int col) throws RemoteException;
	
	//Remove client from zone
	void unregister(IClient client) throws RemoteException;
	
	//Return Zone ID
	int getID() throws RemoteException;
	
	//Place a player in a specific location within the zone
	void placePlayer(IClient client, int x, int y) throws RemoteException;

	//Move the player within/between the zones and return the updated map
	String movePlayer(IClient client, Player.Direction direction) throws RemoteException;
	
	//Set zone index in the zone array stored in the manager node
	void setPosition(int index) throws RemoteException;
	
	//Check if cell is empty
	boolean cellIsEmpty(int row, int col) throws RemoteException;

}