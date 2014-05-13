package groupthinkclient;

import GroupThink.GTP.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;

import static javax.swing.JOptionPane.*;

import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class GroupThinkClient extends JFrame {
    
    //General Constants
    private final String FILE_NAME = "file.gt";
    
    // Networking Constants
    private static final int PORT = 2606;
    private static final String HOSTNAME = "224.0.0.0";
    public static final Long ACTIVE_TIMEOUT = 30*1000*1000*1000L; //30 seconds
    
    // Networking Variables
    static DataOutputStream dos = null; // For writing to the socket
    static DataInputStream dis = null;  // For reading from the socket
    //static DataInputStream is;
    static PrintWriter out;             // For persisting the file [commit]
    static BufferedReader in = null;
    static AtomicInteger myID;
    static EP currentError;
    static final Queue packetQueue = new Queue(); // queue of packets received
    //static DataList myDataList;
    static ConcurrentHashMap<Integer, User> idToUser; // keep track of the current group
    private int leaderID; // keep track of the current leader
    public AtomicInteger nextUserID;

    // Gui Constants
    private final boolean SPLIT_PANE_DYN_UPDATE_ON_RESIZE = true;
    private final int GUI_WIDTH = 500, GUI_HEIGHT = 300;
    
    // GUI Variables
    public static AtomicReference<String> username;
    private static JPanel chatRoomPanel, chatLog;
    private JPanel chatRoom, inputPanel,topPanel, topContainerPanel, topButtonPanel;
    private JButton sendButton, commitButton;
    private JToggleButton observerToggle;
    private static JTextField messageField;
    private static JScrollPane chatLogScroller, nameScroller;
    private JSplitPane splitPane;
    private RTextScrollPane rtsp;
    static RSyntaxTextArea editor;
    public static CheckBoxList chatNameList;
    private Border defaultPanelBorder;
    private static SimpleDateFormat sdf;
    private static ArrayList<JLabel> messages;
    
    //General Variables
    public static AtomicBoolean observerMode = new AtomicBoolean(false);
    
    // Change / Synchronization Attributes:
    public static ChangeLogger logger; // adds changes to logs
    public static ConcurrentHashMap<Long, GlobalChange> gChanges; // list of global changes
    public static final ConcurrentLinkedQueue<LocalChange> lChanges = new ConcurrentLinkedQueue(); // list of local changes
    public static AtomicBoolean leader; // do you have the token?
    public static AtomicLong highestSequentialChange = new AtomicLong(0); // global change counter
    public static int currentLeader;
    public static TCP token;

    public static void main(String[] args) {
        // Multicaster to send packets:
        UDPMultiCaster.initialize(PORT, HOSTNAME);
        GroupThinkClient.UDPMultiCaster.initialize(PORT, HOSTNAME);
        
        // Start GUI:
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Set the Look and Feel:
                try {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (ClassNotFoundException | 
                         IllegalAccessException | 
                         InstantiationException | 
                         UnsupportedLookAndFeelException e
                        ) {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    e.printStackTrace();
                }

                // Create the GUI:
                GroupThinkClient client = new GroupThinkClient();
                
                // Display the GUI:
                client.setVisible(true);
            }
        });
    }

    /*
     * The GroupThinkClient constructor should only be called on the EDT...
     */
    public GroupThinkClient(){
         this.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {
                boolean isLeader = false;
                LOP lop = new LOP((short)myID.get(), isLeader);
                try {
                    UDPMultiCaster.sendPacket(lop);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Window closing...");
            }
            @Override public void windowClosed(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
        
        // Load Networking Tools:
        idToUser = new ConcurrentHashMap<Integer, User>();
        leader = new AtomicBoolean(false);
        myID = new AtomicInteger(-1);
        nextUserID = new AtomicInteger(0);
        currentLeader=-1;
        username = new AtomicReference<String>(null);
        messages = new ArrayList<JLabel>();
        token = null;
        String un="";
        gChanges = new ConcurrentHashMap<Long, GlobalChange>();
        
        // Load GUI Tools:
        sdf = new SimpleDateFormat("HH:mm:ss");
        defaultPanelBorder = BorderFactory.createLineBorder(Color.black);
        chatRoom = new JPanel(new BorderLayout());
        
        Thread pt = new Thread(new PacketWorker());
        pt.start();

        Thread lt = new Thread(new ListenerWorker(PORT, HOSTNAME));
        lt.start();
        chatNameList = new CheckBoxList(idToUser.values().toArray());
        
        Thread ht = new Thread(new HeartbeatWorker(ACTIVE_TIMEOUT));
        ht.start();
        
        Thread lct = new Thread(new LocalChangeWorker());
        lct.start();
        
        un=showInputDialog(this, "Please enter your requested username:",
                "Login to the GroupThink Server", JOptionPane.QUESTION_MESSAGE);
        username.compareAndSet(null, un);
        
        while(!requestUsername(un)){
            if(currentError!=null)
                showMessageDialog(this, currentError.getMessage());
            else
                showMessageDialog(this, "Unidentified error requesting " + username);

            un = showInputDialog(this, "Please try another username:");
        }
        
        chatNameList.setUsername(username.get());
        System.out.println("I have a username!!! " + username.get());
        
        idToUser.put(myID.get(), new User(myID.get(), username.get()));
        
        nameScroller = new JScrollPane(chatNameList);
        chatLog = new JPanel(new GridLayout(0,1,2,2));
        chatLog.setBackground(Color.WHITE);
        chatLogScroller = new JScrollPane(chatLog);
        sendButton = new JButton();
        sendButton.addActionListener(getSendButtonActionListener());
        try{
            Image img = ImageIO.read(getClass().getResource("send.jpg"));
            sendButton.setIcon(new ImageIcon(img));
        } catch (IOException ioe){
            ioe.printStackTrace();
            sendButton.setText(">");
        }
        messageField = new JTextField();
        messageField.setText("Enter messages to send here...");
        messageField.addMouseListener(
            new MouseListener(){
                @Override public void mouseClicked(MouseEvent e) {
                    messageField.setText("");
                }
                @Override public void mousePressed(MouseEvent e) {}
                @Override public void mouseReleased(MouseEvent e) {}
                @Override public void mouseEntered(MouseEvent e) {}
                @Override public void mouseExited(MouseEvent e) {}
            }
        );
        messageField.addKeyListener(
                new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if(e.getKeyChar() == KeyEvent.VK_ENTER){
                            sendButton.doClick();
                            messageField.setText("");
                        }
                    }
                    @Override public void keyPressed(KeyEvent e) {}
                    @Override public void keyReleased(KeyEvent e) {}
                }
        );
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(messageField, BorderLayout.CENTER);

        chatRoom.add(chatLogScroller, BorderLayout.CENTER);
        chatRoom.add(inputPanel, BorderLayout.SOUTH);

        chatRoomPanel = new JPanel(new BorderLayout());
        chatRoomPanel.add(nameScroller, BorderLayout.WEST);
        chatRoomPanel.add(chatRoom, BorderLayout.CENTER);

        topPanel = new JPanel(new BorderLayout());
        topButtonPanel = new JPanel();

        commitButton = new JButton("Commit");
        commitButton.addActionListener(getCommitButtonActionListener());
        observerToggle = new JToggleButton("Observer Mode");
        observerToggle.addActionListener(getObserverToggleActionListener());
        
        //TODO populate file details from file.gt
        try {
            System.out.println(System.getProperty("user.home") + System.getProperty("file.separator") +  FILE_NAME);
            File gtFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") +  FILE_NAME);
            if(!gtFile.exists()){
                gtFile.createNewFile();
            }
            BasicFileAttributes attr = Files.readAttributes(gtFile.toPath(), BasicFileAttributes.class);

            GTFile gtf = new GTFile(FILE_NAME, attr.size(), attr.lastModifiedTime().toMillis());

            topPanel.add(gtf.getFileIcon(), BorderLayout.WEST);

        } catch (IOException e) {
            e.printStackTrace();
            }

        topButtonPanel.add(observerToggle);
        topButtonPanel.add(commitButton);
        topPanel.add(topButtonPanel, BorderLayout.EAST);
        
        topContainerPanel = new JPanel(new BorderLayout());
        topContainerPanel.add(topPanel, BorderLayout.NORTH);
        
        editor = new RSyntaxTextArea(20, 60);
        editor.setCloseCurlyBraces(false);
        
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        editor.setCodeFoldingEnabled(false);
        editor.setAutoIndentEnabled(false);
        
        // <ANG> register the change-logging document filter:
        this.logger = new ChangeLogger(this);
        ((RSyntaxDocument) editor.getDocument()).setDocumentFilter(logger);
        // <\ANG>
        
        rtsp = new RTextScrollPane(editor);
        topContainerPanel.add(rtsp, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, SPLIT_PANE_DYN_UPDATE_ON_RESIZE);
//        outerPanel = new JPanel(new BorderLayout());

        splitPane.setDividerLocation(0.5d); //TODO - is this doing anything??
        splitPane.setTopComponent(topContainerPanel);
        splitPane.setBottomComponent(chatRoomPanel);

        setContentPane(splitPane);

        setTitle("(" + username.get() + ") GroupThink Client");
        setSize(GUI_WIDTH, GUI_HEIGHT);
        setLocationRelativeTo(null); //<-- centers the gui on screen
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        
    }
    
    public static void updateHighestSequentialChange(){
        ArrayList<Long> keys = new ArrayList<Long>(gChanges.keySet());
        
        Collections.sort(keys);
        
        for(long i=0;i<keys.size();i++){
            System.out.println("CurrentKey is " + keys.get((int)i));
            if(i==0 && keys.get(0)==null){
                System.out.println("Setting to 0");
                highestSequentialChange.compareAndSet(highestSequentialChange.get(), 0);
                return;
            }
            else if(i>0){
                if(keys.get((int)i)!=(keys.get((int)i-1) + 1)){
                    System.out.println("Setting hgc to " + keys.get((int)i-1));
                    highestSequentialChange.compareAndSet(highestSequentialChange.get(), keys.get((int)i-1));
                    return;
                }
                else
                   highestSequentialChange.incrementAndGet();
            }
        }
               
    }

    private ActionListener getObserverToggleActionListener(){
        return new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractButton abstractButton = (AbstractButton) e.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                observerMode.compareAndSet(observerMode.get(), !observerMode.get());

                if(selected){
                    observerToggle.setForeground(Color.GREEN.darker());
                    editor.setEnabled(false);
                } else{
                    observerToggle.setForeground(Color.BLACK);
                    editor.setEnabled(true);
                }

                System.out.println("Observer Mode is: " + (observerMode.get()? "ON!" : "OFF!"));
            }
        };
    }

    //TODO - need to add functionality to the commit button
    private ActionListener getCommitButtonActionListener(){
        return new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
    }

    private ActionListener getSendButtonActionListener(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer> sendTo = new ArrayList<Integer>();
                if(!chatNameList.allChecked()) {
                    for (String name : chatNameList.getCheckedItemNames()) {
                        int id = -13;
                        for (Map.Entry<Integer, User> entry : idToUser.entrySet()) {
                            if (entry.getValue().getUsername().equalsIgnoreCase(name)) {
                                id = entry.getKey();
                                break;
                            }
                        }
                        if (id>0) {
                            sendTo.add(id);
                        }
                    }
                } else{
                    sendTo.add(-1);
                }

                if(!sendTo.isEmpty()){
                    int[] ids = new int[sendTo.size()];
                    for(int i=0;i<sendTo.size();i++){
                        ids[i] = sendTo.get(i);
                    }
                    sendChatMessage(messageField.getText(), ids);
                } else{
//                    showMessageDialog(splitPane, "Please select one or more recipients in order to send a message.");
                    sendChatMessage(messageField.getText(), new int[]{-1});
                }

            }
        };
    }

    private void sendChatMessage(String message, int[] recipients){
        CMP cmp = null;
        
        for(int i : recipients){
            System.out.println("to " + i);
            cmp = new CMP(i, (short)myID.get(), message);
            try {
                UDPMultiCaster.sendPacket(cmp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(cmp != null && i != -1){ //if not sending to everyone, be sure to display on user's screen
                displayChatMessage(cmp, true);
            }
        }
    }
    
    public static void displayChatMessage(CMP p, boolean pm){
        String timeStamp = sdf.format(new Date());
        String message = "";
        
        //System.out.println(idToUser.get(p.getUserID()));
        
        String name = p.getUserID() == myID.get()? username.get() : idToUser.get(p.getUserID()).getUsername();
        //System.out.println("name is " + name);
        Color c = chatNameList.getUserColor(name);
        JLabel chatMessage = new JLabel();
        if(!pm){ //if this is NOT a private message...
            message = String.format("\n%s - %s: %s", timeStamp, (name != null? name : "Anon"), p.getMessage());
        } else{
            chatMessage.setFont(chatMessage.getFont().deriveFont(Font.ITALIC));
            message = String.format("\n%s - %s -> %s: %s", timeStamp, (name != null? name : "Anon"), idToUser.get(p.getIntendedRecipient()).getUsername(), p.getMessage());
        }

        chatMessage.setText(message);
        chatMessage.setForeground(c);
        messages.add(chatMessage);
        if(messages.size()>100){
            chatLog.remove(messages.get(0));
            messages.remove(0);
        }

        chatLog.add(chatMessage);
        chatLogScroller.updateUI();
        chatLogScroller.getViewport().setViewPosition(new Point(0,chatLog.getHeight()));

    }

    public static void addUser(String name, int id){
        if(id!=myID.get() && !chatNameList.containsName(name)){
            idToUser.put(id, new User(id, name));
            System.out.println("name is " + name);
            chatNameList.addName(name);
            
            if (chatLog!=null) {
                JLabel logInMessage = new JLabel();
                logInMessage.setFont(logInMessage.getFont().deriveFont(Font.ITALIC));
                logInMessage.setForeground(Color.GREEN.darker());
                String timeStamp = sdf.format(new Date());
                String message = String.format("%s - %s %s", timeStamp, idToUser.get(id).getUsername(), "has logged in");
                logInMessage.setText(message);
                chatLog.add(logInMessage);
                chatLogScroller.updateUI();
                chatLogScroller.getViewport().setViewPosition(new Point(0, chatLog.getHeight()));
                chatRoomPanel.updateUI();
        }
    }
    }
    
    public static void removeUser(int id){
        System.out.println("Received a request to remove user: " + id + ", " + idToUser.get(id).getUsername());
        if(id != myID.get()){
            if(idToUser.get(id)!=null){
                JLabel logOutMessage = new JLabel();
                logOutMessage.setFont(logOutMessage.getFont().deriveFont(Font.ITALIC));
                logOutMessage.setForeground(Color.RED.darker());
                String timestamp = sdf.format(new Date());
                String message = String.format("%s - %s %s", timestamp, idToUser.get(id).getUsername(), "has logged out");
                logOutMessage.setText(message);
                chatLog.add(logOutMessage);

                chatNameList.removeName(idToUser.get(id).getUsername());
                idToUser.remove(id);

                chatLogScroller.updateUI();
                chatLogScroller.getViewport().setViewPosition(new Point(0, chatLog.getHeight()));
            }
        }
    }
    
    //only returns 'true' if gets a valid id from the server (the username is valid and available)
    static boolean requestUsername(String un){
        username.compareAndSet(username.get(), un);
        
        System.out.println("Requesting for " + username.get());
        //send out request
        try{
            UDPMultiCaster.sendPacket(new URP(un));
        }catch(IOException ex){
            ex.printStackTrace();
        }
        
        try{
            synchronized(myID){
                myID.wait(1000);
            }
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }
         
        
        System.out.println("Done waiting. " + myID.get());
        
        
        if(myID.get()==-1){
            myID.compareAndSet(-1, 0);
            User me = new User(0, username.get());
            me.setIsLeader(true);
            currentLeader=0;
            idToUser.put(0, me);
            leader.getAndSet(true);
            token = new TCP(0, 0);
            System.out.println(token);
            UDPMultiCaster.printBytes(token.getBytes());
        }
        
        return true;
    }
    
    //========================================================================//
    //                            PACKET SNIFFER                              //
    //========================================================================//
    
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
    
    //========================================================================//
    //                             MULTICASTER                                //
    //========================================================================//
    
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
                //receiver.setSoTimeout(1000); // Why?
                inet = InetAddress.getByName(myHost);
                receiver.joinGroup(inet);
                
                sender = new MulticastSocket();
                sender.setReceiveBufferSize(57344);
                sender.setSendBufferSize(57344);
                //sender.setSoTimeout(1000); // Why?
                
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

        // send a GTPPacket
        static void sendPacket(GTPPacket p) throws IOException, SocketTimeoutException{
            byte[] b = p.getBytes();
            DatagramPacket packet = new DatagramPacket(b, 0, b.length, inet, myPort);
            sender.send(packet);
        }

        // recieve a byte array, (of an unspecified type of packet)
        static byte[] receivePacket() throws IOException, SocketTimeoutException{
            DatagramPacket recPack;
            byte[] recv = new byte[516];
            recPack = new DatagramPacket(recv, recv.length);
            receiver.receive(recPack);  // blocks
            return recv;
        }
    }

    
}
