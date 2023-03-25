package shared.common;

public class CLIMessage {
	public static void DisplayMessage(String message , Boolean isError) {
		System.out.println(message);
		if(isError) {
			System.exit(1);
		}
	}
	

}
