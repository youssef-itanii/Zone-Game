package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import manager.Manager;
import shared.common.Message;
import shared.remote_objects.IClient;
import shared.remote_objects.IZone;
import zone.Zone;

public class Client implements IClient{

	private Manager connection;
	private Zone subscribedZone;
	
	public void User() {
		try {
			UnicastRemoteObject.exportObject(this , 0);
		}
		catch(RemoteException e) {
			
		}
	}
	
	@Override
	public void setZone(IZone target) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCoordinates(int x, int y) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getX() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getY() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recieveMessage(Message message) {
		// TODO Auto-generated method stub
		
	}

}
