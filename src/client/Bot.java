package client;

import shared.common.Player;

public class Bot {
	public static void main(String[] args) {

		Player player = new Player();
		Client client = new Client(player);
		client.register();
		player.setClient(client);
		player.autoRun();


	}
}
