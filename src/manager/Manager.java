package manager;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import shared.common.AppConfig;
import shared.common.CLIMessage;
import shared.remote_objects.IClient;
import shared.remote_objects.IManager;
import shared.remote_objects.IZone;


public class Manager implements IManager {

	private List<IClient> connectedClients = null;
	private int registeredZones = 0;
	private int NUMBER_OF_ZONES;
	private int ZONES_PER_ROW;
	private int PORT;
	int zoneRow = 0;
	int zoneCol = 0;
	Registry registry;


	//=========================================================================

	/***
	 * Constructor for Manager
	 * Creates the registry, binds itself, and exports itself as a remote object
	 * Init array for clients
	 */
	public Manager() {
		initManagerConfig();
		try {

			registry = LocateRegistry.createRegistry(PORT);
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
	
	private void initManagerConfig() {
		NUMBER_OF_ZONES = AppConfig.getNumberOfZones();
		ZONES_PER_ROW = AppConfig.getZonePerRow();
		PORT = AppConfig.getPort();
	}
    //===========================================================================
	/***
	 * Register new client
	 */
	@Override
	public int register(IClient client){

		connectedClients.add(client);
		CLIMessage.DisplayMessage("New user registered", false);

		sendMessage(client , "Welcome");
		CLIMessage.DisplayMessage("Current connected clients "+connectedClients.size(), false);
//


		return connectedClients.size();

	}
    //===========================================================================
	/***
	 * Register new zone
	 */
	@Override
	public int register(IZone zone) {

		if(zoneCol == NUMBER_OF_ZONES - 1) {
			zoneCol = 0;
			zoneRow ++;
			if(zoneRow == NUMBER_OF_ZONES) {
				return -1;
			}
		}

		//set position in registry
		int currentIndex = registeredZones;
		try {
			zone.setPosition(currentIndex);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to register new zone", false);
			return -1;
		}
		zoneCol++;
		registeredZones++;
		return currentIndex;

	}
	
    //===========================================================================
	@Override
	public IZone getNeighborZone(int index) {
	return null;
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
	 * Moves the client to a zone based on their request upon registration
	 * @param client This is the client to move
	 * @param zoneID Zone ID
	 * @return int Returns whether the player can be placed or not
	 */
	@Override
	public void setZone(IClient client, int zoneID) {
		if(client == null) return ;
		
		int row = (zoneID - 1)/NUMBER_OF_ZONES;
		int col = (zoneID - 1)%NUMBER_OF_ZONES;

		try {
			IZone selectedZone ;
			try {
				selectedZone = (IZone) registry.lookup("Zone-"+zoneID);			
			}
			catch(Exception ex) {
				System.out.println("unablet to connect to zone");
				return;
			}
			
			if(selectedZone == null) {
				return;
			}
			selectedZone.register(client , -1 , -1);
			
		}
		catch(RemoteException e) {
			sendMessage(client, "Unable to register to zone");
			CLIMessage.DisplayMessage("Unable to register client to zone", false);
		}

	}
	//=======================================================================================
	/***
	 * Send message to target client
	 */
	@Override
	public void sendMessage(IClient client, String message) {

		try {
			client.recieveMessage(message, "Manager");
		}
		catch(RemoteException e) {
			removeClient(client);
			CLIMessage.DisplayMessage("Unable to send message to client", false);
		}

	}
    //===========================================================================
	@Override
	public String getAvaialbeZones() throws RemoteException {
		String zoneSelectionMessage = "===============[Zone-select]=============== \n"
				+ "Please select a zone number from the following range\n"
				+ "Zones available: " + (NUMBER_OF_ZONES) + "\n"
				+ "Range: 0 - "+(NUMBER_OF_ZONES);

		return zoneSelectionMessage;
	}
    //===========================================================================






}