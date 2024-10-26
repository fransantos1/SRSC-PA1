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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

    private final static String defaultPathToConfig = "./src/DSTP/cryptoconfig.txt";

    private static String ciphersuite = null;
    private static IvParameterSpec ivSpec = null;
    private static SecretKey key = null;
    private static MessageDigest hash = null;
    private static Mac hMac = null;
    private static SecretKey hMacKey = null;
    private static byte[] keyBytes = null;


    public static void main(String[] args) throws Exception {
        
        init();
    }


    private static void init() throws Exception {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(defaultPathToConfig)) {
            System.out.println("Read Properties");
             prop.load(fis);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        ciphersuite=prop.getProperty("CONFIDENTIALIY"); 
        System.out.println("Confidentiality Read: "+ ciphersuite);
        //! GCM 
        //! tem que se criar um iv novo para cada 
        //! SHASHA precisam de coisas adicionais
        
        keyBytes = Base64.getDecoder().decode(prop.getProperty("SYMMETRIC_KEY"));
        System.out.println("SymetricKey Read: "+Utils.toString(keyBytes));
        

        String symetricKey = prop.getProperty("SYMMETRIC_KEY");
        int keylen = Integer.parseInt(prop.getProperty("SYMMTRIC_KEY_SIZE"));
        System.out.println("Key: "+symetricKey+" Size: "+ keylen);
        keyBytes = new byte[keylen/8];
        for (int i = 0; i < symetricKey.length(); i += 2) {
            // Convert the pair of hex characters to a byte
            keyBytes[i / 2] = (byte) (
                (Character.digit(symetricKey.charAt(i), 16) << 4)  // First hex char
                + Character.digit(symetricKey.charAt(i + 1), 16)   // Second hex char
            );
        }
        System.out.println(" Size: "+ keyBytes.length);
        
        //key = keyBytes;
        key = new SecretKeySpec(keyBytes, ciphersuite.split("/")[0]);
        String ivHex = prop.getProperty("IV");
        byte[] ivBytes = null;
        if(!ivHex.equals("NULL")){
            int len = Integer.parseInt(prop.getProperty("IV_SIZE"));
            System.out.println("IV: "+ivHex+" Size: "+ len);
            ivBytes = new byte[len];
            for (int i = 0; i < len*2; i += 2) {
                ivBytes[i / 2] = (byte) ((Character.digit(ivHex.charAt(i), 16) << 4)
                                     + Character.digit(ivHex.charAt(i + 1), 16));
            }
            for (int i = 0; i < ivBytes.length; i++) {
                System.out.printf("0x%02X", ivBytes[i]);
                if (i < ivBytes.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(Utils.toHex(ivBytes));   
        }
        if(ivBytes != null)
            ivSpec = new IvParameterSpec(ivBytes);

        String integrity = prop.getProperty("INTEGRITY");
        if (integrity.equals("H")) {
            hash = MessageDigest.getInstance(prop.getProperty("H"));
        }else{
            hMac = Mac.getInstance(prop.getProperty("MAC"));
            byte[] macKeyBytes = Base64.getDecoder().decode(prop.getProperty("MACKEY"));
            hMacKey = new SecretKeySpec(macKeyBytes, prop.getProperty("MAC"));
            System.out.println("hmacKey: "+ hMacKey);
            System.out.println("hMac: "+ hMac);

        }
            

        /*
            Mac hMac = Mac.getInstance("HMacSHA256");
            Key hMacKey =new SecretKeySpec(key.getEncoded(), "HMacSHA256");
        
         */


        send( Utils.toByteArray("123Testing123"));  






    }


/*
         DSTP Packet format
    DSTP Header:
    Version(16bit) | Release(8bit) | Payload Len(16bit) 

    DSTP Payload:
    Encrypted( Sequence Number(16bit) | DATA(variable) | H(variable) )
    Encrypted( Sequence Number(16bit) | DATA(variable) | HMAC(variable) )
 */

    private byte[] receave() throws IOException {
        
        return new byte[10];
    }







//    private void send(DatagramSocket socket, byte[] inByteArray, InetAddress address, int port) throws IOException {

static private void send(byte[] inByteArray)  throws Exception {
        String serverHost = "localhost";
        int port = 2141;

        //Version(16bit) | Release(8bit) | Payload Len(16bit)  
        int sequenceNumber = 4; 

        byte[] sendHash = new byte[hash.getDigestLength()];

        hash.update(inByteArray);
        sendHash = hash.digest();

        System.out.println("Message : " + Utils.toString(inByteArray));
        System.out.println("Hash : " + Utils.toString(sendHash));
        System.out.println("HashSize : " + hash.getDigestLength());
        System.out.println("HashLen : " + sendHash.length);
        

        byte[] DSTPPayload = new byte[2 + inByteArray.length  + sendHash.length]; //+2 is for the sequence number

        
        DSTPPayload[0] = (byte) ((sequenceNumber >> 8) & 0xFF);
        DSTPPayload[1] = (byte) (sequenceNumber & 0xFF); 
        System.out.println("SequenceNumber : " + sequenceNumber);

        System.arraycopy(inByteArray, 0, DSTPPayload, 2, inByteArray.length);
        System.arraycopy(sendHash, 0, DSTPPayload, 2+inByteArray.length, sendHash.length);

        //ENCRYPTS DSTPPayload
        Cipher cipher = Cipher.getInstance(ciphersuite);

        if(ivSpec != null){
            cipher.init(Cipher.ENCRYPT_MODE, key,ivSpec);
        }else{
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }

        byte[] encryptDSTPayload = cipher.doFinal(DSTPPayload);



        int version = 0x009; 
        int release = 0x05; 
        int payloadLen = encryptDSTPayload.length;

        byte[] DSTPHeader = new byte[5];
        DSTPHeader[0] = (byte) ((version >> 8) & 0xFF); 
        DSTPHeader[1] = (byte) (version & 0xFF);        
        // Insert the Release (8 bits)
        DSTPHeader[2] = (byte) (release & 0xFF);   
        // Insert the Payload Length (16 bits)
        DSTPHeader[3] = (byte) ((payloadLen >> 8) & 0xFF); 
        DSTPHeader[4] = (byte) (payloadLen & 0xFF);
        

        byte[] fullPayLoad = new byte[encryptDSTPayload.length + DSTPHeader.length];
        System.arraycopy(DSTPHeader, 0, fullPayLoad, 0, DSTPHeader.length);
        System.arraycopy(encryptDSTPayload, 0, fullPayLoad, DSTPHeader.length, encryptDSTPayload.length);

        /* 
        
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
        */

        System.out.println("----------------------------RECIEVING----------------------------");

        byte[] incoming = new byte[5]; 
        //RECIEVING

        //HEADER
        int extractedVersion = ((fullPayLoad[0] & 0xFF) << 8) | (fullPayLoad[1] & 0xFF); // Combine to get version
        int extractedRelease = fullPayLoad[2] & 0xFF; // 8-bit release value
        int extractedPayloadLen = ((fullPayLoad[3] & 0xFF) << 8) | (fullPayLoad[4] & 0xFF); // Combine to get payload length
        System.out.println("Extracted Version: " + extractedVersion);
        System.out.println("Extracted Release: " + extractedRelease);
        System.out.println("Extracted Payload Length: " + extractedPayloadLen);


        if(ivSpec != null){
            cipher.init(Cipher.DECRYPT_MODE, key,ivSpec);
        }else{
            cipher.init(Cipher.DECRYPT_MODE, key);
        }

        byte[] extractedDSTPPayload =new byte[cipher.getOutputSize(extractedPayloadLen)];;
        int ptLength=cipher.update(fullPayLoad,5, extractedPayloadLen, extractedDSTPPayload,0);
        ptLength += cipher.doFinal(extractedDSTPPayload, ptLength);
        //PAYLOAD

        int hashSize = hash.getDigestLength();

        int extractedSequenceNumber = ((extractedDSTPPayload[0] & 0xFF) << 8) | (extractedDSTPPayload[1] & 0xFF); // Sequence number
        System.out.println("Extracted Sequence Number: " + extractedSequenceNumber);

        byte[] extractedMessageBytes = new byte[extractedPayloadLen -2- hashSize]; // Adjust as needed
        System.arraycopy(extractedDSTPPayload, 2, extractedMessageBytes, 0, extractedMessageBytes.length); // Adjust offset if needed
        System.out.println("Extracted Message: " + Utils.toString(extractedMessageBytes));

        
        byte[] extractedHashIn = new byte[hashSize];
        System.arraycopy(extractedDSTPPayload, 2+extractedMessageBytes.length, extractedHashIn, 0, extractedHashIn.length); // Adjust offset if needed
        System.out.println("Extracted Hash: " + Utils.toString(extractedHashIn));

        hash.update(extractedMessageBytes);
        System.out.println(" verified: " + MessageDigest.isEqual(hash.digest(), extractedHashIn));

    }

  

    



}
