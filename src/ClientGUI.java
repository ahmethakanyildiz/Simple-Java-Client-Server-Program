import java.awt.EventQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import java.awt.SystemColor;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.awt.event.ActionEvent;

public class ClientGUI {
	
	private JFrame frmCryptoMessenger;
	private static final String IP = "127.0.0.1";
	private static final int PORT = 9090;
	private Thread thread=null;
	private OutputStream output= null;
	private ObjectOutputStream objectOutputStream= null;
	private InputStream input= null;
	private ObjectInputStream objectInputStream= null;
	private Socket socket = null;
	private ServerConnection serverConn = null;
	private String name=null;
	private String id=null;
	private SecretKey aesKey=null;
	private SecretKey desKey=null;
	private IvParameterSpec aesIv=null;
	private IvParameterSpec desIv=null;
	private PrintStream messages = null;
	private Message encryptedMessage=null;

	public static void main(String[] args) throws IOException {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
					window.frmCryptoMessenger.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	public ClientGUI()  {
		initialize();
	}

	private void initialize() {
		
		frmCryptoMessenger = new JFrame();
		frmCryptoMessenger.setTitle("Crypto Messenger");
		frmCryptoMessenger.setBounds(100, 100, 647, 720);
		frmCryptoMessenger.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmCryptoMessenger.setResizable(false);
		frmCryptoMessenger.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server");
		lblNewLabel.setBounds(0, 0, 643, 26);
		frmCryptoMessenger.getContentPane().add(lblNewLabel);
		lblNewLabel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "", TitledBorder.LEADING, TitledBorder.TOP, null, SystemColor.inactiveCaptionText));
		
		JButton connectButton = new JButton("Connect");
		connectButton.setBounds(36, 49, 124, 26);
		frmCryptoMessenger.getContentPane().add(connectButton);
		
		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.setEnabled(false);
		disconnectButton.setBounds(164, 49, 124, 26);
		frmCryptoMessenger.getContentPane().add(disconnectButton);
		
		JPanel methodPanel = new JPanel();
		methodPanel.setBounds(298, 36, 135, 50);
		methodPanel.setLayout(new BoxLayout(methodPanel,BoxLayout.X_AXIS));
		methodPanel.setBorder(BorderFactory.createTitledBorder("Method"));
		frmCryptoMessenger.getContentPane().add(methodPanel);
		JRadioButton aes = new JRadioButton("AES");
		aes.setSelected(true);
		methodPanel.add(aes);
		JRadioButton des = new JRadioButton("DES");
		methodPanel.add(des);
		ButtonGroup methodGroup = new ButtonGroup();
		methodGroup.add(aes);
		methodGroup.add(des);
		
		JPanel modePanel = new JPanel();
		modePanel.setBounds(443, 36, 135, 50);
		modePanel.setLayout(new BoxLayout(modePanel,BoxLayout.X_AXIS));
		modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
		frmCryptoMessenger.getContentPane().add(modePanel);
		JRadioButton cbc = new JRadioButton("CBC");
		cbc.setSelected(true);
		modePanel.add(cbc);
		JRadioButton ofb = new JRadioButton("OFB");
		modePanel.add(ofb);
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(cbc);
		modeGroup.add(ofb);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBounds(10, 91, 612, 400);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 91, 612, 400);
		scrollPane.setViewportView(textArea);
		frmCryptoMessenger.getContentPane().add(scrollPane);
		
		messages = new PrintStream(new CustomOutputStream(textArea));
		
		JPanel textPanel = new JPanel();
		textPanel.setBackground(SystemColor.text);
		textPanel.setBounds(10, 497, 215, 145);
		textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.X_AXIS));
		textPanel.setBorder(BorderFactory.createTitledBorder("Text"));
		frmCryptoMessenger.getContentPane().add(textPanel);
		
		JTextPane textPane = new JTextPane();
		textPanel.add(textPane);
		
		JPanel encryptPanel = new JPanel();
		encryptPanel.setBackground(SystemColor.text);
		encryptPanel.setBounds(235, 497, 215, 145);
		encryptPanel.setLayout(new BoxLayout(encryptPanel,BoxLayout.X_AXIS));
		encryptPanel.setBorder(BorderFactory.createTitledBorder("Crypted Text"));
		frmCryptoMessenger.getContentPane().add(encryptPanel);
		
		JTextPane encryptPane = new JTextPane();
		encryptPane.setEditable(false);
		encryptPanel.add(encryptPane);
		
		JButton sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		sendButton.setBounds(460, 574, 162, 40);
		frmCryptoMessenger.getContentPane().add(sendButton);
		
		JButton encryptButton = new JButton("Encrypt");
		encryptButton.setBounds(460, 528, 162, 40);
		frmCryptoMessenger.getContentPane().add(encryptButton);
		
		JLabel status = new JLabel("Not Connected");
		status.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "", TitledBorder.LEADING, TitledBorder.TOP, null, SystemColor.inactiveCaptionText));
		status.setBounds(0, 653, 643, 30);
		frmCryptoMessenger.getContentPane().add(status);
		
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				name=JOptionPane.showInputDialog("Enter username:");
				if(name==null) return;
				while(name.equals("")) {
					name=JOptionPane.showInputDialog("Enter username:");
					if(!name.equals("")) break;
				}
				
				try {
					socket = new Socket(IP, PORT);
					output = socket.getOutputStream();
			        objectOutputStream = new ObjectOutputStream(output);
			        input = socket.getInputStream();
					objectInputStream = new ObjectInputStream(input);
			        serverConn = new ServerConnection(objectInputStream, objectOutputStream, messages, aes, cbc);
					
					thread = new Thread(serverConn);
					thread.start();
					id=serverConn.getID();
					aesKey=serverConn.getAesKey();
					desKey=serverConn.getDesKey();
					aesIv=serverConn.getAesIv();
					desIv=serverConn.getDesIv();
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				connectButton.setEnabled(false);
				disconnectButton.setEnabled(true);
				textPane.setText("");
				encryptPane.setText("");
				status.setText("Connected: "+name);
			}
		});
		
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serverConn.setRunning(false);
				
				try {
					Message disconnect= new Message("disconnect",name,id,null);
					objectOutputStream.writeObject(disconnect);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				disconnectButton.setEnabled(false);
				connectButton.setEnabled(true);
				sendButton.setEnabled(false);
				status.setText("Not Connected");
			}
		});

		encryptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String method;
				String mode;
				if(aes.isSelected()) method="AES";
				else method="DES";
				if(cbc.isSelected()) mode = "CBC";
				else mode ="OFB";
				String ciphertext=null;
				if(!textPane.getText().equals("") && textPane.getText()!=null) {
					try {
						ciphertext=encrypt(method,mode,textPane.getText());
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
							| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
						e.printStackTrace();
					}
					encryptedMessage=new Message("message",name,id,ciphertext);
					encryptPane.setText(encryptedMessage.getText());
					if(disconnectButton.isEnabled()) sendButton.setEnabled(true);
				}
				
			}
		});
		
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					objectOutputStream.writeObject(encryptedMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	private String encrypt(String method, String mode, String plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher;
		if(method.equals("AES")) {
			if(mode.equals("CBC")) {
				cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
			}
			else {
				cipher=Cipher.getInstance("AES/OFB/PKCS5Padding");
			}
			cipher.init(Cipher.ENCRYPT_MODE, aesKey, aesIv);
		}
		else {
			if(mode.equals("CBC")) {
				cipher=Cipher.getInstance("DES/CBC/PKCS5Padding");
			}
			else {
				cipher=Cipher.getInstance("DES/OFB/PKCS5Padding");
			}
			cipher.init(Cipher.ENCRYPT_MODE, desKey, desIv);
		}
		byte[] encVal=cipher.doFinal(plaintext.getBytes());
		String ciphertext= Base64.getEncoder().encodeToString(encVal);
		return ciphertext;
	}
	
}
