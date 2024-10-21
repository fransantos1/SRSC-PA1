package DSTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.Properties;

import javax.swing.JFileChooser;


/*
    CONFIDENTIALIY: ALG/MODE/PADDING
    SYMMETRIC_KEY: key in hexadecimal value with the required key size
    SYMMTRIC_KEY_SIZE: integer representing the number of BITS
    IV_SIZE: integer or NULL
    IV: hexadecimal value or NULL
    INTEGRITY: HMAC or H
    H: definition of secure ash Function or NULL
    MAC: definition of MAC (HMAC or CMAC algorithms)
    MACKEY: mackey value in hexadecimal with rquired keysize or NULL
    MACKEY_SIZE: integer representing the size of the MACKEY in BITS
 */

public class DSTP {

    private final static String defaultPathToConfig = "./cryptoconfig.txt";
    private String CONFIDENTIALIY;
    private byte[] SYMMETRIC_KEY;
    private int SYMMTRIC_KEY_SIZE;
    private int IV_SIZE;
    private byte[] IV;
    private String INTEGRITY;
    private String H;
    private String MAC;
    private byte[] MACKEY;
    private int MACKEY_SIZE;

    public static void main(String[] args) throws IOException {
        
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(defaultPathToConfig)) {
             prop.load(fis);
         } catch (FileNotFoundException ex) {
             // FileNotFoundException catch is optional and can be collapsed
         } catch (IOException ex) {
         }
         
         
     System.out.println(prop.getProperty("CONFIDENTIALIY"));
        send( Utils.toByteArray("123Testing123") );
        
     // expected port of the server (default = 1234)	
     // the server hostname or IP address
         // default is localhost, 127.0.0.1
     String serverhost="localhost"; 
         int port;
     if (args.length == 2)
            {
            port = Integer.parseInt(args[0]);
            serverhost = args[1];
            }
     else
         {
         System.out.println("Use: java ClientApp <port> <serverhost>");
         System.exit(1);
         }
     }

    private void ready(int port, String host) throws IOException {

                 

        
        System.out.println("Select the file to send");
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);
            byte[] fileByteArray = new byte[20];
        	

            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }


/*
         DSTP Packet format
    DSTP Header:
    Version(16bit) | Release(8bit) | Payload Len(16bit) 

    DSTP Payload:
    Encrypted( Sequence Number(16bit) | DATA(variable) | H(variable) )
    Encrypted( Sequence Number(16bit) | DATA(variable) | HMAC(variable) )
 */

    private void receave() throws IOException {
        
    }
//    private void send(DatagramSocket socket, byte[] inByteArray, InetAddress address, int port) throws IOException {

static private void  send(byte[] inByteArray) throws IOException {
        String serverHost = "localhost";
        int port = 2141;

        //Version(16bit) | Release(8bit) | Payload Len(16bit)  
        int sequenceNumber = 4; 


        String exampleHash = "966983bd574c04b5f682408f8af8c914b332df63c4de8067d9d2cec9c95727b4";
        byte[] hash = Utils.toByteArray(exampleHash);
        System.out.println("Message : " + Utils.toString(inByteArray));
        System.out.println("Hash : " + Utils.toString(hash));
        System.out.println("HashLen : " + hash.length);
        

        byte[] DSTPPayload = new byte[2 + inByteArray.length  + hash.length]; //+2 is for the sequence number
        
        DSTPPayload[0] = (byte) ((sequenceNumber >> 8) & 0xFF);
        DSTPPayload[1] = (byte) (sequenceNumber & 0xFF); 
        System.out.println("SequenceNumber : " + sequenceNumber);

        
        System.arraycopy(inByteArray, 0, DSTPPayload, 2, inByteArray.length);
        System.arraycopy(hash, 0, DSTPPayload, 2+inByteArray.length, hash.length);


        int version = 0x009; 
        int release = 0x05; 
        int payloadLen = DSTPPayload.length;

        byte[] DSTPHeader = new byte[5];
        DSTPHeader[0] = (byte) ((version >> 8) & 0xFF); 
        DSTPHeader[1] = (byte) (version & 0xFF);        
        // Insert the Release (8 bits)
        DSTPHeader[2] = (byte) (release & 0xFF);   
        // Insert the Payload Length (16 bits)
        DSTPHeader[3] = (byte) ((payloadLen >> 8) & 0xFF); 
        DSTPHeader[4] = (byte) (payloadLen & 0xFF);
        

        byte[] fullPayLoad = new byte[DSTPPayload.length + DSTPHeader.length];
        System.arraycopy(DSTPHeader, 0, fullPayLoad, 0, DSTPHeader.length);
        System.arraycopy(DSTPPayload, 0, fullPayLoad, DSTPHeader.length, DSTPPayload.length);

        
        while(true){

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(serverHost);
            
            DatagramPacket packet = new DatagramPacket(DSTPHeader, DSTPHeader.length, address, port);
            socket.send(packet);
            packet = new DatagramPacket(DSTPPayload, DSTPPayload.length, address, port);
            socket.send(packet);
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
            break;
        }
        }


        //HEADER
        int extractedVersion = ((fullPayLoad[0] & 0xFF) << 8) | (fullPayLoad[1] & 0xFF); // Combine to get version
        int extractedRelease = fullPayLoad[2] & 0xFF; // 8-bit release value
        int extractedPayloadLen = ((fullPayLoad[3] & 0xFF) << 8) | (fullPayLoad[4] & 0xFF); // Combine to get payload length
        System.out.println("Extracted Version: " + extractedVersion);
        System.out.println("Extracted Release: " + extractedRelease);
        System.out.println("Extracted Payload Length: " + extractedPayloadLen);

        //PAYLOAD
        int extractedSequenceNumber = ((fullPayLoad[5] & 0xFF) << 8) | (fullPayLoad[6] & 0xFF); // Sequence number
        byte[] messageBytes = new byte[extractedPayloadLen -2- hash.length]; // Adjust as needed
        System.arraycopy(fullPayLoad, 7, messageBytes, 0, messageBytes.length); // Adjust offset if needed
        byte[] hashIn = new byte[hash.length];
        System.arraycopy(fullPayLoad, 7+messageBytes.length, hashIn, 0, hashIn.length); // Adjust offset if needed
        System.out.println("Extracted Sequence Number: " + extractedSequenceNumber);
        System.out.println("Extracted Message: " + Utils.toString(messageBytes));
        System.out.println("Extracted Hash: " + Utils.toString(hashIn));











	      
/* 
            DatagramPacket sendPacket = new DatagramPacket(DSTPHeader, DSTPHeader.length, address, port);
	    // Send the data (block)
            socket.send(sendPacket);
            System.out.println("Sent: Sequence number = " + sequenceNumber);

	    // Control the receivd ack	    
            boolean ackRec; 

            while (true) {
		// Create packet for datagram ack
                byte[] ack = new byte[2]; 
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try {
		    // Wait server to send the ack		    
                    socket.setSoTimeout(50); 
                    socket.receive(ackpack);
		    // Lets control the received sequence number
                    ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
		    // ok received and ack		    
                    ackRec = true;
                } catch (SocketTimeoutException e) {
                    System.out.println("Expired time out waiting for ack");
		    // Not receive an ack		    
                    ackRec = false; 
                }

		// If recived correct ack ...

                if ((ackSequence == sequenceNumber) && (ackRec)) {
                    System.out.println("Ack received: Sequence Number = " + ackSequence);
                    break;
		    //
                }
		// Was not received, will resend the packet (and block)
                else {
                    socket.send(sendPacket);
                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
                }
            }*/
        
    }
  

    



}
