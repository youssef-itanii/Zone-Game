package zone;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.ArrayList;

import shared.remote_objects.IManager;
import shared.remote_objects.IZone;
import shared.common.AppConfig;
import shared.common.CLIMessage;
import shared.common.Player;
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
    private String SERVER;
    private int PORT;
    private String MANAGER_NAME = "Manager";
	private int MAX_ZONES;
	//Zone size
    private int N;
    private int ZONES_PER_ROW;
    private Registry registry;
    private int index;
    //===========================================================================
    public Zone() {
    	try {
			UnicastRemoteObject.exportObject(this , 0);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to export zone", true);
		}
    
        try {
			registry = LocateRegistry.getRegistry(SERVER, PORT);
		} catch (RemoteException e) {
			CLIMessage.DisplayMessage("Unable to locate registry in zone", true);
		}
        
        
        initZoneConfig();
    	clientList = new ArrayList<IClient>();
        board = new IClient[N][N]; // Initialize the zone nodes
        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                board[i][j] = null;
            }
        }
        
        //Register with manager node
        RegsiterManagerNode(); 
        
        //Set up connections with the left and upper zones
        connectToPreviousZones();
     
    }

    private void initZoneConfig() {
    	N = AppConfig.getZoneSize();
    	MAX_ZONES = AppConfig.getNumberOfZones();
    	ZONES_PER_ROW = AppConfig.getZonePerRow();
    	while(ZONES_PER_ROW == 0) {
    		ZONES_PER_ROW = AppConfig.getZonePerRow();

    	}
    	SERVER= AppConfig.getServerAddress();
    	PORT=AppConfig.getPort();
    	
    	CLIMessage.DisplayMessage("==============================", false);
    	CLIMessage.DisplayMessage("Configuration set:", false);
    	CLIMessage.DisplayMessage("Zone size: "+N, false);
    	CLIMessage.DisplayMessage("Max zones: "+MAX_ZONES, false);
    	CLIMessage.DisplayMessage("Zones per row: "+ZONES_PER_ROW, false);
    	CLIMessage.DisplayMessage("Server: "+SERVER, false);
    	CLIMessage.DisplayMessage("Port: "+PORT, false);
    	CLIMessage.DisplayMessage("==============================\n", false);
    }
    
    
    //connects to the previously created zones located to the left and above the zone
    private void connectToPreviousZones() {
		zoneLeft = connectToNewZone(-1);
		zoneUp = connectToNewZone(-ZONES_PER_ROW);
    }
    //===========================================================================
    /**
     * Register zone in the Manager node and binds the zone to the registry
     */
    public void RegsiterManagerNode(){
        try {
            manager = (IManager) registry.lookup(MANAGER_NAME);
            this.ID = manager.register(this);
            CLIMessage.DisplayMessage("Found manager and registered with ID "+ID, false);
            try {
				registry.bind("Zone-"+ID, this);
				CLIMessage.DisplayMessage("Binded Zone-"+ID, false);

			} catch (AlreadyBoundException e) {
				 CLIMessage.DisplayMessage("Unable to bind zone to registry", true);
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
		CLIMessage.DisplayMessage("Index set "+index, false);
	}
	
    //===========================================================================
    /**
     * Add client to a zone
     */
	@Override
    public void register(IClient client , int row , int col) {
		//If the row and column were not set, select a random location
		if(row == -1 || col == -1) {
			Random rand = new Random();
			
			col = rand.nextInt(N);
			row = rand.nextInt(N);
			//Keep on trying until we get a free cell
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
        broadcastMap(client);
    }
    //===========================================================================
    /**
     * Remove the registered client from the zone
     * @param client
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
        broadcastMap(client);
        
    }
    
    private void unregisterDisconnectedUser(IClient client , int row , int col) {
		CLIMessage.DisplayMessage("Unable to communicate with client. Unregistering client...", false);
    	if(clientList.contains(client)) {
    		clientList.remove(client);
    	}
    	
    	board[row][col] = null;
    	broadcastMap(client);
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
     */
   
    private String GenerateUpdatedMapString() {
		String generatedMap = "";
		for (int i = 0; i < N; i++) {     
            for (int j = 0 ;j < N; j++) {
                    if(board[i][j] == null)
                    	generatedMap+="0 ";
					else
						try {
							generatedMap+=((char)board[i][j].getID())+" ";
						} catch (RemoteException e) {
							generatedMap+="? ";
							unregisterDisconnectedUser(board[i][j], i, j);
						}
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
    		return true;
    	}
		return false;
 
    }
    //===========================================================================
    private String movePlayerToNewZone(int col , int row , IZone targetZone, IClient client , Player.Direction direction){
    	CLIMessage.DisplayMessage("Attempting to move to new zone", false);
    	//attempt to check if the cell is free
    	try {
			if(!targetZone.cellIsEmpty(row, col)) {
				return "";
			}
		} catch (RemoteException | NullPointerException ex) {
			//Handle unavailable zones based on the direction the user is taking
			switch (direction) {
			case LEFT: 
				targetZone = connectToNewZone(-1);
				zoneLeft = targetZone;
				break;
				
			case RIGHT: 
				targetZone = connectToNewZone(1);
				zoneRight = targetZone;
				break;
			case UP: 
				targetZone = connectToNewZone(-ZONES_PER_ROW);
				zoneUp = targetZone;
				break;
			case DOWN: 
				targetZone = connectToNewZone(ZONES_PER_ROW);
				zoneDown = targetZone;
				break;
			
			default:
				return "";
			}
		}
    	//If there is no new zone, then return null and do nothing
    	if(targetZone == null) return "";
		try {
			if(!targetZone.cellIsEmpty(row, col)) {
				return "";
			}
		} catch (RemoteException e2) {
			//If the newly connected zone disconnects, then just return an empty string
			CLIMessage.DisplayMessage("ERROR: Cannot communicate with target zone", false);
			return "";
		}
    	unregister(client);
    	try {
			targetZone.register(client , row , col);
			targetZone.placePlayer(client, row, col); // Move to the new zone
		} catch (RemoteException e1) {
			CLIMessage.DisplayMessage("ERROR:Cannot communicate with target zone", false);
			return "";
		}
        try {
			client.setZone(targetZone); // Set client's new zone to target zone
			CLIMessage.DisplayMessage("*Player left the zone, broadcasting updated map", false);
			//Broadcast the map first to avoid erasing the messages
			broadcastMap(client);
			sendMessageToNeighbors(row , col, client);
			return "";
		} catch (RemoteException e) {
			unregisterDisconnectedUser(client, row, col);
		}
		return ""; 
    }
    	

    //===========================================================================
    /***
     * Searches for the next available zone to connect to
     * @param offset: offset required to increment the index 
     * @return
     */
    private IZone connectToNewZone(int offset) {
    	
    	IZone newZone = null;
    	int currentIndex = index;
    	CLIMessage.DisplayMessage("\n==========================================", false);
    	CLIMessage.DisplayMessage("Searching for new zone to connect to", false);
    	
    	while((currentIndex +offset >= 0 && currentIndex+offset < MAX_ZONES)) {
    		currentIndex+= offset;
    		boolean isNewRow;
    		boolean isPreviousRow;
    		if(currentIndex == 0) {
    			isPreviousRow = false;
    			isNewRow = false;
    			
    		}
    		else {
    			isPreviousRow = ZONES_PER_ROW%(currentIndex + 1) == 0; 
    			isNewRow = ZONES_PER_ROW%(currentIndex) == 0 && index != 0;    			
    		}
    		if(offset == 1 && isNewRow) return null;
        	if(offset == -1 && isPreviousRow) return null;
    	
    		
				try {
					newZone = (IZone) registry.lookup("Zone-"+currentIndex);
					CLIMessage.DisplayMessage("ERROR: Attempting to check Zone-"+currentIndex,false);
				} catch (AccessException e1) {
					CLIMessage.DisplayMessage("ERROR: Unable to access registery", false);
					return null;
				} catch (RemoteException e1) {
					CLIMessage.DisplayMessage("ERROR: Unable to communicate with registery", false);
					return null;
				} catch (NotBoundException e1) {
					CLIMessage.DisplayMessage("ERROR: Zone-"+currentIndex+" is not bound",false);
					continue;
				}
			
    	  		
    		try {
    			if(newZone == null) {
    				CLIMessage.DisplayMessage(" NULL: Zone-"+currentIndex,false);
    				continue;
    			}
    			//To make sure that the zone is still active, try getting the ID
				int zoneId = newZone.getID();
//				if(newZone.equals(zoneDown) || newZone.equals(zoneRight)) return null;
				CLIMessage.DisplayMessage("*Connecting to Zone-"+currentIndex,false);
				CLIMessage.DisplayMessage("\n==========================================", false);
				return newZone;
			} catch (RemoteException e) {
				CLIMessage.DisplayMessage("ERROR: Unable to communicate with Zone-"+currentIndex, false);
				CLIMessage.DisplayMessage("\n==========================================", false);
				newZone = null;
			}
    	}
    	System.out.println("FAIL: No zone found");
    	CLIMessage.DisplayMessage("\n==========================================", false);
    	return null;    

    }
    //===========================================================================
    private void sendMessageToNeighbors(int row, int col , IClient sender) {

		messageNeighbor((row - 1 >= 0) , sender, row, col,  row -1 , col);
		messageNeighbor(((row + 1 < N) ) , sender, row, col, row + 1 , col);
		messageNeighbor((col - 1 >= 0) , sender, row, col, row , col - 1);
		messageNeighbor((col + 1 < N) , sender, row,col ,row , col+1);

    }
    //===========================================================================
    /***
     * 
     * @param exp: expression that tells us if the selected row/col is within bounds
     * @param sender: client sending the message
     * @param senderRow: row where the client is located
     * @param senderCol: col where the client is located
     * @param row target: row
     * @param col target: col
     */
    private void messageNeighbor(boolean exp, IClient sender, int senderRow , int senderCol , int row , int col) {
    	if(exp) {
    		IClient neighbor = board[row][col];
    		if(neighbor!= null) {
    			int neighborID;
    			try {
    				CLIMessage.DisplayMessage("Sending message to neighbor @ row"+row+" col "+col, false);
    				neighbor.recieveMessage("Hello" , "Player "+(char)sender.getID());
    				neighborID = neighbor.getID();
//    				sender.recieveMessage("Hello!", "Player "+neighborID);
    				
    			} catch (RemoteException e) {
    				unregisterDisconnectedUser(neighbor, row, col);
    				return;
    			}
    			try {
					sender.recieveMessage("Hello!", "Player "+(char)neighborID);
				} catch (RemoteException e) {
					unregisterDisconnectedUser(sender, row, col);
    				return;
				}
    			
    			
    		}
    	}
    }
    //===========================================================================
    private void broadcastMap(IClient movingClient) {
    	String map = GenerateUpdatedMapString();
    	
    	for(IClient client: clientList) {
    		try {
    			if(client.equals(movingClient)) continue;
    			client.recieveUpdatedMap(map);
			} catch (RemoteException e) {
				CLIMessage.DisplayMessage("Unable to send message to neighboring client", false);
			}
    	}
	
    	
    }
    //===========================================================================
    private String updateBoard(IClient client , int prevRow, int prevCol , int newRow, int newCol) {
        board[prevRow][prevCol] = null;
        board[newRow][newCol] = client;
    
        broadcastMap(client);
		sendMessageToNeighbors(newRow, newCol , client);
	
    	return GenerateUpdatedMapString();
    }
    //===========================================================================
	@Override
	public String movePlayer(IClient client, Player.Direction direction) throws RemoteException{
        int xCoordinate = client.getX();
        int yCoordinate = client.getY();
        
        String eventPrefix = "# Player "+(char)client.getID();
        
        CLIMessage.DisplayMessage(eventPrefix+" requested movement", false);
        int yUpdated;
        int xUpdated;
        switch(direction){
            case UP:
                yUpdated = yCoordinate - 1;
                if(yUpdated < 0){ 
                    return movePlayerToNewZone(xCoordinate , N -1 , zoneUp , client , direction);
                    
                }
                else{ 
                    if(!playerCanMove(yUpdated, xCoordinate))
                        return "";
                    else{

                        CLIMessage.DisplayMessage(eventPrefix+" moved up", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yUpdated, xCoordinate);
                    }
                }
            case DOWN:
                yUpdated = yCoordinate + 1;
                if(yUpdated >= N){
                    return movePlayerToNewZone(xCoordinate , 0 , zoneDown , client, direction);
             
                }
                else{
                
                    if(!playerCanMove(yUpdated, xCoordinate))
                        return "";
                    else{
                        CLIMessage.DisplayMessage(eventPrefix+" moved down", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yUpdated, xCoordinate);
                    }
                }
    
            case LEFT:
                xUpdated = xCoordinate - 1;
                if(xUpdated < 0){
                    return movePlayerToNewZone(N-1 , yCoordinate , zoneLeft , client, direction);
                }
                else
                {
	                if(!playerCanMove(yCoordinate, xUpdated))
	                    return "";
	                else{
	                    CLIMessage.DisplayMessage(eventPrefix+" moved left", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yCoordinate, xUpdated);
	                }
                }
            case RIGHT:
                xUpdated = xCoordinate + 1;
                if(xUpdated >= N) {
                    return movePlayerToNewZone(0, yCoordinate , zoneRight , client, direction);
                }
                else
                {
	                if(!playerCanMove(yCoordinate, xUpdated))
	                    return "";
	                else{
	                    CLIMessage.DisplayMessage(eventPrefix+" moved right", false);
                        return updateBoard(client , yCoordinate , xCoordinate , yCoordinate, xUpdated);
	                }
                }
            default:
            	 CLIMessage.DisplayMessage(eventPrefix+" requested invalid movment", false);
                return "";
        }
    }
    //===========================================================================
	@Override
	public int getID() throws RemoteException {
		return ID;
		
	}
    //===========================================================================
	@Override
	public boolean cellIsEmpty(int row, int col) throws RemoteException {
		return board[row][col] == null;
	}

	@Override
	public String getZonesMap() throws RemoteException {
		int upZoneID = -1;
		int downZoneID = -1;
		int leftZoneID = -1;
		int rightZoneID = -1;
		
		String map = "";
		try {	
		
			upZoneID = zoneUp.getID();
			
		} catch (RemoteException | NullPointerException e) {
			zoneUp = connectToNewZone(-ZONES_PER_ROW);
			if(zoneUp != null)
				upZoneID = zoneUp.getID();
		}
		
		
		try {	
			downZoneID = zoneDown.getID();
			
		} catch (RemoteException | NullPointerException e) {
			zoneDown = connectToNewZone(ZONES_PER_ROW);
			if(zoneDown != null)
				downZoneID = zoneDown.getID();	
		}
		
		try {	
			leftZoneID = zoneLeft.getID();
		} catch (RemoteException  | NullPointerException e) {
			zoneLeft = connectToNewZone(-1);
			if(zoneLeft != null)
				leftZoneID = zoneLeft.getID();
		}
		
		try {	
			rightZoneID = zoneRight.getID();
			
		} catch (RemoteException  | NullPointerException e) {
			zoneRight = connectToNewZone(1);
			if(zoneRight != null)
				rightZoneID = zoneRight.getID();
		}
		
		if(upZoneID!=-1) {
			map+="\tz"+upZoneID+"\n";
			map+= "\t|\n";
		}
		if(leftZoneID!=-1) {
			map+="z"+leftZoneID+"----";
			map+="YOU";
		}
		if(leftZoneID == -1) {
			map+="\t YOU";
		}
		if(rightZoneID != -1) {
			map+="----z"+rightZoneID+"\n";
		}
		if(rightZoneID == -1) {
			map+="\n";
		}
		
		if(downZoneID!=-1) {
			map+= "\t|\n";
			map+="\tz"+downZoneID;
		}
	
		return map;
	}




    


}