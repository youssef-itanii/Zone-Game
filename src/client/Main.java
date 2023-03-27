package client;

import java.util.Scanner;

import shared.common.Player;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);  // Create a Scanner object

		Player player = new Player();
		Client client = new Client(player);
		client.register();
		player.setClient(client);
		player.requestAvaialableZones();
		while(player.zoneID == -1) {
			
		}
		player.displayMap();
		
		
		String input = "";
		while(!input.equals("exit")) {
			System.out.println("Enter direction to move");
			 input = scanner.nextLine();  // Read user input
			 player.move(input);
			 player.displayMap();
		}
		player.logout();
		

	}
}
