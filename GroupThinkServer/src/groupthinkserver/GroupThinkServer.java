package groupthinkserver;

import GroupThink.GTP.EP;
import GroupThink.GTP.GTPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupThinkServer {
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_WHITE = "\u001B[0m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_BLUE = "\u001B[34m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_PURPLE = "\u001B[35m";
    
    public static final double COMMIT_RATIO = 0.66;
    
    public static boolean voting = false;
    public static boolean commitable = false;
    public static boolean debug = false;
    public static final int PORT = 2606;
    public static ArrayList<String> clients;
    public static boolean existingDocument;
    public static final int BLOCKSIZE = 512;
    public static Queue myQueue;
    private static final String HOSTNAME = "224.0.0.0";
    
    
    public static void main(String[] args){
        UDPMultiCaster.initialize(PORT, HOSTNAME);
        clients = new ArrayList<String>();
        clients.add("server");
        myQueue=new Queue();
        
        existingDocument=false;
        
        try{
            DocumentSerializer.DeSerialize();
            existingDocument=true;
        }catch(Exception ex){
            System.out.println("Could not read document.");
            DocumentSerializer.document = "public class monkey{\n\n\tmoney(int haha){}\n}";
            try {
                DocumentSerializer.Serialize();
            } catch (IOException ex1) {
                Logger.getLogger(GroupThinkServer.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        
        //start the PacketWorker
        Thread pt = new Thread(new PacketWorker());
        pt.start();
        
        //start the ListenerWorker
        Thread lt = new Thread(new ListenerWorker(PORT, HOSTNAME));
        lt.start();
        
    }
    
    public static class DocumentSerializer{
        public static String document;
        
        static void Serialize() throws FileNotFoundException, IOException{
            String filename = System.getProperty("user.home")+System.getProperty("file.separator")+"session.gt";
            File file = new File(filename);
            FileOutputStream fileOut =
            new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(document);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + filename);            
        }
        
        static void DeSerialize() throws FileNotFoundException, IOException, ClassNotFoundException{
            String filename = System.getProperty("user.home")+System.getProperty("file.separator")+"session.gt";
            File file = new File(filename);
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            document = (String)in.readObject();
            in.close();
            fileIn.close();
        }
        
    }
    
     //class for examining received packets, without throwing exceptions
    static class PacketSniffer{
        public static final int OC_WCP=1;
        public static final int OC_DCP=2;
        public static final int OC_RPP=3;
        public static final int OC_CVP=4;
        public static final int OC_URP=5;
        public static final int OC_UCP=6;
        public static final int OC_EP=7;
        
        //returns int of packet type
        static int packetType(byte[] b){
            byte[] op = new byte[2];
            op[0] = b[0];
            op[1] = b[1];

            ByteBuffer bb = ByteBuffer.wrap(op);
            
            return (int)bb.getShort();
        }
        
        //returns int for intended user id, -1 for all, 0 for server
        static int intendedRecipient(byte[] b){
            byte[] ir = new byte[2];
            ir[0] = b[2];
            ir[1] = b[3];

            ByteBuffer bb = ByteBuffer.wrap(ir);
            
            return (int)bb.getShort();
        }
        
        //returns int for error code, -1 if not an error packet
        static int errorCode(byte[] b){
            if(packetType(b)!=OC_EP){
                return -1;
            }
            
            byte[] ec = new byte[2];
            ec[0] = b[4];
            ec[1] = b[5];

            ByteBuffer bb = ByteBuffer.wrap(ec);
            
            return (int)bb.getShort();
        }
        
        static String getErrorUsername(byte[] b){
            if(errorCode(b)==3){
                String[] ms = new EP(b).getMessage().split("'");
                
                return ms[1];
            }
            
            return null;
        }
        
    }
    
    
    //class to handle most basic UDP communications
    public static class UDPMultiCaster {
        
        static int myPort;
        static String myHost;
        static MulticastSocket receiver = null;
        static MulticastSocket sender = null;
        static InetAddress inet;
        
        //UDPClient MUST be initialized to send/recieve data
        static void initialize(int p, String h){
            myPort = p;
            myHost = h;
            try {
                receiver = new MulticastSocket(myPort);
                receiver.setReceiveBufferSize(57344);
                receiver.setSendBufferSize(57344);
                receiver.setSoTimeout(1000);
                inet = InetAddress.getByName(myHost);
                receiver.joinGroup(inet);
                
                sender = new MulticastSocket();
                sender.setReceiveBufferSize(57344);
                sender.setSendBufferSize(57344);
                sender.setSoTimeout(1000);
                
            } catch (SocketException ex) {
                ex.printStackTrace();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        //just prints contents of byte array, for debug purposes
        public static void printBytes(byte[] b){
            System.out.println("\n");
            for(int i=0;i<b.length;i++)
                System.out.print(b[i] + "  ");
        }

        //send a GTPPacket
        static void sendPacket(GTPPacket p) throws IOException, SocketTimeoutException{

            byte[] b = p.getBytes();

            DatagramPacket packet = new DatagramPacket(b, 0, b.length, inet, myPort);
            sender.send(packet);

        }

        //recieve a byte array, (of an unspecified type of packet)
        static byte[] receivePacket() throws IOException, SocketTimeoutException{

            DatagramPacket recPack;

            byte[] recv = new byte[516];

            recPack = new DatagramPacket(recv, recv.length);
            receiver.receive(recPack);

            return recv;

        }
    }
    
}