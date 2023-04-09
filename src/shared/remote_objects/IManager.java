package shared.remote_objects;
import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.common.Message;

public interface IManager extends Remote{

	int register(IClient client) throws RemoteException;
	int register(IZone zone) throws RemoteException;
	void unregister(IClient client) throws RemoteException;
	boolean moveClient(IClient client, IZone caller, IZone dest, int x , int y) throws RemoteException;
	int setZone(IClient client, int zoneID) throws RemoteException;
	void sendMessage(IClient client , String message) throws RemoteException;
	String getAvaialbeZones() throws RemoteException;
	IZone getNeighborZone(int row , int col) throws RemoteException;
}