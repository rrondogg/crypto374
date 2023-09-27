// Programmer: Rron Tenezhdolli
//COSC 439/522, W '23
// Project description: Multi-threaded client program that utilizes the main thread as a listener and a seperate thread as a sender
// File name: rte_TCPClient.java
// When you run this program you can input your user name, host address and port number in the command line
// You use -p for port number, -h for host address, and -u for user name
// If you don't use -p, you will be set to a default port number of 22700
// If you don't use -h, your host address will be set to your own local address
// If you don't use -h, the client program will later on prompt you again to input this field
// You can input these commands in any order:
// java TCPClient.java -p 22700 -u rron == java TCPClient.java -u rron -p 22700

import java.io.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
//main class that acts as a listener thread for the server
public class rte_TCPClient
{
	private static InetAddress host;
	private static int portnum = 22700;
	public static String username;
	public static Scanner localinput = new Scanner(System.in);
	public static PublicKey publicKey;
	private static PrivateKey privateKey;
	public static PublicKey serverPublicKey;
	
	public static void main(String[] args) throws NoSuchAlgorithmException

	{
		try {
			// variables for handling command line arguments
			String terminalPortnum = "";
			String terminalHostAddress = "";
			String terminalUsername = "";

			for(int i = 0; i < args.length; i++) {				// loop to iterate through all args
					if(args[i].contains("-p") ) {
					terminalPortnum = args[i+1];					// if the command is present we send the next index over to port cmd
				} 	
					if(args[i].contains("-h")) {
					terminalHostAddress = args[i+1];
				}
					if(args[i].contains("-u")) {
					terminalUsername = args[i+1];
				}
			}

			//handling other cases
			
			if(terminalUsername.equals("")) {								//prompting the user again for user name if they missed it
				System.out.print("Please enter a username: ");
				terminalUsername = localinput.nextLine();
				username = terminalUsername;
			} else {
				username = terminalUsername;
			}
			
			if(!terminalPortnum.equals("")) {							//checking if user inputed a port number or not
				portnum = Integer.parseInt(terminalPortnum);
			}

			if(terminalHostAddress.equals("")) {							//checking if user inputed a host address or not
				host = InetAddress.getByName(InetAddress.getLocalHost().getCanonicalHostName());
			} else {
				
				host = InetAddress.getByName(terminalHostAddress);
			}

		}
		catch(UnknownHostException e){
			System.out.println("Host ID not found!");
			System.exit(1);
		} 
		run(portnum);
	}	
	
	/**Function links the client program to the server program through the socket
	 * The main use of this part is retrieving data from the server
	 * The sending gets given to the sender thread below this method
	 * @param port
	 * @return void
	 * @throws NoSuchAlgorithmException 
	 * @throws InterruptedException 
	 */
	private static void run(int port) throws NoSuchAlgorithmException{
		Socket link = null;

		try{
			// Establish a connection to the server
			link = new Socket(host,port); 
			rsaKeyGeneration();

			// Set up input and output streams for the connection
			BufferedReader dataFromServer = new BufferedReader(new InputStreamReader(link.getInputStream()));
			DataOutputStream dataToServer = new DataOutputStream(link.getOutputStream()); 

			//Psuedo handshake
			sendPublicKeyToServer(link);
			receivePublicKeyFromServer(link);
			
			//creating sender thread
			Sender senderThread = new Sender(dataToServer);
			
			//starting the thread
			senderThread.start();
			
			//chat contents being sent if a new user connects
			String chatContentTemp;
			chatContentTemp = dataFromServer.readLine();
			while(chatContentTemp != null) {
				System.out.println(chatContentTemp);
				chatContentTemp = dataFromServer.readLine();
			}
			
			dataToServer.flush();
			
			//closing
			dataToServer.close();
			dataFromServer.close();


		}catch(ConnectException e) {
			System.out.println("\nConnection Error");
			System.out.println("Please make sure that your server is running and that your command arguments are of the following format in any order:");
			System.out.println("-p portnum -h IP -u username");
			System.out.println("The portnumber should be a number between 0-65535, should you omit this it would be defaulted to 22700");
			System.out.println("Should you omit the IP, it will be defaulted to your local address");
			
			System.exit(1);
		}
		catch(IOException e){
			e.printStackTrace();
		}

		finally{
			try{
				System.out.println("\n!!!!! Closing connection... !!!!!");
				link.close(); 
			}

			catch(IOException e){
				System.out.println("Unable to disconnect!");
				System.exit(1);
			}

		}

	}
	private static void sendPublicKeyToServer(Socket link) {
		 try {
		        DataOutputStream keyStreamToServer = new DataOutputStream(link.getOutputStream());

		        byte[] publicKeyBytes = rte_TCPClient.publicKey.getEncoded();
		        keyStreamToServer.writeInt(publicKeyBytes.length);
		        keyStreamToServer.write(publicKeyBytes);

		        keyStreamToServer.flush();        
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	}
	
	
	// Function to receive the server's public key from the server
	private static void receivePublicKeyFromServer(Socket link) {
	    try {
	    	DataInputStream keyStreamFromServer = new DataInputStream(link.getInputStream());
	    	int publicKeyLength = keyStreamFromServer.readInt();

	        byte[] publicKeyBytes = new byte[publicKeyLength];
	        keyStreamFromServer.readFully(publicKeyBytes);

	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
	        rte_TCPClient.serverPublicKey = keyFactory.generatePublic(keySpec);

	        //System.out.println("Received server public key: " + rte_TCPClient.serverPublicKey);
	    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
	        e.printStackTrace();
	    }
	}


	
	public static void rsaKeyGeneration() throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair pair = generator.generateKeyPair();
		rte_TCPClient.privateKey = pair.getPrivate();
		rte_TCPClient.publicKey  = pair.getPublic();	
	}

	 public static byte[] encryptMessage(String plainMessage, PublicKey recipientPublicKey) throws Exception {
	        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        encryptCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
	        byte[] plainMessageBytes = plainMessage.getBytes(StandardCharsets.UTF_8);
	        byte[] encryptedMessageBytes = encryptCipher.doFinal(plainMessageBytes);
	        return encryptedMessageBytes;
	    }
	
	public static String decryptMessage(byte[] cipherMessage) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Cipher decryptCipher = Cipher.getInstance("RSA");
		decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] cipherMessageBytes = decryptCipher.doFinal(cipherMessage);
		String decryptedMessage = new String(cipherMessageBytes, StandardCharsets.UTF_8);
		
		return decryptedMessage;	
	}
	
}
//The sender class reads messages typed at the keyboard, and sends them to the server
class Sender extends Thread{
	
	//constructor and printwriter object for sender thread
	private DataOutputStream dataToServer;

	
	public Sender(DataOutputStream dataToServer) throws IOException{
		this.dataToServer = dataToServer;
	}

	// overwrite the method 'run' of the Runnable interface

	// this method is called automatically when a sender thread starts.
	public void run(){
		try {
			//Set up stream for keyboard entry
			BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
			
			String plainMessageToBeSent;		
			
			dataToServer.writeUTF(rte_TCPClient.username); // sending the username to the server
			// Get data from the user, encrypt it, and send it to the server
			do{		
				plainMessageToBeSent = userEntry.readLine();
				byte[] encryptedMessageToBeSent = rte_TCPClient.encryptMessage(plainMessageToBeSent, rte_TCPClient.serverPublicKey);
				dataToServer.writeInt(encryptedMessageToBeSent.length);
				dataToServer.write(encryptedMessageToBeSent); 	
				
				
			}while (!plainMessageToBeSent.equals("DONE"));
			
			dataToServer.flush();
			userEntry.close();
			
		}catch(IOException e) {
			System.out.println("Unable to connect!");
			System.exit(1);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}