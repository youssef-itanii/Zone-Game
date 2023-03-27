package zone;
package shared.remote_objects;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Random;


public class Zone implements IZone{
    private Zone zone1;
    private Zone zone2;
    private Zone zone3;
    private List<IPlayer> playerList;
    IPlayer[][] board;
    private Manager manager;
    private int myZoneId;
    final String REGISTRY_TABLE = "localhost";
    final int REGISTRY_PORT = 1099;
    final String MANAGER_NAME = "Manager";

    public Zone() throws RemoteException{
        /**
         * Register the manager
         */
        super();
        board = new IPlayer[5][5];
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                board = null;
            }
        }
        RegisterNode();
    }

    public void RegisterNodeRequest(){
        /** Recieve id from the server
         */
        try {
            Registry registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
            manager = (Manager) registry.lookup(MANAGER_NAME);
            CLIMessage.DisplayMessage("Found manager and registered", false);
            this.myZoneId = manager.register(this);
        } catch (RemoteException e) {
            CLIMessage.DisplayMessage("Unable to register client", true);
        } catch (NotBoundException e) {
            CLIMessage.DisplayMessage("Unable to locate Manager in registry", true);
        }
    }

    void register(IClient client) throws RemoteException{
        int xCoordinate = rand.nextInt(8) + 1; // TODO: Maybe ask input position
        int yCoordinate = rand.nextInt(8) + 1; // TODO: Maybe ask input position
        while(board[xCoordinate][yCoordinate] == null){
            xCoordinate = rand.nextInt(8) + 1;
            yCoordinate = rand.nextInt(8) + 1;
        }
        board[xCoordinate][yCoordinate] = client;

    }
    void unregister(IClient client) throws RemoteException{

    }

    String updateCoordinates(IClient client, Player.Direction direction) throws RemoteException{

    }
    String updateCoordinates(IClient client,int x , int y) throws RemoteException{

    }
    boolean playerCanMove(int x , int y)throws RemoteException{

    }
    boolean playerCanMove(Player.Direction direction) throws RemoteException{

    }
    void recieveMessage(String message) throws RemoteException{

    }




}

