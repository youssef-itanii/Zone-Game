package shared.common;

public class Message {
	
	
	public enum Type{
		INPUT_REQ,
		RESP
	}
	
	public String author;
	public String content;
	public Type type;

	
	public Message(String author, String content) {
		this.content = content;
		this.author = author;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return author+": "+content;
		
	}
	
}
