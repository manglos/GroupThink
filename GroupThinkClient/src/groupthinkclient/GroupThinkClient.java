package groupthinkclient;

import GroupThink.GTP.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javax.swing.*;

import static javax.swing.JOptionPane.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

@SuppressWarnings("MagicConstant")
public class GroupThinkClient extends JFrame {
    //Constants
    private final int GUI_WIDTH = 500;
    private final int GUI_HEIGHT = 300;
    private static final int PORT = 2606;
    private static final String HOSTNAME = "224.0.0.0";

    //Communication/networking variables
    static DataOutputStream dos = null;
    static DataInputStream dis = null;
    static DataInputStream is;
    static PrintWriter out;
    static BufferedReader in = null;
    static boolean debug = false;
    static int myID;
    static EP currentError;
    static Queue myQueue;

    //GUI-related variables
    String username;


    public static void main(String[] args) {
        GroupThinkClient.UDPMultiCaster.initialize(PORT, HOSTNAME);
        currentError = null;
        myQueue = new Queue();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GroupThinkClient client = new GroupThinkClient();
                client.setVisible(true);
            }
        });

    }

    public GroupThinkClient(){
        RTextScrollPane sp;
        JPanel cp = new JPanel(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);

        sp = new RTextScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);

        setTitle("GroupThink Client");
        setSize(GUI_WIDTH, GUI_HEIGHT);
        setLocationRelativeTo(null); //<-- centers the gui on screen
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        
        username = showInputDialog(cp, "Please enter your requested username:");
        while(!requestUsername(username)){
            if(currentError!=null)
                showMessageDialog(cp, currentError.getMessage());
            else
                showMessageDialog(cp, "Unidentified error requesting " + username);
            
            username = showInputDialog(cp, "Please try another username:");
        }
        
        setTitle("(" + username + ") GroupThink Client");
        
        Thread pt = new Thread(new PacketWorker());
        pt.start();
        
        Thread lt = new Thread(new ListenerWorker(PORT, HOSTNAME));
        lt.start();
        
    }
    
    
    //only returns 'true' if gets a valid id from the server (the username is valid and available)
    static boolean requestUsername(String un){
        
        GTPPacket request = new URP(un);
        System.out.println(((URP)request).getUsername());
        GTPPacket response=null;
        byte[] rb=null;
        
        
        try {
            UDPMultiCaster.sendPacket(request);
            
            boolean retry=true;
            
            //retries until reciept of UCP or EP pertaining to the request
            while(retry){
                rb = UDPMultiCaster.receivePacket();
                
                int code = PacketSniffer.packetType(rb);
                int recipient = PacketSniffer.intendedRecipient(rb);
               
                //if packet is for everyone, and is a valid UCP
                if(recipient==-1 && code==PacketSniffer.OC_UCP){
                    response = new UCP(rb);
                    if(((UCP)response).getUsername().equals(un))
                        retry=false;
                }
                //else if packet is for everone, and is a valid EP with code 3 - <username> is unavailable
                else if(recipient==-1 && code==PacketSniffer.OC_EP && PacketSniffer.errorCode(rb)==3){
                    
                    if(PacketSniffer.getErrorUsername(rb)!=null && PacketSniffer.getErrorUsername(rb).equals(un)){
                        response = new EP(rb);
                        currentError=(EP)response;
                        System.out.println("Error Code "+ response);
                        return false;
                    }
                }
                
                System.out.println("retrying");
            }
            myID=((UCP)response).getUserID();
            System.out.println("Username confirmed ID = " + myID);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (WrongPacketTypeException ex) {
            //System.out.println(ex);
            response = new EP(rb);
            UDPMultiCaster.printBytes(response.getBytes());
            currentError=(EP)response;
            System.out.println("Error Code "+ response);
        }
        return false;
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
    static class UDPMultiCaster {
        
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
        static void printBytes(byte[] b){
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
