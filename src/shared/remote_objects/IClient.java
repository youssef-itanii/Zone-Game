package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Message;


public interface IClient extends Remote{
	
	void setZone(IZone target) throws RemoteException;
	void setCoordinates(int x , int y) throws RemoteException;
//	Response sayHello() ?
	void getX() throws RemoteException;
	void getY() throws RemoteException;
	void recieveMessage(String message) throws RemoteException;;

	
}
