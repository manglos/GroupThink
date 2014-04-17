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
import java.util.Scanner;

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
    static int myID;


    public static void main(String[] args) {
        
        UDPClient.initialize(PORT, hostname);
        
        System.out.println("Enter a requested username.");
        String input="";
        Scanner scan = new Scanner(System.in);
        input = scan.nextLine();
        
        while(!input.equals("exit") && !requestUsername(input))
            input=scan.nextLine();
        
        System.out.println("Goodbye.");
    }
    
    
    //only returns 'true' if gets a valid id from the server (the username is valid and available)
    static boolean requestUsername(String un){
        
        GTPPacket request = new URP(un);
        System.out.println(((URP)request).getUsername());
        GTPPacket response=null;
        byte[] rb=null;
        
        
        try {
            UDPClient.sendPacket(request);
            rb = UDPClient.receivePacket();
            response = new UCP(rb);
            myID=((UCP)response).getUserID();
            System.out.println("Username confirmed ID = " + myID);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (WrongPacketTypeException ex) {
            //System.out.println(ex);
            response = new EP(rb);
            System.out.println("Error Code "+ ((EP)response).getErrorCode()+ " - " + ((EP)response).getMessage());
        }
        return false;
    }
    
    
    //class to handle most basic UDP communications
    static class UDPClient {
        
        static int myPort;
        static String myHost;
        
        //UDPClient MUST be initialized to send/recieve data
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
        
        //just prints contents of byte array, for debug purposes
        static void printBytes(byte[] b){
            System.out.println("\n");
            for(int i=0;i<b.length;i++)
                System.out.print(b[i] + "  ");
        }

        //send a GTPPacket
        static void sendPacket(GTPPacket p) throws IOException, SocketTimeoutException{

            byte[] b = p.getBytes();

            DatagramPacket packet = new DatagramPacket(b, 0, b.length, inet, myPort);
            client.send(packet);

        }

        //recieve a byte array, (of an unspecified type of packet)
        private static byte[] receivePacket() throws IOException, SocketTimeoutException{

            DatagramPacket receiver;

            byte[] recv = new byte[516];

            receiver = new DatagramPacket(recv, recv.length);
            client.receive(receiver);

            return recv;

        }
    }
    
    
}
