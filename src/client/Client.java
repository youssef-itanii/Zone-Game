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

import java.util.Random;


public class Client implements IClient{

	private IManager manager;
	private IZone zone;
	private Player player;
	
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
	
	public void register() {
		try {
			Registry registry = LocateRegistry.getRegistry("localhost" , 1099);
			manager = (IManager) registry.lookup("Manager");
			CLIMessage.DisplayMessage("Found manager and registered", false);


			int randomZone = rand.nextInt(4) + 1; // TODO: Ask input from user

			StringBuilder sb = new StringBuilder("Zone-");
			sb.insert(5, String.valueOf(randomZone));

			Registry registry = LocateRegistry.getRegistry("localhost" , 1099);
			zone = (IZone) registry.lookup(sb.toString());
			setZone(zone);

			int ID = manager.register(this, randomZone);
			
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to register client", true);
		} catch (NotBoundException e) {
			CLIMessage.DisplayMessage("Unable to locate Manager in registry", true);
		}

	}
	
	@Override
	public void setZone(IZone target) throws RemoteException {
		zone = target;
		
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
	public void recieveMessage(String message) throws RemoteException {
		CLIMessage.DisplayMessage(message, false);
		
	}

	@Override
	public void recieveUpdatedMap(String map) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	
	public String requestMovement(Player.Direction direction) {
		try {
			if(zone.playerCanMove(direction)) {
				return zone.updateCoordinates(this, direction);
			}
		
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to request coordinates update", false);
		}
		
		return "";
	}
	
	public void unregister() {
		try {
			manager.unregister(this);
			
		} catch (RemoteException e) {
	
			CLIMessage.DisplayMessage("Unable to unregister client", true);
		
		
		}
	}

}
