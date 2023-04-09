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
	String map = "0 0 0 0 0 0 0 0 0 0  = 0 0 0 0 0 P 0 0 0 0 = 0 0 P 0 0 0 0 0 0 0 = 0 0 0 0 0 0 0 0 P 0 = 0 0 0 0 0 0 0 P 0 0";
	private Scanner scanner;
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
		scanner.close();
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
				return false;
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
		clearScreen();
		System.out.println("\u001B[1;92m====================  ZONE "+zoneID+"  ====================\u001B[0m");
		System.out.println("\u001B[1;92m====================  YOUR ID: "+client.ID+"  ====================\u001B[0m");
		System.out.println(mapToDisplay);

	}
}

	private void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}