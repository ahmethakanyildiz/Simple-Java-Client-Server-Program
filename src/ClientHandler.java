import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

	private BufferedWriter bWriter;
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private ArrayList<ClientHandler> clients;
	private String id=null;
	private boolean running=true;

	public ClientHandler(BufferedWriter bWriter, ObjectInputStream in, ObjectOutputStream out, ArrayList<ClientHandler> clients, String i) throws IOException {
		this.clients = clients;
		this.bWriter=bWriter;
		id=i;
		objectInputStream = in;
		objectOutputStream = out;
	}

	@Override
	public void run() {
		try {
			while (running) {
				Message request= (Message) objectInputStream.readObject();
				if(request.getType().equals("disconnect")) {
					for(ClientHandler aClient: clients) {
						if((aClient.id).equals(request.getId())) {
							if(id.equals(aClient.id)) running=false;
							Message disconnected= new Message("disconnected",null,null,null);
							aClient.objectOutputStream.writeObject(disconnected);
							clients.remove(aClient);
							break;
						}
					}
				}
				else {
					bWriter.append(request.getName()+"\n"+request.getText()+"\n");
					bWriter.flush();
					System.out.println(request.getName()+"\n"+request.getText());
					outToAll(request);
				}
			}
		} catch (Exception e) {
			try {
				for(ClientHandler aClient: clients) {
					if((aClient.id).equals(id)) {
						clients.remove(aClient);
						break;
					}
				}
				objectInputStream.close();
				objectOutputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void outToAll(Message message) {
		for(ClientHandler aClient: clients) {
	        try {
				aClient.objectOutputStream.writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
