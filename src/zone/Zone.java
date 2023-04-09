package zone;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import shared.remote_objects.IManager;
import shared.remote_objects.IZone;
import shared.common.CLIMessage;
import shared.common.Player;
import shared.common.Player.Direction;
import shared.remote_objects.IClient;

import java.util.Random;


public class Zone implements IZone{
    private IZone zoneUp;
    private IZone zoneDown;
    private IZone zoneLeft;
    private IZone zoneRight;
    private List<IClient> clientList;
    IClient[][] board;
    private IManager manager;
    private int myZoneId;
    final String REGISTRY_TABLE = "localhost";
    final int REGISTRY_PORT = 1099;
    final String MANAGER_NAME = "Manager";

    final int ZONE_SIZE = 5;
    Registry registry;
    int col;
    int row;
    private static final Map<Player.Direction, IZone> neighborDirection = new HashMap<>();
    

    /**
     * 1 (0,0) 2 (0,1)
     * 3 (1,0) 4 (1,1)
     */
 
    public Zone() throws RemoteException{
        super();
        board = new IClient[ZONE_SIZE][ZONE_SIZE]; // Initialize the zone nodes
        for(int i=0; i<ZONE_SIZE; i++){
            for(int j=0; j<ZONE_SIZE; j++){
                board[i][j] = null;
            }
        }
        
        registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
        RegsiterManagerNode(); // Connects to the manager
        RegisterZones();
    }

    /**
     * Register this node to the Manager
     */
    public void RegsiterManagerNode(){
        try {
            manager = (IManager) registry.lookup(MANAGER_NAME);
            CLIMessage.DisplayMessage("Found manager and registered", false);
            this.myZoneId = manager.register(this);
        } catch (RemoteException e) {
            CLIMessage.DisplayMessage("Unable to register client", true);
        } catch (NotBoundException e) {
            CLIMessage.DisplayMessage("Unable to locate Manager in registry", true);
        }
    }
    
	@Override
	public void setPosition(int x, int y) throws RemoteException {
		col = x;
		row = y;
		
	}



    private void RegisterZones(){


            try {
            	
            	zoneUp = manager.getNeighborZone(row -1 , col);
            	neighborDirection.put(Player.Direction.UP, zoneUp);
                CLIMessage.DisplayMessage("Upper zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register upper zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate upper zone in registry", true);
            }
        


            try {
               
            	zoneDown = manager.getNeighborZone(row+1 , col+1);
            	neighborDirection.put(Player.Direction.DOWN, zoneDown);
                CLIMessage.DisplayMessage("Bottom zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register bottom zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate down zone in registry", true);
            }
        


            try {
            	zoneLeft = manager.getNeighborZone(row, col - 1);
            	neighborDirection.put(Player.Direction.DOWN, zoneLeft);
            	
                CLIMessage.DisplayMessage("Left zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register left zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate down zone in registry", true);
            }
        

            try {
                zoneRight = manager.getNeighborZone(row, col + 1);
            	neighborDirection.put(Player.Direction.DOWN, zoneRight);
                CLIMessage.DisplayMessage("Right zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register bottom zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate right zone in registry", true);
            }

        
    }

    /**
     * Add client to the zone
     * @param client
     * @throws RemoteException
     */
	@Override
    public void register(IClient client) throws RemoteException{
        Random rand;
		int xCoordinate = rand.nextInt(ZONE_SIZE); // TODO: Maybe ask input position
        int yCoordinate = rand.nextInt(ZONE_SIZE); // TODO: Maybe ask input position
        while(board[xCoordinate][yCoordinate] != null){
            xCoordinate = rand.nextInt(ZONE_SIZE);
            yCoordinate = rand.nextInt(ZONE_SIZE);
        }
        board[xCoordinate][yCoordinate] = client;
        clientList.add(client);
        client.setCoordinates(xCoordinate, yCoordinate);
    }

    /**
     * Remove the registered client from the zone
     * todo: maybe check if the zone has the client
     * @param client
     * @throws RemoteException
     */
    public void unregister(IClient client) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();
        board[yCoordinate][xCoordinate] = null;
        clientList.remove(client);
    }

    public void recieveClient(IClient client, int x, int y) throws RemoteException{
        if(board[y][x] != null){
            throw new Exception("The position is occupied");
        }
        board[y][x] = client;
    }
    
    /**
     * Recieve a direction request and update the user with the update
     * @param client
     * @param direction
     * @return
     * @throws RemoteException
     */
    public String updateCoordinates(IClient client, Player.Direction direction) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();
        switch(direction){
            case Player.Direction.Up:
                int yUpdated = yCoordinate - 1;
                if(yUpdated < 0) // Leave the zone
                    //todo: print player in different zone
                    return zoneUp.playerCanMove(xCoordinate, ZONE_SIZE - 1);
                else{
                    board[yUpdated][xCoordinate] == client;
                    client.setCoordinates(xCoordinate, yUpdated);
                    return "";
                }
                break;
            case Player.Direction.DOWN:
                int yUpdated = yCoordinate + 1;
                if(yUpdated > ZONE_SIZE)
                    //todo: print
                    return false;
                else
                    return board[yUpdated][xCoordinate] == null;
                break;
            case Player.Direction.Left:
                int xUpdated = xCoordinate - 1;
                if((xUpdated < 0) && (zoneLeft==null))
                    return false;
                else if(xUpdated < 0)
                    return zoneLeft.playerCanMove(ZONE_SIZE-1, yCoordinate);
                else
                    return board[xUpdated][yCoordinate] == null;
                break;
            case Player.Direction.Right:
                int xUpdated = xCoordinate + 1;
                if((xUpdated > ZONE_SIZE) && (zoneRight==null))
                    return false;
                else if(xUpdated > ZONE_SIZE)
                    return zoneRight.playerCanMove(0, yCoordinate);
                else
                    return board[xUpdated][yCoordinate] == null;
                break;
        }
    }

    /**
     * Update client screen
     * @param client
     * @param x
     * @param y
     * @return
     * @throws RemoteException
     */
	@Override
	public String updateCoordinates(IClient client,int x , int y) throws RemoteException{

        StringBuilder sb = new StringBuilder();
        //sb.append("xxxxxxxxxxxxx\n");
        for (int i = y - 1; i <= y + 1; i++) {
            sb.append(" ");
            for (int j = x - 1; j <= x + 1; j++) {
                if (i >= 0 && i < ZONE_SIZE && j >= 0 && j < ZONE_SIZE && (i != y || j != x)) {
                    if(board[i][j] == null)
                        sb.append(" P ");
                    else
                        sb.append(" 0 ");
                } 
//                else {
//                    sb.append("    ");
//                }
            }
            sb.append(" = ");
        }
        return sb.toString();
    }
	@Override
    public boolean playerCanMove(int x , int y)throws RemoteException{
        return board[y][x] == null;
    }

    private void movePlayerToNewZone(int x , int y , IZone targetZone, IClient client){
        unregister(client); // Unregister from currrent zone
        targetZone.register(client); // Register to the target zone
        targetZone.recieveClient(client, x, y); // Move to the new zone
        client.setZone(targetZone); // Set client's new zone to target zone
        client.setCoordinates(x, y); // Update client's coordinates
    }
    /**
     *
     * | 1 | 2 |
     * | 3 | 4 |
     * @param direction
     * @return
     * @throws RemoteException
     */
	@Override
	public boolean playerCanMove(IClient client, Player.Direction direction) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();

        int yUpdated;
        int xUpdated;
        switch(direction){
            case UP:
                /**
                 * x x x |
                 * x x x |
                 * x 0 x
                 */
                yUpdated = yCoordinate - 1;
                if((yUpdated < 0) && (zoneUp==null)) // (1) Reached the global border
                    return false;
                else if(yUpdated < 0){  // (2) Leave the zone
                    if(zoneUp.playerCanMove(xCoordinate, ZONE_SIZE - 1)){
                        movePlayerToNewZone(xCoordinate , ZONE_SIZE -1 , zoneUp , client);
                    }
                    return true;
                }
                else{ // (3) Within the zone
                    if(board[yUpdated][xCoordinate] == null)
                        return false;
                    else{
                        board[yCoordinate][xCoordinate] = null;
                        board[yUpdated][xCoordinate] = client;
                        return true;
                    }
                }
                break;
            case DOWN:
                yUpdated = yCoordinate + 1;
                if((yUpdated > ZONE_SIZE) && (zoneDown==null))
                    return false;
                else if(yUpdated > ZONE_SIZE){
                    if(zoneDown.playerCanMove(xCoordinate, 0)){
                        movePlayerToNewZone(xCoordinate , 0 , zoneDown , client);
                    }
                    return true;
                }
                else{

                    return board[yUpdated][xCoordinate] == null;
                }
    
            case LEFT:
                xUpdated = xCoordinate - 1;
                if((xUpdated < 0) && (zoneLeft==null))
                    return false;
                else if(xUpdated < 0){
                    if(zoneLeft.playerCanMove(ZONE_SIZE-1, yCoordinate)){
                        movePlayerToNewZone(ZONE_SIZE-1 , yCoordinate , zoneLeft , client);
                    }
                    return true;
                }
                else
                    return board[xUpdated][yCoordinate] == null;
            case RIGHT:
                xUpdated = xCoordinate + 1;
                if((xUpdated > ZONE_SIZE) && (zoneRight==null))
                    return false;
                else if(xUpdated > ZONE_SIZE) {
                    if(zoneRight.playerCanMove(0, yCoordinate)){
                        movePlayerToNewZone(0, yCoordinate , zoneRight , client);
                    }
                    return true;
                }
                else
                    return board[xUpdated][yCoordinate] == null;
            default:
                return false;
        }
    }
    public void recieveMessage(String message) throws RemoteException{

    }




    }


}