package shared.remote_objects;
import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Message;

public interface IManager extends Remote{

	void register(IClient client) throws RemoteException;
	void register(IZone zone) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	boolean moveClient(IClient client, IZone caller, IZone dest, int x , int y) throws RemoteException;
	boolean moveClient(IClient client, int zoneID) throws RemoteException;
	void sendMessage(IClient client , String message) throws RemoteException;
}
