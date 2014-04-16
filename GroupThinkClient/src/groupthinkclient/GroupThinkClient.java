package groupthinkclient;

import GroupThink.GTP.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupThinkClient {
    static int PORT = 2606;
    static DataOutputStream dos = null;
    static DataInputStream dis = null;
    static String hostname = "localhost";
    static DataInputStream is;
    static PrintWriter out;
    static Socket clientSocket = null;
    static BufferedReader in = null;
    static ServerSocket echoServer = null;
    static boolean debug = false;
    static DatagramSocket client = null;
    static InetAddress inet;


    public static void main(String[] args) {
        
        UDPClient.initialize(PORT, hostname);
        GTPPacket request = new URP("manglosc");
        GTPPacket response=null;
        byte[] rb=null;
        
        try {
            UDPClient.sendPacket(request);
            rb = UDPClient.receivePacket();
            response = new UCP(rb);
            System.out.println("Username confirmed ID = " + ((UCP)response).getUserID());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (WrongPacketTypeException ex) {
            System.out.println(ex);
            response = new EP(rb);
        }
        
        
    }
    
    
    static class UDPClient {
        
        static int myPort;
        static String myHost;
        
        static void initialize(int p, String h){
            myPort = p;
            myHost = h;
            try {
                client = new DatagramSocket();
                client.setReceiveBufferSize(57344);
                client.setSendBufferSize(57344);
                client.setSoTimeout(1000);
                inet = InetAddress.getByName(myHost);
            } catch (SocketException ex) {
                ex.printStackTrace();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        }
        
        static void printBytes(byte[] b){
            System.out.println("\n");
            for(int i=0;i<b.length;i++)
                System.out.print(b[i] + "  ");
        }

        static void sendPacket(GTPPacket p) throws IOException, SocketTimeoutException{

            byte[] b = p.getBytes();

            DatagramPacket packet = new DatagramPacket(b, 0, b.length, inet, myPort);
            client.send(packet);

        }

        private static byte[] receivePacket() throws IOException, SocketTimeoutException{

            DatagramPacket receiver;

            byte[] recv = new byte[516];

            receiver = new DatagramPacket(recv, recv.length);
            client.receive(receiver);

            return recv;

        }
    }
    
    
}
