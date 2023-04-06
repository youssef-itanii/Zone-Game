package zone;
package shared.remote_objects;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

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


    private static final Map<Integer, SimpleEntry<Integer, Integer>> BOARD_MAP = new HashMap<>();

    /**
     * 1 (0,0) 2 (0,1)
     * 3 (1,0) 4 (1,1)
     */
    static {
        // Adding key-value pairs to the map
        MY_MAP.put(1, new SimpleEntry<>(0, 0));
        MY_MAP.put(2, new SimpleEntry<>(0, 1));
        MY_MAP.put(3, new SimpleEntry<>(1, 0));
        MY_MAP.put(4, new SimpleEntry<>(1, 1))
    }

    public Zone() throws RemoteException{
        super();
        board = new IClient[ZONE_SIZE][ZONE_SIZE]; // Initialize the zone nodes
        for(int i=0; i<ZONE_SIZE; i++){
            for(int j=0; j<ZONE_SIZE; j++){
                board[i][j] = null;
            }
        }
        RegsiterManagerNode(); // Connects to the manager
        RegisterZones();
    }

    /**
     * Register this node to the Manager
     */
    public void RegsiterManagerNode(){
        try {
            Registry registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
            manager = (IManager) registry.lookup(MANAGER_NAME);
            CLIMessage.DisplayMessage("Found manager and registered", false);
            this.myZoneId = manager.register(this);
        } catch (RemoteException e) {
            CLIMessage.DisplayMessage("Unable to register client", true);
        } catch (NotBoundException e) {
            CLIMessage.DisplayMessage("Unable to locate Manager in registry", true);
        }
    }

    /**
     * 1 | 2 | 3
     * 4 | 5 | 6
     *
     *
     * 1(0,0) 2 (0,1)
     * 2(1,0) 3 (1,1)
     *
     */

    private int getNeighbourId(SimpleEntry<Integer, Integer> target, ){
        int key = -1; // Initialize with default value
        for (Map.Entry<Integer, SimpleEntry<Integer, Integer>> entry : BOARD_MAP.entrySet()) {
            if (entry.getValue().equals(target)) {
                key = entry.getKey();
                return key;
            }
        }
        if (key != -1) {
            System.out.println("Key found: " + key); // Output: Key found: 2
        } else {
            System.out.println("Key not found");
        }
        return key;
    }

    private void RegisterZones(){
        SimpleEntry<Integer, Integer> firstIndex = BOARD_MAP.get(this.myZoneId).getKey();
        SimpleEntry<Integer, Integer> secondIndex = BOARD_MAP.get(this.myZoneId).getValue();

        SimpleEntry<Integer, Integer> up = new SimpleEntry<>(firstIndex-1, secondIndex);
        SimpleEntry<Integer, Integer> down = new SimpleEntry<>(firstIndex-1, secondIndex);
        SimpleEntry<Integer, Integer> left = new SimpleEntry<>(firstIndex-1, secondIndex);
        SimpleEntry<Integer, Integer> right = new SimpleEntry<>(firstIndex-1, secondIndex);


        if(firstIndex-1>=0){ // Upper zone
            SimpleEntry<Integer, Integer> up = new SimpleEntry<>(firstIndex-1, secondIndex);
            int up_zone_id = getNeighbourId(up);

            try {
                Registry registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
                zoneRight = (IZone) registry.lookup("Zone-" + up_zone_id);
                CLIMessage.DisplayMessage("Upper zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register upper zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate upper zone in registry", true);
            }
        }

        if(firstIndex+1<ZONE_SIZE){ // Bottom zone
            SimpleEntry<Integer, Integer> down = new SimpleEntry<>(firstIndex+1, secondIndex);
            int down_zone_id = getNeighbourId(down);

            try {
                Registry registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
                zoneRight = (IZone) registry.lookup("Zone-" + down_zone_id);
                CLIMessage.DisplayMessage("Bottom zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register bottom zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate down zone in registry", true);
            }
        }

        if(secondIndex-1>=0){ // Left zone
            SimpleEntry<Integer, Integer> left = new SimpleEntry<>(firstIndex, secondIndex-1);
            int left_zone_id = getNeighbourId(left);

            try {
                Registry registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
                zoneRight = (IZone) registry.lookup("Zone-" + left_zone_id);
                CLIMessage.DisplayMessage("Left zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register left zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate down zone in registry", true);
            }
        }

        if(secondIndex+1<ZONE_SIZE){ // Right zone
            SimpleEntry<Integer, Integer> right = new SimpleEntry<>(firstIndex, secondIndex+1);
            int right_zone_id = getNeighbourId(right);

            try {
                Registry registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
                zoneRight = (IZone) registry.lookup("Zone-" + right_zone_id);
                CLIMessage.DisplayMessage("Right zone registered", false);
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register bottom zone", true);
            } catch (NotBoundException e) {
                CLIMessage.DisplayMessage("Unable to locate right zone in registry", true);
            }

        }
    }

    /**
     * Add client to the zone
     * @param client
     * @throws RemoteException
     */
    void register(IClient client) throws RemoteException{
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
    void unregister(IClient client) throws RemoteException{
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
    String updateCoordinates(IClient client, Player.Direction direction) throws RemoteException{
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
            case Player.Direction.Down:
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
    String updateCoordinates(IClient client,int x , int y) throws RemoteException{

        StringBuilder sb = new StringBuilder();
        //sb.append("xxxxxxxxxxxxx\n");
        for (int i = y - 1; i <= y + 1; i++) {
            sb.append(" ");
            for (int j = x - 1; j <= x + 1; j++) {
                if (i >= 0 && i < ZONE_SIZE && j >= 0 && j < ZONE_SIZE && (i != y || j != x)) {
                    if(board[i][j] == null)
                        sb.append(" P ");
                    else
                        sb.append(" O ");
                } else {
                    sb.append("    ");
                }
            }
            sb.append("\n");
            //sb.append("             \n");
        }
        //sb.append("xxxxxxxxxxxxx\n");
        return sb.toString();
    }
    boolean playerCanMove(int x , int y)throws RemoteException{
        return board[y][x] == null;
    }

    /**
     *
     * | 1 | 2 |
     * | 3 | 4 |
     * @param direction
     * @return
     * @throws RemoteException
     */
    boolean playerCanMove(IClient client, Player.Direction direction) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();
        switch(direction){
            case Player.Direction.Up:
                /**
                 * x x x |
                 * x x x |
                 * x 0 x
                 */
                int yUpdated = yCoordinate - 1;
                if((yUpdated < 0) && (zoneUp==null)) // (1) Reached the global border
                    return false;
                else if(yUpdated < 0){  // (2) Leave the zone
                    if(zoneUp.playerCanMove(xCoordinate, ZONE_SIZE - 1)){
                        unregister(client); // Unregister from currrent zone
                        zoneUp.register(client); // Register to the upper (top) zone
                        zoneUp.recieveClient(client, xCoordinate, ZONE_SIZE - 1); // Move to the upper (top) zone
                        client.setZone(zoneUp);
                        client.setCoordinates(xCoordinate, ZONE_SIZE - 1);
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
            case Player.Direction.Down:
                int yUpdated = yCoordinate + 1;
                if((yUpdated > ZONE_SIZE) && (zoneDown==null))
                    return false;
                else if(yUpdated > ZONE_SIZE)
                    if(zoneDown.playerCanMove(xCoordinate, 0)){
                        unregister(client); // Unregister from currrent zone
                        zoneDown.register(client); // Register to the bottom zone
                        zoneDown.recieveClient(client, xCoordinate, 0); // Move to the bottom zone
                        client.setZone(zoneDown);
                        client.setCoordinates(xCoordinate, 0);
                    }
                    return true;
                else
                    return board[yUpdated][xCoordinate] == null;
                break;
            case Player.Direction.Left:
                int xUpdated = xCoordinate - 1;
                if((xUpdated < 0) && (zoneLeft==null))
                    return false;
                else if(xUpdated < 0)
                    if(zoneLeft.playerCanMove(ZONE_SIZE-1, yCoordinate)){
                        unregister(client); // Unregister from currrent zone
                        zoneLeft.register(client); // Register to the left zone
                        zoneLeft.recieveClient(client, ZONE_SIZE-1, yCoordinate); // Move to the left zone
                        client.setZone(zoneLeft);
                        client.setCoordinates(ZONE_SIZE-1, yCoordinate);
                    }
                    return true;
                else
                    return board[xUpdated][yCoordinate] == null;
                break;
            case Player.Direction.Right:
                int xUpdated = xCoordinate + 1;
                if((xUpdated > ZONE_SIZE) && (zoneRight==null))
                    return false;
                else if(xUpdated > ZONE_SIZE)
                    if(zoneRight.playerCanMove(0, yCoordinate)){
                        unregister(client); // Unregister from currrent zone
                        zoneRight.register(client); // Register to the righ zone
                        zoneRight.recieveClient(client, 0, yCoordinate); // Move to the right zone
                        client.setZone(zoneRight);
                        client.setCoordinates(0, yCoordinate);
                    }
                    return true;
                else
                    return board[xUpdated][yCoordinate] == null;
                break;
        }
    }
    void recieveMessage(String message) throws RemoteException{

    }




}