package shared.common;

public class Message {
	public String author;
	public String content;
	
	public Message(String author, String content) {
		this.content = content;
		this.author = author;
	}
	
	@Override
	public String toString() {
		return author+": "+content;
		
	}
}
