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
	public int x = -1;
	public int y = -1;
	public String map = "";
	private Scanner scanner;
	private Client client;
	
	public boolean hasZone = false;
	
	
	public void start() {
		clearScreen();
		
		//Request selection of zone
		requestAvaialableZones();
		
		//Display map of zone you are registered too
//		while(map == "") {}
		displayMap();
		
		//Take in movement input
		scanner = new Scanner(System.in);
		String input = "";
		while(!input.equals("exit")) {
			System.out.println("Enter direction to move");
			 input = scanner.nextLine();  // Read user input
			 boolean moved = move(input);
			 if(moved) displayMap();
			 else {
				 System.out.println("\u001B[31mInvalid input: Try again.\u001B[0m");
			 }
		}
	}
	
	public void setClient(Client client) {
		this.client = client;
	}

	public void setCoordinates(int y , int x) {
		CLIMessage.DisplayMessage("Coodirates set x: "+x +" y: "+y, false);
		this.x = x;
		this.y = y;
	}

	public void logout() {
		client.unregister();
		scanner.close();
		System.exit(0);
	}
	
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
		if(!mapResp.equals("")) {
			map = mapResp;
			x = future_x;
			y = future_y;
			client.X = x;
			client.Y = y;
			
		}
			return true;
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
		clearScreen();
		System.out.println(map);
		System.out.println("\u001B[1;92m====================  ZONE "+client.getZoneID()+"  ====================\u001B[0m");
		System.out.println("\u001B[1;92m====================  YOUR ID: "+client.ID+"  ====================\u001B[0m");
		System.out.println("\u001B[1;92mYOUR POSITION: \n-ROW: "+this.y+"\n-COL: "+this.x+" \u001B[0m");
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
		scanner  = new Scanner(System.in);
		
		int input = -1;
		while(client.zone == null && input!=-2) {
			System.out.print("Enter the zone ID: ");
			input = scanner.nextInt();
//			try {
				client.registerToZone(input);
//			}catch(Exception ex) {
//				CLIMessage.DisplayMessage("Unable to register you in the zone. Try another one within the range", false);
//				System.out.print(ex);
//			}
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
	
	private void clearScreen() {
		System.out.print("\033[H\033[2J");  
		System.out.flush();
	}
}