// Programmer: Rron Tenezhdolli
// COSC 439/522, W '23
// Project Description: Server program with main thread handling shared data structures for clients and other thread for handling client threads
// File name: "rte_TCPServer.java"
// When running this program you can either give it a command line argument for the port number
// or go with the default port number 22700
// Command line argument is formatted as follows: java TCPServer -p 22700
//

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

//main thread class handling list of strings and list of clients
public class rte_TCPServer{													//class static variables to be used by different threads
	private static ServerSocket servSock;
	private static int portnum = 22700;
	public static File chatfile;
	public static PublicKey publicKey;
	private static PrivateKey privateKey;
	public static ArrayList<String> chats = new ArrayList<>();			//storing chats in a static list
	public static ArrayList<ClientHandler> clients = new ArrayList<>();		//storing users in a static list
	public static Map<Socket,PublicKey> clientKeyMap = new HashMap<>();
	
	public static void main(String[] args) throws NoSuchAlgorithmException{
		
		try{
			//getting ready for command line inputs
			String terminalPortnum = "";
			Scanner terminalInput = new Scanner(System.in);
			
			// handling input for command line
			for(int i = 0; i < args.length; i++) {
					if(args[i].contains("-p") ) {
					terminalPortnum = args[i+1];					//incrementing to next index of args to receive port 
					break; 							//using labels to break out of if statement only
				}
			}
			
			terminalInput.close();		
			System.out.println("Opening port...\n");
			
			rsaKeyGeneration();
			// creating socket based on input
			if(terminalPortnum.equals("")) {									
				servSock = new ServerSocket(portnum); 
			} else {
				servSock = new ServerSocket(Integer.parseInt(terminalPortnum)); 
			}

		}
		catch(IOException e){
			System.out.println("Unable to attach to port!");
			System.exit(1);
		}
		do 
		{  
			run();
		}while (true);

	}
	
	/**Function to check if portnumber is formatted correctly
	 * @param String portnumber
	 * @return boolean
	 */
	public static boolean containsNums (String str) {
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) < '0' || str.charAt(i) > '9') {
				return false;
			}
		}
		return true;
	}
	
	public static void rsaKeyGeneration() throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair pair = generator.generateKeyPair();
		rte_TCPServer.privateKey = pair.getPrivate();
		rte_TCPServer.publicKey  = pair.getPublic();	
	}
	
	/**
	 * Run program to accept the incoming socket from the client 
	 * Establishes connection with the client and sets up BufferedReader and PrintWriter
	 * Server also sets up PrintWriter and scanner for the rte_chat file
	 * Server communicated with the client and keeps track of the session time with System.nanoTime()
	 * @return void
	 */
	private static void run(){
		Socket link = null; 
		try {
			// Put the server into a waiting state
			link = servSock.accept(); 
			String hostName = InetAddress.getLocalHost().getHostName();
			System.out.println("Client has estabished a connection to " + hostName);

			//checking if room is empty to create a chat file
			if(clients.size() == 0) {
				chatfile = new File("rte_chat.txt");
			}		
			//starting a new client connection
			newClient(link);

		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Function that takes link and creates a new client. Also adds client to shared list and starts
	 * @param link
	 * @return void
	 */
	public static synchronized void newClient(Socket link) {
		ClientHandler client = new ClientHandler(link);
		clients.add(client);
		client.start();
	}
	
	/**
	 * Function that takes a message and adds it to the list holding chat file contents. 
	 * We send the message to all clients except for the one that sent it
	 * @param msg
	 * @param client
	 * @return void
	 */
	public static synchronized void broadcast(String msg, ClientHandler client) {
		chats.add(msg);
		for(ClientHandler clientel : clients) {  //iterating through clients
			if(clientel != client) {
				clientel.sendMessageOverStream(msg);
			}
		}
	}
}

/**
 * Thread that takes care of all incoming sender threads from client.
 * Handles input and output and broadcasts messages appropriately
 * Creates a chat file when user connections to empty room
 * @author rron_
 *
 */
class ClientHandler extends Thread
{
	private Socket clientThread;  
	private BufferedReader dataFromClient;
	private PrintWriter dataToClient;
	private DataInputStream keyStreamFromClient;
	private DataOutputStream keyStreamToClient;
	private String user;

	public ClientHandler(Socket s){

		//set up the socket
		clientThread = s;
		try{
			//Set up input and output streams for socket
			dataFromClient = new BufferedReader(new InputStreamReader(clientThread.getInputStream())); 
			dataToClient = new PrintWriter(clientThread.getOutputStream(),true); 
			keyStreamFromClient = new DataInputStream(clientThread.getInputStream());
			keyStreamToClient = new DataOutputStream(clientThread.getOutputStream());
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * Simple send method that sends parameter msg to client thread
	 * @param msg
	 */
	// Data stream to client, must encrypt
	public void sendMessageOverStream(String msg) {	
		dataToClient.println(msg);
	}
	

	public PublicKey handleKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		int publicKeyLength = keyStreamFromClient.readInt();

	    // Read the public key bytes
	    byte[] publicKeyBytes = new byte[publicKeyLength];
	    keyStreamFromClient.readFully(publicKeyBytes);
	    // Reconstruct the public key
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Change to the appropriate algorithm if needed
	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
	    PublicKey receivedPublicKey = keyFactory.generatePublic(keySpec);
	    
	    return receivedPublicKey;
	}
	
	//this method is called automatically when a client thread starts.
	public void run(){
		try {
			//setup writer for appending and reader for reading
			FileWriter chatLogFile = new FileWriter(rte_TCPServer.chatfile, true);
			BufferedReader readChatFile = new BufferedReader(new FileReader(rte_TCPServer.chatfile.getAbsolutePath()));
			Scanner scannerForChatFile = new Scanner(rte_TCPServer.chatfile);			
		
			byte[] publicKeyBytes = rte_TCPServer.publicKey.getEncoded();
			
			int publicKeyLength = publicKeyBytes.length;
			
			keyStreamToClient.writeInt(publicKeyLength);
			keyStreamToClient.write(publicKeyBytes);
			keyStreamToClient.flush();
			
			
			//add client and public key to map		
			rte_TCPServer.clientKeyMap.put(this.clientThread, handleKeys());
			
			System.out.println(rte_TCPServer.clientKeyMap.get(this.clientThread));
			
			
			//setting up user name
			String username = dataFromClient.readLine();	
			user = username;
			
			//if a new user joins a session, the entire chat contents gets echo'd to them
			String textFromChatFile = "";
			try {
				if(rte_TCPServer.clients.size() > 1) { //checking for users using shared data structure
					if(readChatFile.ready()) {
						while(textFromChatFile != null) {
							sendMessageOverStream(textFromChatFile);
							textFromChatFile = scannerForChatFile.nextLine();
						}
					}
				}else {
					textFromChatFile = "NONE";
				}
			}catch (Exception e) {

			}finally { 
				//case where there is no need to send old chat contents
				scannerForChatFile.close();
				
				//New user joining
				chatLogFile.write(username + " connected to the chatroom\n");  //logging to chat when a connection is made
				rte_TCPServer.broadcast(username + " connected to the chatroom", this); //broadcasting to other users that somebody joined
				
				//handling and processing incoming data
				String messageFromClient = dataFromClient.readLine(); 
				
				
				while (!messageFromClient.equals("DONE")){
					//System.out.println(username + ": " + messageFromClient);			//printing out username before message					
					if(!messageFromClient.equals("DONE")) {							//checking "done" so we dont write it to chat file
						chatLogFile.write(username + ": " + messageFromClient + "\n");  //logging message
						rte_TCPServer.broadcast(username + ": " + messageFromClient, this);  //broadcasting to other users the message
					}
				
					dataToClient.flush();	
					chatLogFile.flush();
					messageFromClient = dataFromClient.readLine();
				}

				chatLogFile.write(username + " disconnected from the chatroom\n"); //logging when someone disconnects from chatroom
				chatLogFile.close();


				readChatFile.close();			
				dataToClient.close();
				dataFromClient.close();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		finally{
			try{
				rte_TCPServer.broadcast(user + " disconnected from the chatroom", this); //broadcasting that a user left the chatroom
				System.out.println(user + " disconnected from the chatroom");  //printing to the server that a user left the chatroom
				rte_TCPServer.clients.remove(this);		//removing that user that disconnected from shared data structure

				if(rte_TCPServer.clients.size() == 0) {  //checking if room is empty so we destroy file
					rte_TCPServer.chatfile.delete();
				}
				clientThread.close();   //closing client

			}

			catch(IOException e){
				System.out.println("Unable to disconnect!");
				System.exit(1);
			}
		}

	}
}


