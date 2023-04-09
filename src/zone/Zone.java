package zone;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import shared.remote_objects.IManager;
import shared.remote_objects.IZone;
import shared.common.AppConfig;
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
    private IClient[][] board;
    private IManager manager;
    private int ID;
    final String REGISTRY_TABLE = "localhost";
    final int REGISTRY_PORT = 1099;
    final String MANAGER_NAME = "Manager";
	private int MAX_ZONES;
    final int N;
    Registry registry;
    int index;
    int rowPosition;

    /**
     * 1 (0,0) 2 (0,1)
     * 3 (1,0) 4 (1,1)
     */
 
    public Zone() {
    	try {
			UnicastRemoteObject.exportObject(this , 0);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to export zone", true);
		}
    	
    	N = AppConfig.getZoneSize();
    	MAX_ZONES = AppConfig.getNumberOfZones();
        board = new IClient[N][N]; // Initialize the zone nodes
        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                board[i][j] = null;
            }
        }
        
        try {
			registry = LocateRegistry.getRegistry(REGISTRY_TABLE, REGISTRY_PORT);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to locate registry in zone", true);
		}
        clientList = new ArrayList<IClient>();
        RegsiterManagerNode(); // Connects to the manager
     
    }

    /**
     * Register this node to the Manager
     */
    public void RegsiterManagerNode(){
        try {
            manager = (IManager) registry.lookup(MANAGER_NAME);
            this.ID = manager.register(this);
            CLIMessage.DisplayMessage("Found manager and registered with ID "+ID, false);
            if(this.ID == MAX_ZONES -1 && manager != null) {
            	manager.notifyZones();
            }
        } catch (RemoteException e) {
            CLIMessage.DisplayMessage("Unable to register zone", true);
        } catch (NotBoundException e) {
            CLIMessage.DisplayMessage("Unable to locate Manager in registry for zone", true);
        }
    }
    
	@Override
	public void setPosition(int index) throws RemoteException {
		this.index = index;
		
	}

    private void RegisterZones(){

            try {
            	zoneUp = manager.getNeighborZone(index - 4);
            	if(zoneUp == null) {
            		CLIMessage.DisplayMessage("No upper zone", false);
            	}
            	else {
                    CLIMessage.DisplayMessage("Upper zone registered", false);
            	}

            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register upper zone", true);
            }
        
            try {
               
            	zoneDown = manager.getNeighborZone(index + 4);
            	if(zoneDown == null) {
            		CLIMessage.DisplayMessage("No bottom zone", false);
            	}
            	else {
                    CLIMessage.DisplayMessage("Bottom zone registered", false);
            	}
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register bottom zone", true);
            }
        

            try {
            	zoneLeft = manager.getNeighborZone(index - 1);
            	if(zoneLeft == null) {
            		CLIMessage.DisplayMessage("No left zone", false);
            	}
            	else {
                    CLIMessage.DisplayMessage("Left zone registered", false);
            	}
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register left zone", true);
            }
        

            try {
                zoneRight = manager.getNeighborZone(index + 1);
            	if(zoneRight == null) {
            		CLIMessage.DisplayMessage("No right zone", false);
            	}
            	else {
                    CLIMessage.DisplayMessage("Right zone registered", false);
            	}
            } catch (RemoteException e) {
                CLIMessage.DisplayMessage("Unable to register bottom zone", true);
            }  
    }

    /**
     * Add client to the zone
     * @param client
     * @throws RemoteException
     */
	@Override
    public void register(IClient client , int row , int col) {
		if(row == -1 || col == -1) {
			Random rand = new Random();
			
			col = rand.nextInt(N); // TODO: Maybe ask input position
			row = rand.nextInt(N); // TODO: Maybe ask input position
			while(board[row][col] != null){
				col = rand.nextInt(N);
				row = rand.nextInt(N);
			}
		}
        board[row][col] = client;
        clientList.add(client);
        try {
			client.setZone(this);
		} catch (RemoteException e2) {
			CLIMessage.DisplayMessage("Unable to set zone to client", false);
		}
        try {
			client.setCoordinates(row, col);
		} catch (RemoteException e1) {
			CLIMessage.DisplayMessage("Unable to set coordinates to client", false);
		}
        try {
			client.recieveUpdatedMap(GenerateUpdatedMapString());
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to generate map for client", false);
		}
        CLIMessage.DisplayMessage("Registered client", false);
    }

    /**
     * Remove the registered client from the zone
     * todo: maybe check if the zone has the client
     * @param client
     * @throws RemoteException
     */
    public void unregister(IClient client) {
    	
    		int xCoordinate;
			try {
				xCoordinate = client.getX();
				int yCoordinate = client.getY();
				board[yCoordinate][xCoordinate] = null;
			} catch (RemoteException e) {
				CLIMessage.DisplayMessage("Connection has been lost with a client", false);
			}
    		
    	
        clientList.remove(client);
    }

    public void placePlayer(IClient client, int y, int x) throws RemoteException{
        board[y][x] = client;
    }
    
    /**
     * Recieve a direction request and update the user with the update
     * @param client
     * @param direction
     * @return
     * @throws RemoteException
     */
   
    private String GenerateUpdatedMapString() {
		String generatedMap = "";
        for (int i = 0; i < N; i++) {     
            for (int j = 0 ;j < N; j++) {
                    if(board[i][j] == null)
                    	generatedMap+="0 ";
                    else
                        generatedMap+="P ";
                } 
            if(i != N-1)
            	generatedMap+= "= ";
        }
        generatedMap.replace("\t", "");
        return generatedMap;	
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
	public String updateCoordinates(IClient client,int x , int y){

		String generatedMap = GenerateUpdatedMapString();
        return generatedMap;
    }
	
	
		
	
    private boolean playerCanMove(int row , int col){
    	if(board[row][col] == null) {
    		CLIMessage.DisplayMessage("Cell requested is free", false);
    		return true;
    	}
    	CLIMessage.DisplayMessage("Cell requested is occupied", false);
		return false;
 
    }

    private String movePlayerToNewZone(int col , int row , IZone targetZone, IClient client){
    	if(!playerCanMove(row, col)) {
    		return "";
    	}
        try {
			unregister(client);
			targetZone.register(client , row , col); // Register to the target zone
			targetZone.placePlayer(client, row, col); // Move to the new zone
			client.setZone(targetZone); // Set client's new zone to target zone
			CLIMessage.DisplayMessage("Sending map", false);
			return "";
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to unregister client", false);
		}
		return ""; 
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
	public String movePlayer(IClient client, Player.Direction direction) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();
        CLIMessage.DisplayMessage("Player requsedted movement", false);
        int yUpdated;
        int xUpdated;
        switch(direction){
            case UP:
                yUpdated = yCoordinate - 1;
                if(yUpdated < 0){  // (2) Leave the zone
                	if(zoneUp == null) return "";
                    return movePlayerToNewZone(xCoordinate , N -1 , zoneUp , client);
                    
                }
                else{ // (3) Within the zone
                    if(!playerCanMove(yUpdated, xCoordinate))
                        return "";
                    else{
                        board[yCoordinate][xCoordinate] = null;
                        board[yUpdated][xCoordinate] = client;
                        CLIMessage.DisplayMessage("Player moved up", false);
                        return GenerateUpdatedMapString();
                    }
                }
            case DOWN:
                yUpdated = yCoordinate + 1;
                if(yUpdated >= N){
                	if(zoneDown==null) return "";
                    return movePlayerToNewZone(xCoordinate , 0 , zoneDown , client);
             
                }
                else{
                
                    if(!playerCanMove(yUpdated, xCoordinate))
                        return "";
                    else{
                        board[yCoordinate][xCoordinate] = null;
                        board[yUpdated][xCoordinate] = client;
                        CLIMessage.DisplayMessage("Player moved down", false);
                        return GenerateUpdatedMapString();
                    }
                }
    
            case LEFT:
                xUpdated = xCoordinate - 1;
                if(xUpdated < 0){
                	if(zoneLeft==null) return "";
                    return movePlayerToNewZone(N-1 , yCoordinate , zoneLeft , client);
                }
                else
                {
	                if(!playerCanMove(yCoordinate, xUpdated))
	                    return "";
	                else{
	                    board[yCoordinate][xCoordinate] = null;
	                    board[yCoordinate][xUpdated] = client;
	                    CLIMessage.DisplayMessage("Player moved left", false);
	                    return GenerateUpdatedMapString();
	                }
                }
            case RIGHT:
                xUpdated = xCoordinate + 1;
                if(xUpdated >= N) {
                	if(zoneRight==null) return "";
                    return movePlayerToNewZone(0, yCoordinate , zoneRight , client);
                }
                else
                {
	                if(!playerCanMove(yCoordinate, xUpdated))
	                    return "";
	                else{
	                    board[yCoordinate][xCoordinate] = null;
	                    board[yCoordinate][xUpdated] = client;
	                    CLIMessage.DisplayMessage("Player moved right", false);
	                    return GenerateUpdatedMapString();
	                }
                }
            default:
            	 CLIMessage.DisplayMessage("Player requested invalid movment", false);
                return "";
        }
    }
    public void recieveMessage(String message) throws RemoteException{

    }

	@Override
	public String updateCoordinates(IClient client, Direction direction) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerNeighbouringZone() {
		   RegisterZones();
		
	}

	@Override
	public int getID() throws RemoteException {
		return ID;
		
	}




    


}