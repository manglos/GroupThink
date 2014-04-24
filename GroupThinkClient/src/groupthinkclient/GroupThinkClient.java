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
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.UIManager.*;

import static javax.swing.JOptionPane.*;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class GroupThinkClient extends JFrame {
    //Constants
    private static boolean DEBUG = true;
    private final boolean SPLIT_PANE_DYN_UPDATE_ON_RESIZE = true;
    private final int DOC_ROWS = 20;
    private final int DOC_COLS = 80;
    private final int GUI_WIDTH = 500;
    private final int GUI_HEIGHT = 300;
    private final int REPO_PANEL_MAX_WIDTH = 30;
    private static final int PORT = 2606;
    private static final String HOSTNAME = "224.0.0.0";

    //Communication/networking variables
    static DataOutputStream dos = null;
    static DataInputStream dis = null;
    static DataInputStream is;
    static PrintWriter out;
    static BufferedReader in = null;
    static int myID;
    static EP currentError;
    static Queue myQueue;
    static Map<Integer, String> idToUsernameMap;

    //GUI-related variables
    private String username;
    private JPanel chatRoom;
    private JPanel chatRoomPanel;
    private JPanel cp;
    private JPanel inputPanel;
    private JPanel repoPanel;
    private JPanel outerPanel;

    private JButton sendButton;
    private JTextField messageField;
    private JTextArea chatLog;
    private JScrollPane chatLogScroller;
    private JScrollPane nameScroller;
    private JScrollPane repoScroller;
    private JSplitPane innerSplitPane;

    private RTextScrollPane rtsp;
    private RSyntaxTextArea editor;

    private CheckBoxList chatNameList;
    private Border defaultPanelBorder;

    private SimpleDateFormat sdf;

    public static void main(String[] args) {
        UDPMultiCaster.initialize(PORT, HOSTNAME);
        GroupThinkClient.UDPMultiCaster.initialize(PORT, HOSTNAME);
        currentError = null;
        myQueue = new Queue();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                //Set the Look and Feel...
                try {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    e.printStackTrace();
                }

                GroupThinkClient client = new GroupThinkClient();
                client.setVisible(true);
            }
        });

    }

    /*
     * The GroupThinkClient constructor should only be called on the EDT...
     */
    public GroupThinkClient(){
        idToUsernameMap = new HashMap<Integer, String>();
        sdf = new SimpleDateFormat("HH:mm:ss");

        if(DEBUG){
            //populate the name list with dummy data
            for(int i=0;i<10;i++){
                idToUsernameMap.put(i, "Name_"+i);
            }
        }

        defaultPanelBorder = BorderFactory.createLineBorder(Color.black);

        cp = new JPanel(new BorderLayout());

        chatRoom = new JPanel(new BorderLayout());
//        chatRoom.setBorder(defaultPanelBorder);

        chatNameList = new CheckBoxList(idToUsernameMap.values().toArray());
        nameScroller = new JScrollPane(chatNameList);

        chatLog = new JTextArea();
        chatLog.setEditable(false);
        chatLogScroller = new JScrollPane(chatLog);

        sendButton = new JButton();
        try{
            Image img = ImageIO.read(getClass().getResource("send.jpg"));
            sendButton.setIcon(new ImageIcon(img));
        } catch (IOException ioe){
            ioe.printStackTrace();
            sendButton.setText(">");
        }
        messageField = new JTextField();
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(messageField, BorderLayout.CENTER);

        chatRoom.add(chatLogScroller, BorderLayout.CENTER);
        chatRoom.add(inputPanel, BorderLayout.SOUTH);

        chatRoomPanel = new JPanel(new BorderLayout());
        chatRoomPanel.add(nameScroller, BorderLayout.WEST);
        chatRoomPanel.add(chatRoom, BorderLayout.CENTER);

        GridLayout repoLayout = new GridLayout(0, 1, 5, 0);

        repoPanel = new JPanel(repoLayout); //TODO - populate this with icons representing files in the repo
        repoPanel.setMaximumSize(new Dimension(REPO_PANEL_MAX_WIDTH, this.getHeight()));
        repoScroller = new JScrollPane(repoPanel);

        //try to generate some dummy repo files
        if(DEBUG){
            Random rand = new Random();
            ArrayList<RepoDocument> dummyFiles = new ArrayList<RepoDocument>();
            for(int i=0;i<5;i++){
                dummyFiles.add(new RepoDocument("File#"+(i+1), 2345l, System.currentTimeMillis()));
            }
            for(RepoDocument d : dummyFiles){
                repoPanel.add(d.getFileIcon());
            }
        }

        editor = new RSyntaxTextArea(20, 60);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        editor.setCodeFoldingEnabled(true);

        rtsp = new RTextScrollPane(editor);

        innerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, SPLIT_PANE_DYN_UPDATE_ON_RESIZE);
        outerPanel = new JPanel(new BorderLayout());
//        outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, SPLIT_PANE_DYN_UPDATE_ON_RESIZE);

        innerSplitPane.setDividerLocation(0.5d); //TODO - is this doing anything??
        innerSplitPane.setTopComponent(rtsp);
        innerSplitPane.setBottomComponent(chatRoomPanel);

        outerPanel.add(innerSplitPane, BorderLayout.CENTER);
        outerPanel.add(repoScroller, BorderLayout.EAST);
//        outerSplitPane.setLeftComponent(innerSplitPane);
//        outerSplitPane.setRightComponent(repoPanel);





//        cp.add(rtsp, BorderLayout.NORTH);


        setContentPane(outerPanel);

        setTitle("GroupThink Client");
        setSize(GUI_WIDTH, GUI_HEIGHT);
        setLocationRelativeTo(null); //<-- centers the gui on screen
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        username = showInputDialog(cp, "Please enter your requested username:");
//        while(!requestUsername(username)){
//            if(currentError!=null)
//                showMessageDialog(cp, currentError.getMessage());
//            else
//                showMessageDialog(cp, "Unidentified error requesting " + username);
//
//            username = showInputDialog(cp, "Please try another username:");
//        }
        
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
                //else if packet is for everyone, and is a valid EP with code 3 - <username> is unavailable
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
