package manager;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import shared.common.CLIMessage;
import shared.common.Message;
import shared.remote_objects.IClient;
import shared.remote_objects.IManager;
import shared.remote_objects.IZone;


public class Manager implements IManager {

	private List<IClient> connectedClients = null;
	private int registeredZones = 0;
	private int MAX_ZONES = 4;
	private ArrayList<IZone> zones;
	
	public Manager() {
		try {

			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind("Manager", this);
			UnicastRemoteObject.exportObject(this , 0);
			

		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to register manager", true);
		} catch (AlreadyBoundException e) {
			CLIMessage.DisplayMessage("Manager is already bound", true);;
		}
		
		connectedClients = new ArrayList<>();
		CLIMessage.DisplayMessage("Manager is ready", false);
	}
	@Override
	public void register(IClient client){

		connectedClients.add(client);
		Message message = new Message("Manager" , "Welcome!");
		CLIMessage.DisplayMessage("New user registered", false);
		
		sendMessage(client , message.toString());
		CLIMessage.DisplayMessage("Current connected clients "+connectedClients.size(), false);
		
	}

	@Override
	public void register(IZone zone) throws RemoteException {
		//Add more logic here
		zones.add(zone);
		
	}
	//===============================CLIENT REMOVAL==========================================
	@Override
	public void unregister(IClient client) throws RemoteException {
		// TODO Auto-generated method stub
		removeClient(client);
		
	}
	
	private void removeClient(IClient client) {
		connectedClients.remove(client);
		CLIMessage.DisplayMessage("Current connected clients "+connectedClients.size(), false);
	}
	//=======================================================================================
	@Override
	public boolean  moveClient(IClient client, IZone caller ,IZone dest , int x , int y) throws RemoteException {
		// TODO Auto-generated method stub
		if(dest.playerCanMove(x, y)) {
			
			return true;
		}
		
			caller.recieveMessage("Cannot move player to that zone");
			return false;
		
		
		
	}

	@Override
	public void sendMessage(IClient client, String message) {
		
		try {
			client.recieveMessage(message);
		}
		catch(RemoteException e) {
			removeClient(client);
			CLIMessage.DisplayMessage("Unable to send message to client", false);
		}
		
	}
	@Override
	public boolean moveClient(IClient client, int zoneID) {
		IZone selectedZone = zones.get(zoneID);
		try {
			selectedZone.register(client);
		}
		catch(RemoteException e) {
			sendMessage(client, "Unable to register to zone");
			CLIMessage.DisplayMessage("Unable to register client to zone", false);
		}
		
		
	}

}
