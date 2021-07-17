import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JRadioButton;

public class ServerConnection implements Runnable{

	private boolean running=true;
	private ObjectInputStream objectInputStream;
	private PrintStream messages;
	private String id;
	private List<String> keys;
	private SecretKey aesKey;
	private SecretKey desKey;
	private IvParameterSpec aesIv;
	private IvParameterSpec desIv;
	private JRadioButton aes;
	private JRadioButton cbc;
	
	
	@SuppressWarnings("unchecked")
	public ServerConnection(ObjectInputStream in, ObjectOutputStream out, PrintStream m, JRadioButton aes, JRadioButton cbc) throws IOException, ClassNotFoundException {
		this.aes=aes;
		this.cbc=cbc;
		
		messages=m;
		System.setOut(messages);
		
        objectInputStream= in;
		keys= (List<String>) objectInputStream.readObject();
		id= (String) objectInputStream.readObject();
		
		byte[] decoded = Base64.getDecoder().decode(keys.get(0));
		aesKey = new SecretKeySpec(decoded, 0, decoded.length, "AES");
		
		decoded = Base64.getDecoder().decode(keys.get(1));
		desKey = new SecretKeySpec(decoded, 0, decoded.length, "DES");
		
		decoded = Base64.getDecoder().decode(keys.get(2));
		aesIv= new IvParameterSpec(decoded);
		
		decoded = Base64.getDecoder().decode(keys.get(3));
		desIv= new IvParameterSpec(decoded);
	}

	public SecretKey getAesKey() {
		return aesKey;
	}

	public SecretKey getDesKey() {
		return desKey;
	}

	public IvParameterSpec getAesIv() {
		return aesIv;
	}

	public IvParameterSpec getDesIv() {
		return desIv;
	}

	public String getID(){
		return id;
	}
	
	public List<String> getKeys(){
		return keys;
	}
	
	public void setRunning(boolean bool){running=bool;}
	
	public String decrypt(Message encryptedMessage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher;
		
		if(aes.isSelected()) {
			if(cbc.isSelected()) {
				cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
			}
			else {
				cipher=Cipher.getInstance("AES/OFB/PKCS5Padding");
			}
			cipher.init(Cipher.DECRYPT_MODE, aesKey, aesIv);
		}
		else {
			if(cbc.isSelected()) {
				cipher=Cipher.getInstance("DES/CBC/PKCS5Padding");
			}
			else {
				cipher=Cipher.getInstance("DES/OFB/PKCS5Padding");
			}
			cipher.init(Cipher.DECRYPT_MODE, desKey, desIv);
		}
		byte[] decodedValue=Base64.getDecoder().decode(encryptedMessage.getText());
		byte[] decValue=cipher.doFinal(decodedValue);
		String decryptedMessage=new String(decValue);
		return decryptedMessage;
	}
	
	@Override
	public void run() {
		
			Message serverResponse;
			try {
				while(running) {
					serverResponse = (Message) objectInputStream.readObject();
					if(running==false) {
						if(serverResponse.getType().equals("disconnected")) {
							System.out.println("Disconnected!");
						}
						break;
					}
					System.out.println(serverResponse.getText());
					String decrypted;
					try {
						decrypted=decrypt(serverResponse);
						System.out.println(serverResponse.getName()+">"+decrypted);
					}catch(Exception e) {
						System.out.println("Error! Please check encryption method/mode!");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					objectInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

	}

}
