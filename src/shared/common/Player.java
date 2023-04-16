package shared.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import client.Client;

public class Player {
	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		UR, 	//Diagonal: Up Right
		UL, 	//Diagonal: Up Left
		DR, 	//Diagonal: Down Right
		DL		//Diagonal: Down Left
	}
	private String OTHER_PLAYER = "\u001B[1;47m\u001B[1;31mP\u001B[0m\u001B[0m";
	private String DARK_AREA = "\u001B[1;40m \u001B[0m";
	private String LIGHT_AREA = "\u001B[1;47m \u001B[0m";
	private String YOU = "\u001B[1;47m\u001B[1;32mU\u001B[0m\u001B[0m";
	public int x = -1;
	public int y = -1;
	public String map = "";
	public String zonesMap = "";
	private Scanner scanner;
	private Client client;
	int messageCounter = 0;
	final int MESSAGE_LIMIT = 4;
	public boolean hasZone = false;
	public List<String> messages;
	
	public Player() {
		messages = new ArrayList<String>();
	}
	//=============================================================================================
	public void start() {

		clearScreen();
		
		//Request selection of zone
		requestAvaialableZones();
		
		//Display map of zone you are registered too
		displayMap();
		
		//Take in movement input
		scanner = new Scanner(System.in);
		String input = "";
		while(!input.equals("exit")) {
			System.out.println("Enter direction to move");
			 input = scanner.nextLine();  // Read user input
			 if(input.contains("/map")){
				 client.requestZonesMap();
				 continue;
			 }
			 boolean moved = move(input);
			 if(moved) displayMap();
			 else {
				 System.out.println("\u001B[31mInvalid input: Try again.\u001B[0m");
			 }
		}
	}

	//=============================================================================================
	public void addNewMessage(String message) {
		messages.add(message);
		messageCounter++;
		CLIMessage.DisplayMessage(message, false);
		//To avoid crowding the command line, set a limit to the number of messages
		//Remove the first MESSAGE_LIMIT - 1 messages from the history list
		if(messageCounter == MESSAGE_LIMIT - 1) {
			messages.subList(0, messageCounter).clear();
			messageCounter = 0;
		}
	}
	//=============================================================================================
	public void displayMessages() {
		for(String message: messages) {
			CLIMessage.DisplayMessage(message, false);
		}
	}
	//=============================================================================================
	public void setClient(Client client) {
		this.client = client;
	}
	//=============================================================================================
	public void setCoordinates(int y , int x) {
		CLIMessage.DisplayMessage("Coodirates set x: "+x +" y: "+y, false);
		this.x = x;
		this.y = y;
	}
	//=============================================================================================
	public void logout() {
		client.unregister();
		scanner.close();
		System.exit(0);
	}
	//=============================================================================================
	public boolean move(String input) {
		Direction direction;
		int future_x = x;
		int future_y = y;
		switch(input.toLowerCase()) {
			case "w":
				direction = Direction.UP;
				future_y = y-1;
				break;
			case "a":
				direction = Direction.LEFT;
				future_x = x - 1;
				break;
			case "s":
				direction = Direction.DOWN;
				future_y = y + 1;
				break;
			case "d":
				direction = Direction.RIGHT;
				future_x = x + 1;
				break;
			default:
				return false;
		}
		String mapResp = client.requestMovement(direction);
		//"" means that the map did not update
		if(!mapResp.equals("")) {
			map = mapResp;
			x = future_x;
			y = future_y;
			client.X = x;
			client.Y = y;
			
		}
			return true;
	}
	//=============================================================================================
	public void displayMap() {
		String[] sections = map.split(" = ");
		String mapToDisplay = "==";
		for(int row = 0; row < sections.length; row ++) {
			
			mapToDisplay+="=";
		}
		mapToDisplay+="\n";
		for(int row = 0; row < sections.length; row ++) {
			mapToDisplay+="|";
			String[] cells = sections[row].split(" ");
			
			for(int col = 0; col < cells.length; col++) {
				if(row >= 0 && col >= 0) {
					//If it is your current position 
					if(row == y && col == x) {
						mapToDisplay+= markAsYourself();
					}
					//If neighbors
					else if((row == y && (col == x +1 || col == x -1)) || (col == x && (row == y +1 || row == y -1))) {
						if((((int)cells[col].charAt(0))>64)) {
							mapToDisplay+= markAsOtherPlayer(cells[col]);
							continue;
						}
						
						mapToDisplay += LIGHT_AREA;

					}
					else {
						mapToDisplay+= DARK_AREA;
					}
					
				}
				
			
			}
			mapToDisplay+="|\n";
		}
		mapToDisplay+="==";
		for(int row = 0; row < sections.length; row ++) {
			
			mapToDisplay+="=";
		}
		clearScreen();
		System.out.println(map);
		System.out.println("\u001B[1;92m====================  ZONE "+client.getZoneID()+"  ====================\u001B[0m");
		System.out.println("\u001B[1;92m====================  YOUR ID: "+(char)client.ID+"  ====================\u001B[0m");
		System.out.println("\u001B[1;92mYOUR POSITION: \n-ROW: "+this.y+"\n-COL: "+this.x+" \u001B[0m");
		System.out.println(mapToDisplay);
		displayMessages();
		
	}
	
	
	public void displayZonesMap() {
		clearScreen();
		CLIMessage.DisplayMessage(zonesMap,false);
		
	}
	
	
	private String markAsOtherPlayer(String character) {
		return "\u001B[1;47m\u001B[1;31m"+character+"\u001B[0m\u001B[0m";
	}
	
	private String markAsYourself() {
		char character = (char)client.ID;
		return "\u001B[1;47m\u001B[1;32m"+character+"\u001B[0m\u001B[0m";
	}
	//=============================================================================================
	public void requestAvaialableZones() {
		String mess = client.requestAvaialableZones();
		startZoneSelection(mess);
	}
	//=============================================================================================
	public void processMessage(String message) {
	
		if(message.contains("[Zone-select]")) {
			startZoneSelection(message);
		}
		else {
//			CLIMessage.DisplayMessage(message, false);
			addNewMessage(message);
		}
	}
	//=============================================================================================
	public void startZoneSelection(String message) {
		System.out.println(message);
		scanner  = new Scanner(System.in);
		
		int input = -1;
		while(client.zone == null && input!=-2) {
			System.out.print("Enter the zone ID: ");
			input = scanner.nextInt();
			client.registerToZone(input);

		}
		if(input == -2) {
			CLIMessage.DisplayMessage("Exiting...", false);
			System.exit(0);
		}
		
		CLIMessage.DisplayMessage("\n\u001B[1;92mRegistered to zone "+client.getZoneID()+"\u001B[0m", false);
		CLIMessage.DisplayMessage("\u001B[1;92mStarting game... \u001B[0m", false);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//=============================================================================================
	private void clearScreen() {
		System.out.print("\033[H\033[2J");  
		System.out.flush();
	}
}