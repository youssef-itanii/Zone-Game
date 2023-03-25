package shared.common;

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
	
	public int x;
	public int y;
	
	
	Client client;
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setCoordinates(int x , int y) {
		this.x = x;
		this.y = y;
	}
	
	public void move(String input) {
		Direction direction;
		switch(input.toLowerCase()) {
			case "w":
				direction = Direction.UP;
				break;
			case "a":
				direction = Direction.LEFT;
				break;
			case "s":
				direction = Direction.DOWN;
				break;
			case "d":
				direction = Direction.RIGHT;
				break;
			default:
				return;
		}
	}
}
