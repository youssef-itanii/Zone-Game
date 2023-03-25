package shared.remote_objects;
import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Message;

public interface IManager extends Remote{

	void register(IClient client) throws RemoteException;
	void register(IZone zone) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	void moveClient(IClient client, IZone dest) throws RemoteException;
	void sendMessage(IClient client , Message message) throws RemoteException;
}
