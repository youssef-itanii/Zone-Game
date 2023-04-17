package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import shared.common.CLIMessage;
import shared.common.Player;
import shared.remote_objects.IClient;
import shared.remote_objects.IManager;
import shared.remote_objects.IZone;

public class Client implements IClient{

	private IManager manager;
	public IZone zone;
	private Player player;
	public int ID = 64;
	public int X;
	public int Y;
	
	public Client(Player player) {
		try {
			UnicastRemoteObject.exportObject(this , 0);
			this.player = player;
		}
		catch(RemoteException e) {
			e.printStackTrace();
			CLIMessage.DisplayMessage("Unable to export user object", true);
		}

	}
    //===========================================================================
	public void register() {
		try {
			Registry registry = LocateRegistry.getRegistry("localhost" , 1099);
			manager = (IManager) registry.lookup("Manager");
			CLIMessage.DisplayMessage("Found manager and registered", false);
			
			ID += manager.register(this);
			CLIMessage.DisplayMessage("GOT ID "+ID, false);
		} catch (RemoteException e) {

			CLIMessage.DisplayMessage("Unable to register client", true);
		} catch (NotBoundException e) {
			CLIMessage.DisplayMessage("Unable to locate Manager in registry", true);
		}

	}
    //===========================================================================
	@Override
	public void setZone(IZone target) throws RemoteException {
		zone = target;

	}
    //===========================================================================
	@Override
	public void setCoordinates(int y, int x) throws RemoteException {
		X = x;
		Y = y;
		player.setCoordinates(y, x);
	}
    //===========================================================================
	@Override
	public int getX() throws RemoteException {
		return X;

	}
    //===========================================================================
	@Override
	public int getY() throws RemoteException {
		return Y;

	}
    //===========================================================================
	@Override
	public void recieveMessage(String message, String author) throws RemoteException {
		player.processMessage("* "+author+": "+message);


	}
    //===========================================================================
	@Override
	public void recieveUpdatedMap(String map) throws RemoteException {
		// TODO Auto-generated method stub
		player.map = map;
		player.displayMap();

	}

    //===========================================================================
	public String requestMovement(Player.Direction direction) {
		try {
			return zone.movePlayer(this, direction);

		} catch (RemoteException e) {
			player.clearScreen();
			CLIMessage.DisplayMessage("System: Communication with Zone has failed. Disconnected." , true);
		}

		return "";
	}
    //===========================================================================
	public void unregister() {
		try {
			zone.unregister(this);
			CLIMessage.DisplayMessage("Disconnection successful.", false);
		} catch (RemoteException e) {

			CLIMessage.printError("Unable to unregister from zone due to loss of connection. Logging out", true);


		}
	}
    //===========================================================================


	public void registerToZone(int id) {
		try {
			manager.setZone(this, id);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to register to zone", false);
		}
	}
    //===========================================================================
	public String requestAvaialableZones() {
		try {
			return manager.getAvaialbeZones();
		} catch (RemoteException e) {
			e.printStackTrace();
			CLIMessage.DisplayMessage("Unable to retrieve zones", true);
		}
		return "";
	}
    //===========================================================================
	public int getZoneID() {
		try {
			return zone.getID();
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Zone is not avaialbe", false);
			return -1;
		}
	}
    //===========================================================================
	@Override
	public int getID() throws RemoteException {
		return ID;
	}
	//==========================================================================
	public void requestZonesMap() {
		try {
			player.zonesMap = zone.getZonesMap();
			player.displayZonesMap();
		} catch (RemoteException e) {
			player.clearScreen();
			CLIMessage.DisplayMessage("System: Communication with Zone has failed. Disconnected." , true);
		}
	}

}
