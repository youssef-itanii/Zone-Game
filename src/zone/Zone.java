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
    private int leftZoneOffset = 1;
    private int rightZoneOffset = 1;
    private int upperZoneOffset = 1;
    private int lowerZoneOffset = 1;
    
    private List<IClient> clientList;
    private IClient[][] board;
    private IManager manager;
    private int ID;
    private final String REGISTRY_TABLE = "localhost";
    private final int REGISTRY_PORT = 1099;
    private final String MANAGER_NAME = "Manager";
	private int MAX_ZONES;
    private final int N;
    private final int zonesPerRow;
    private Registry registry;
    private int index;

    public Zone() {
    	try {
			UnicastRemoteObject.exportObject(this , 0);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to export zone", true);
		}
    	
    	N = AppConfig.getZoneSize();
    	MAX_ZONES = AppConfig.getNumberOfZones();
    	zonesPerRow = AppConfig.getZonePerRow();
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
    //===========================================================================
    /***
     * Sets position of the zone in the zone array stored in the manager
     */
	@Override
	public void setPosition(int index) throws RemoteException {
		this.index = index;	
	}
    //===========================================================================
    private void RegisterZones(){

            try {
            	zoneUp = manager.getNeighborZone(index - zonesPerRow);
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
               
            	zoneDown = manager.getNeighborZone(index + zonesPerRow);
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
            	zoneLeft = manager.getNeighborZone(index - leftZoneOffset);
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
                zoneRight = manager.getNeighborZone(index + rightZoneOffset);
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
    //===========================================================================
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
        broadcastMap();
    }
    //===========================================================================
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
        broadcastMap();
        
    }
    
    private void unregisterDisconnectedUser(IClient client , int row , int col) {
		CLIMessage.DisplayMessage("Unable to communicate with client. Unregistering client...", false);
    	if(clientList.contains(client)) {
    		clientList.remove(client);
    	}
    	
    	board[row][col] = null;
    	broadcastMap();
    }
    //===========================================================================
    public void placePlayer(IClient client, int y, int x) throws RemoteException{
        board[y][x] = client;
    }
    //===========================================================================
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
    //===========================================================================

    private boolean playerCanMove(int row , int col){
    	if(board[row][col] == null) {
    		CLIMessage.DisplayMessage("Cell requested is free", false);
    		return true;
    	}
    	CLIMessage.DisplayMessage("Cell requested is occupied", false);
		return false;
 
    }
    //===========================================================================
    private String movePlayerToNewZone(int col , int row , IZone targetZone, IClient client , Player.Direction direction){
    	try {
			if(!targetZone.cellIsEmpty(row, col)) {
				return "";
			}
		} catch (RemoteException e1) {
			switch (direction) {
			case LEFT: 
				targetZone = handleZoneDisconnect(-1);
				zoneLeft = targetZone;
				break;
				
			case RIGHT: 
				targetZone = handleZoneDisconnect(1);
				zoneRight = targetZone;
				break;
			case UP: 
				targetZone = handleZoneDisconnect(-zonesPerRow);
				zoneUp = targetZone;
				break;
			case DOWN: 
				targetZone = handleZoneDisconnect(zonesPerRow);
				zoneDown = targetZone;
				break;
			
			default:
				return "";
			}
		}
    	if(targetZone == null) return "";
		try {
			if(!targetZone.cellIsEmpty(row, col)) {
				return "";
			}
		} catch (RemoteException e2) {
			CLIMessage.DisplayMessage("Cannot communicate with target zone", false);
			return "";
		}
    	unregister(client);
    	try {
			targetZone.register(client , row , col);
			targetZone.placePlayer(client, row, col); // Move to the new zone
		} catch (RemoteException e1) {
			CLIMessage.DisplayMessage("Cannot communicate with target zone", false);
			return "";
		}
        try {
			client.setZone(targetZone); // Set client's new zone to target zone
			CLIMessage.DisplayMessage("Sending map", false);
			broadcastMap();
			sendMessageToNeighbors(row , col, client);
			return "";
		} catch (RemoteException e) {
			unregisterDisconnectedUser(client, row, col);
		}
		return ""; 
    }
    	

    //===========================================================================
    
    private IZone handleZoneDisconnect(int offset) {
    	
    	IZone newZone = null;
    	int currentIndex = index;
    	CLIMessage.DisplayMessage("Searching for new zone to connect to", false);
    	while((currentIndex >= 0 && currentIndex < MAX_ZONES)) {
    		currentIndex+= offset;
    		System.out.println("Current index "+currentIndex +" MAX "+MAX_ZONES);
    		
    		try {
				newZone = manager.getNeighborZone(currentIndex);

			} catch (RemoteException e) {
				CLIMessage.DisplayMessage("Unable to communicate with manager", false);
				return null;
			}
    		try {
    			if(newZone == null) continue;
				int zoneId = newZone.getID();
				return newZone;
			} catch (RemoteException e) {
				newZone = null;
			}
    	}
    	System.out.println("No zone found");
    	return null;    

    }
    
    private void sendMessageToNeighbors(int row, int col , IClient sender) {

		messageNeighbor((row - 1 >= 0) , sender, row, col,  row -1 , col);
		messageNeighbor((row + 1 < N) , sender, row, col, row + 1 , col);
		messageNeighbor((col - 1 >=1 ) , sender, row, col, row , col - 1);
		messageNeighbor((col + 1 < N) , sender, row,col ,row , col+1);

    }
    private void messageNeighbor(boolean exp, IClient sender, int senderRow , int senderCol , int row , int col) {
    	if(exp) {
    		IClient neighbor = board[row][col];
    		if(neighbor!= null) {
    			int neighborID;
    			try {
    				neighbor.recieveMessage("Hello" , "Player "+sender.getID());
    				neighborID = neighbor.getID();
    				
    			} catch (RemoteException e) {
    				unregisterDisconnectedUser(neighbor, row, col);
    				return;
    			}
    			try {
					sender.recieveMessage("Hello!", "Player "+neighborID);
				} catch (RemoteException e) {
					unregisterDisconnectedUser(sender, row, col);
    				return;
				}
    			
    			
    		}
    	}
    }
    
    private void broadcastMap() {

//    	List<IClient> neighboringClients = new ArrayList<IClient>();
//    	int col = -1;
//		
//    	int row = -1;
//		try {
//			row = client.getY();
//			col = client.getX();
//		} catch (RemoteException e) {
//			CLIMessage.DisplayMessage("Unable to obtain client coordinates", false);
//		}
//		if(row - 1 >= 0) {
//			neighboringClients.add(board[row-1][col]);
//		}
// 
//    	
//    	if(row + 1 < N) {
//			neighboringClients.add(board[row+1][col]);
//    	}
//    	
//		if(col - 1 >= 0) {
//			neighboringClients.add(board[row][col - 1]);
//		}
// 
//    	
//    	if(col + 1 < N) {
//			neighboringClients.add(board[row][col+1]);
//    	}
//    	
    	String map = GenerateUpdatedMapString();
//    	for(IClient neighbor : neighboringClients) {
//    		if(neighbor == null) continue;
//    		try {
//				neighbor.recieveUpdatedMap(map);
//			} catch (RemoteException e) {
//				CLIMessage.DisplayMessage("Unable to send message to neighboring client", false);
//			}
//    	}
    	
    	for(IClient client: clientList) {
    		try {
    			client.recieveUpdatedMap(map);
			} catch (RemoteException e) {
				CLIMessage.DisplayMessage("Unable to send message to neighboring client", false);
			}
    	}
	
    	
    }
    private String updateBoard(IClient client , int prevRow, int prevCol , int newRow, int newCol) {
        board[prevRow][prevCol] = null;
        board[newRow][newCol] = client;
        broadcastMap();
    
		sendMessageToNeighbors(newRow, newCol , client);
	
    	return GenerateUpdatedMapString();
    }
    
	@Override
	public String movePlayer(IClient client, Player.Direction direction) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();
        CLIMessage.DisplayMessage("Player requested movement", false);
        int yUpdated;
        int xUpdated;
        switch(direction){
            case UP:
                yUpdated = yCoordinate - 1;
                if(yUpdated < 0){  // (2) Leave the zone
                	if(zoneUp == null) return "";
                    return movePlayerToNewZone(xCoordinate , N -1 , zoneUp , client , direction);
                    
                }
                else{ // (3) Within the zone
                    if(!playerCanMove(yUpdated, xCoordinate))
                        return "";
                    else{

                        CLIMessage.DisplayMessage("Player moved up", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yUpdated, xCoordinate);
                    }
                }
            case DOWN:
                yUpdated = yCoordinate + 1;
                if(yUpdated >= N){
                	if(zoneDown==null) return "";
                    return movePlayerToNewZone(xCoordinate , 0 , zoneDown , client, direction);
             
                }
                else{
                
                    if(!playerCanMove(yUpdated, xCoordinate))
                        return "";
                    else{
                        CLIMessage.DisplayMessage("Player moved down", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yUpdated, xCoordinate);
                    }
                }
    
            case LEFT:
                xUpdated = xCoordinate - 1;
                if(xUpdated < 0){
                	if(zoneLeft==null) return "";
                    return movePlayerToNewZone(N-1 , yCoordinate , zoneLeft , client, direction);
                }
                else
                {
	                if(!playerCanMove(yCoordinate, xUpdated))
	                    return "";
	                else{
	                    CLIMessage.DisplayMessage("Player moved left", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yCoordinate, xUpdated);
	                }
                }
            case RIGHT:
                xUpdated = xCoordinate + 1;
                if(xUpdated >= N) {
                	if(zoneRight==null) return "";
                    return movePlayerToNewZone(0, yCoordinate , zoneRight , client, direction);
                }
                else
                {
	                if(!playerCanMove(yCoordinate, xUpdated))
	                    return "";
	                else{
	                    CLIMessage.DisplayMessage("Player moved right", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yCoordinate, xUpdated);
	                }
                }
            default:
            	 CLIMessage.DisplayMessage("Player requested invalid movment", false);
                return "";
        }
    }


	@Override
	public void registerNeighbouringZone() {
		   RegisterZones();
		
	}

	@Override
	public int getID() throws RemoteException {
		return ID;
		
	}

	@Override
	public boolean cellIsEmpty(int row, int col) throws RemoteException {
		return board[row][col] == null;
	}




    


}