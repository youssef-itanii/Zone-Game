package client;

import java.util.Scanner;

import shared.common.Player;

public class Main {
	public static void main(String[] args) {

		Player player = new Player();
		Client client = new Client(player);
		client.register();
		player.setClient(client);
		player.start();


	}
}