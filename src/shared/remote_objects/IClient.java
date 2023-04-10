package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Message;


public interface IClient extends Remote{

	void setZone(IZone target) throws RemoteException;
	void setCoordinates(int x , int y) throws RemoteException;
	int getID() throws RemoteException;
	//	Response sayHello() ?
	int getX() throws RemoteException;
	int getY() throws RemoteException;
	void recieveMessage(String message, String author) throws RemoteException;
	void recieveUpdatedMap(String map) throws RemoteException;


}