import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable {
	private String type;
	private String name;
	private String id;
	private String text = null;
	
	public Message(String t, String n, String i, String txt) {
		type=t;
		name=n;
		id=i;
		if(type.equals("message")) {
			text=txt;
		}
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}
	
}
