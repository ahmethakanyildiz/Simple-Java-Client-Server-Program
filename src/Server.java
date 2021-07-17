import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Server {

	private static final int PORT = 9090;

	private static ArrayList<ClientHandler> clients = new ArrayList<>();
	private static ExecutorService pool = Executors.newFixedThreadPool(25);
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		
		File file = new File("log.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        
        FileWriter fileWriter = new FileWriter(file, true);
        BufferedWriter bWriter = new BufferedWriter(fileWriter);
        
		ServerSocket server = new ServerSocket(PORT);
		
		SecretKey aesKey = KeyGenerator.getInstance("AES").generateKey();
		String aesKeyEncoded = Base64.getEncoder().encodeToString(aesKey.getEncoded());
		SecretKey desKey = KeyGenerator.getInstance("DES").generateKey();
		String desKeyEncoded = Base64.getEncoder().encodeToString(desKey.getEncoded());
		
		SecureRandom rng = new SecureRandom();
		IvParameterSpec aesIv=createRandomIV(16,Optional.of(rng));
		String encodedAesIv = Base64.getEncoder().encodeToString(aesIv.getIV());
		IvParameterSpec desIv=createRandomIV(8,Optional.of(rng));
		String encodedDesIv = Base64.getEncoder().encodeToString(desIv.getIV());
		
		List<String> keys = new ArrayList<String>();
		keys.add(aesKeyEncoded);
		keys.add(desKeyEncoded);
		keys.add(encodedAesIv);
		keys.add(encodedDesIv);
		
		System.out.println("AES KEY: "+aesKeyEncoded);
		bWriter.append("AES KEY: "+aesKeyEncoded+"\n");
		System.out.println("DES KEY: "+desKeyEncoded);
		bWriter.append("DES KEY: "+desKeyEncoded+"\n");
		System.out.println("AES IV: "+encodedAesIv);
		bWriter.append("AES IV: "+encodedAesIv+"\n");
		System.out.println("DES IV: "+encodedDesIv);
		bWriter.append("DES IV: "+encodedDesIv+"\n");
		bWriter.flush();
		
		int i=0;
		while (true) {
			Socket client = server.accept();
			
			OutputStream output = client.getOutputStream();
	        ObjectOutputStream objectOutputStream = new ObjectOutputStream(output);
	        InputStream input = client.getInputStream();
			ObjectInputStream objectInputStream = new ObjectInputStream(input);
			
			objectOutputStream.writeObject(keys);
	        objectOutputStream.writeObject(Integer.toString(i));
	        
			ClientHandler newClient = new ClientHandler(bWriter, objectInputStream, objectOutputStream, clients, Integer.toString(i));
			clients.add(newClient);
			pool.execute(newClient);
			i++;
		}
		
	}
	
	public static SecretKey generateKeys(String type) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator;
		keyGenerator = KeyGenerator.getInstance(type);
		return keyGenerator.generateKey();
	}
	
	public static IvParameterSpec createRandomIV(int ivSizeBytes, Optional<SecureRandom> rng) {
        byte[] iv = new byte[ivSizeBytes];
        SecureRandom theRNG = rng.orElse(new SecureRandom());
        theRNG.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

}
