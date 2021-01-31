package sample.TransferPackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SendFromServer {
    public static DatagramSocket sendFromServer(FromServer fromServer, int port, InetAddress address, DatagramSocket datagramSocket) {
        try {
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bStream);
            oo.writeObject(fromServer);
            oo.close();
            byte[] buffer = bStream.toByteArray();
            System.out.println(buffer.length);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            System.out.println("   ========= sending packet of length " + packet.getLength()
                    + " ========== ");
            datagramSocket.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datagramSocket;
    }

    public static DatagramSocket sendFromServer(FromServerAndroid fromServer, int port, InetAddress address, DatagramSocket datagramSocket) {
        try {
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bStream);
            oo.writeObject(fromServer);
            oo.close();
            byte[] buffer = bStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            System.out.println("   ========= sending packet of length " + packet.getLength()
            + " ========== ");
            datagramSocket.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datagramSocket;
    }
}
