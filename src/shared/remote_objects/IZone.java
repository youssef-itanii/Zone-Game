package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Player;


public interface IZone extends Remote{
	void register(IClient client, int row , int col) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	int getID() throws RemoteException;
	
	void placePlayer(IClient client, int x, int y) throws RemoteException;
	String updateCoordinates(IClient client, Player.Direction direction) throws RemoteException;
	String updateCoordinates(IClient client,int x , int y) throws RemoteException;

	String movePlayer(IClient client, Player.Direction direction) throws RemoteException;
	void recieveMessage(String message) throws RemoteException;
	void setPosition(int index) throws RemoteException;
	void registerNeighbouringZone()throws RemoteException;
	boolean cellIsEmpty(int row, int col) throws RemoteException;

}