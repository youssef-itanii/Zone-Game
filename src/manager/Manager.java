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
	
	
	//=========================================================================
	
	/***
	 * Constructor for Manager
	 * Creates the registry, binds itself, and exports itself as a remote object
	 * Init array for clients
	 */
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
		zones = new ArrayList<>();
		
		connectedClients = new ArrayList<>();
		CLIMessage.DisplayMessage("Manager is ready", false);
	}
	
	/***
	 * Register new client
	 */
	@Override
	public int register(IClient client){

		connectedClients.add(client);
		Message message = new Message("Manager" , "Welcome!");
		CLIMessage.DisplayMessage("New user registered", false);
		
		sendMessage(client , message.toString());
		CLIMessage.DisplayMessage("Current connected clients "+connectedClients.size(), false);
//		
	

		return connectedClients.size();
		
	}

	/***
	 * Register new zone 
	 */
	@Override
	public int register(IZone zone) throws RemoteException {
		if(registeredZones == MAX_ZONES) {
			//Notify fail due to max capacity
			return -1;
		}
		
		zones.add(zone);
		//Send ID
		registeredZones++;
		return registeredZones;
		
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
	
	/***
	 * Moves the client from one zone to another based on the coordinates processed by the previous zone. 
	 * @param client This is the client to move
	 * @param caller Zone that requested moving the client
	 * @param dest Destination zone
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return boolean Returns whether the player can move or not
	 */
	@Override
	public boolean  moveClient(IClient client, IZone caller ,IZone dest , int x , int y) throws RemoteException {

		if(dest.playerCanMove(x, y)) {
			dest.updateCoordinates(client, x , y);
			return true;
		}
		
		caller.recieveMessage("Cannot move player to that zone");
		return false;	
	}
	/***
	 * Moves the client to a zone based on their request upon registration
	 * @param client This is the client to move
	 * @param zoneID Zone ID
	 * @return int Returns whether the player can be placed or not
	 */
	@Override
	public int setZone(IClient client, int zoneID) {
		return zoneID;
//		try {
//			IZone selectedZone = zones.get(zoneID);
//			selectedZone.register(client);
//			return zoneID;
//		}
//		catch(RemoteException e) {
//			sendMessage(client, "Unable to register to zone");
//			CLIMessage.DisplayMessage("Unable to register client to zone", false);
//		}
//		return -1;	
	}
	//=======================================================================================
	/***
	 * Send message to target client
	 */
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
	public String getAvaialbeZones() throws RemoteException {
		Message zoneSelectionMessage = new Message("Manager" , "===============[Zone-select]=============== \n"
				+ "Please select a zone number from the following range\n"
				+ "Zones available: " + zones.size() + "\n"
				+ "Range: 0 - "+(zones.size()));
		
		return zoneSelectionMessage.toString();
	}


}
