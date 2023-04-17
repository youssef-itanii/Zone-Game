package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IClient extends Remote{

	void setZone(IZone target) throws RemoteException;
	void setCoordinates(int x , int y) throws RemoteException;
	int getID() throws RemoteException;
	int getX() throws RemoteException;
	int getY() throws RemoteException;
	//Recieve messages
	void recieveMessage(String message, String author) throws RemoteException;
	//Sends updated map to client
	void recieveUpdatedMap(String map) throws RemoteException;


}