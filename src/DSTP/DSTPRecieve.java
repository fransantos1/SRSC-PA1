package DSTP;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DSTPRecieve {
    public static void main(String[] args) {
        int port = 2141; // Port to listen   on
        int hashLen = 64;
        try (DatagramSocket socket = new DatagramSocket(port)) {


            byte[] receiveBuffer = new byte[5]; // Buffer to receive data

            System.out.println("UDP Server is running...");

            while (true) {
                DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(packet); // Receive packet

                int extractedVersion = ((receiveBuffer[0] & 0xFF) << 8) | (receiveBuffer[1] & 0xFF); // Combine to get version
                int extractedRelease = receiveBuffer[2] & 0xFF; // 8-bit release value
                int extractedPayloadLen = ((receiveBuffer[3] & 0xFF) << 8) | (receiveBuffer[4] & 0xFF); // Combine to get payload length
                System.out.println("Extracted Version: " + extractedVersion);
                System.out.println("Extracted Release: " + extractedRelease);
                System.out.println("Extracted Payload Length: " + extractedPayloadLen);


                byte[] payload = new byte[extractedPayloadLen];
                packet = new DatagramPacket(payload, payload.length);
                socket.receive(packet); // Receive packet
        
                
                int extractedSequenceNumber = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF); // Sequence number
                System.out.println("Extracted Sequence Number: " + extractedSequenceNumber);
                System.out.println(extractedPayloadLen-2-hashLen);
                byte[] messageBytes = new byte[extractedPayloadLen-2-hashLen]; // Adjust as needed
                System.arraycopy(payload, 2, messageBytes, 0, messageBytes.length); // Adjust offset if needed
                System.out.println("Extracted Message: " + Utils.toString(messageBytes));


                byte[] hashIn = new byte[hashLen];
                System.arraycopy(payload, 2+messageBytes.length, hashIn, 0, hashIn.length); // Adjust offset if needed
                System.out.println("Extracted Hash: " + Utils.toString(hashIn));

               

                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
