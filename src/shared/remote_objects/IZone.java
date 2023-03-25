package shared.remote_objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Movement;


public interface IZone extends Remote{
	void register(IClient client) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	void updateCoordinates(IClient client, Movement.Direction direction) throws RemoteException;
	
}
