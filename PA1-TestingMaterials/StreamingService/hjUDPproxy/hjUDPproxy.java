/* hjUDPproxy, for use in 2024
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

class hjUDPproxy {
    public static void main(String[] args) throws Exception {
	//        InputStream inputStream = new FileInputStream("config.properties");
	//        if (inputStream == null) {
	//            System.err.println("Configuration file not found!");
	//            System.exit(1);
	//}
	
        //Properties properties = new Properties();
        //properties.load(inputStream);
	//String remote = properties.getProperty("remote");
        //String destinations = properties.getProperty("localdelivery");

	String remote=args[0];
	String destinations=args[1];	
	    

        SocketAddress inSocketAddress = parseSocketAddress(remote);
        Set<SocketAddress> outSocketAddressSet = Arrays.stream(destinations.split(",")).map(s -> parseSocketAddress(s)).collect(Collectors.toSet());

        // Manage this according to your required setup, namely
	// if you want to use unicast or multicast channels

        // If listen a remote unicast server try the remote config
        // uncomment the following line
	
	 DatagramSocket inSocket = new DatagramSocket(inSocketAddress); 

	// If listen a remote multicast server using IP Multicasting
        // addressing (remember IP Multicast Range) and port 
	// uncomment the following two lines

	//	MulticastSocket ms = new MulticastSocket(9999);
	//        ms.joinGroup(InetAddress.getByName("239.9.9.9"));

        DatagramSocket outSocket = new DatagramSocket();
        byte[] buffer = new byte[4 * 1024];
        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
	    // If listen a remote unicast server
	    // uncomment the following line

	    inSocket.receive(inPacket);  // if remote is unicast

	    // If listen a remote multcast server
	    // uncomment the following line

            //ms.receive(inPacket);          // if remote is multicast

            System.out.print(":");           // debug
            for (SocketAddress outSocketAddress : outSocketAddressSet) 
		{
                outSocket.send(new DatagramPacket(buffer, inPacket.getLength(), outSocketAddress));
            }
        }
    }

    private static InetSocketAddress parseSocketAddress(String socketAddress) 
    {
        String[] split = socketAddress.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host, port);
    }
}
