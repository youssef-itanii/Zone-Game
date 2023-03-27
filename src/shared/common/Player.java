package shared.common;

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
	public int x = 0;
	public int y = 0;
	
	public int zoneID = -1;
	String map = "0 0 0 0 0 0 0 0 0 0  = 0 0 0 0 0 P 0 0 0 0 = 0 0 P 0 0 0 0 0 0 0 = 0 0 0 0 0 0 0 0 P 0 = 0 0 0 0 0 0 0 P 0 0";
	
	private Client client;
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setCoordinates(int x , int y) {
		this.x = x;
		this.y = y;
	}

	public void logout() {
		client.unregister();
		System.exit(0);
	}
	
	public void move(String input) {
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
				return;
		}
//		String mapResp = client.requestMovement(direction);
//		if(!mapResp.equals("")) {
//			map = mapResp;
			x = future_x;
			y = future_y;
//		}
	}
	
	public void displayMap() {
		String[] sections = map.split(" = ");
		String mapToDisplay = "";
		for(int row = 0; row < sections.length; row ++) {
			
			String[] cells = sections[row].split(" ");
			
			for(int col = 0; col < cells.length; col++) {
				if(row >= 0 && col >= 0) {
					//If it is your current position 
					if(row == y && col == x) {
						mapToDisplay+= YOU;
					}
					//If neighbors
					else if((row == y && (col == x +1 || col == x -1)) || (col == x && (row == y +1 || row == y -1))) {
						if(cells[col].equals("P")) {
							mapToDisplay+= OTHER_PLAYER;
							continue;
						}
						
						mapToDisplay += LIGHT_AREA;

					}
					else {
						mapToDisplay+= DARK_AREA;
					}
					
				}
			
			}
			mapToDisplay+="\n";
		}
		System.out.print("\033[H\033[2J");  
		System.out.flush();  
		System.out.println("\u001B[1;31m====================  ZONE "+zoneID+"  ====================\u001B[0m");
		System.out.println("\u001B[1;31m====================  YOUR ID: "+client.ID+"  ====================\u001B[0m");
		System.out.println(mapToDisplay);
		
	}
	
	
	public void requestAvaialableZones() {
		String mess = client.requestAvaialableZones();
		startZoneSelection(mess);
	}
	
	public void processMessage(String message) {
		
	
		if(message.contains("[Zone-select]")) {
			startZoneSelection(message);
		}
		else {
			CLIMessage.DisplayMessage(message, false);
		}
	}
	
	public void startZoneSelection(String message) {
		System.out.println(message);
		
		Scanner scanner = new Scanner(System.in);
		int input = -1;
		while(zoneID == -1 && input!=-2) {
			System.out.print("Enter the zone ID: ");
			input = scanner.nextInt();
			try {
				zoneID = client.registerToZone(input);
			}catch(Exception ex) {
				CLIMessage.DisplayMessage("Unable to register you in the zone. Try another one within the range", false);
			}
		}
		if(input == -2) {
			CLIMessage.DisplayMessage("Exiting...", false);
			System.exit(0);
		}
		
		CLIMessage.DisplayMessage("Registered to zone "+zoneID, false);
		CLIMessage.DisplayMessage("Starting game... ", false);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
