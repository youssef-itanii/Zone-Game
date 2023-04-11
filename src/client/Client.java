package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import manager.Manager;
import shared.common.CLIMessage;
import shared.common.Message;
import shared.common.Player;
import shared.remote_objects.IClient;
import shared.remote_objects.IManager;
import shared.remote_objects.IZone;
import zone.Zone;

public class Client implements IClient{

	private IManager manager;
	public IZone zone;
	private Player player;
	public int ID;
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
			
			ID = manager.register(this);
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
			CLIMessage.DisplayMessage("Unable to request coordinates update", false);
		}

		return "";
	}
    //===========================================================================
	public void unregister() {
		try {
			manager.unregister(this);

		} catch (RemoteException e) {

			CLIMessage.DisplayMessage("Unable to unregister client", true);


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

}
