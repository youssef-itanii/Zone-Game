package shared.common;

public class CLIMessage {
	public static void DisplayMessage(String message , Boolean isError) {
		System.out.println(message);
		if(isError) {
			System.exit(1);
		}
	}
	
	public static void printError(String message , Boolean exit) {
		System.out.println( "\u001B[1;47m\u001B[1;31mERROR:"+message+"\u001B[0m\u001B[0m");
		if(exit) {
			System.exit(1);	
		}
	}
	

}
