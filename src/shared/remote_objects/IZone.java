package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Player;


public interface IZone extends Remote{
	void register(IClient client) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	String updateCoordinates(IClient client, Player.Direction direction) throws RemoteException;
	String updateCoordinates(IClient client,int x , int y) throws RemoteException;
	boolean playerCanMove(int x , int y)throws RemoteException;
	boolean playerCanMove(Player.Direction direction) throws RemoteException;
	void recieveMessage(String message) throws RemoteException;

	
}
