package shared.remote_objects;
import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Message;

public interface IManager extends Remote{

	int register(IClient client) throws RemoteException;
	int register(IZone zone) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	void setZone(IClient client, int zoneID) throws RemoteException;
	void sendMessage(IClient client , String message) throws RemoteException;
	String getAvaialbeZones() throws RemoteException;
	IZone getNeighborZone(int index) throws RemoteException;
	void notifyZones() throws RemoteException;
}