package groupthinkserver;


import GroupThink.GTP.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPWorker implements Runnable {

    private MulticastSocket receiver;
    private MulticastSocket sender;
    private DatagramPacket packet;
    int myClientNumber;
    int PORT;
    InetAddress inet;
    int prt;
    final String HOST = "224.0.0.0";

    PrintWriter os = null;
    BufferedReader b = null;

    DataOutputStream dos = null;
    DataInputStream dis = null;

    public UDPWorker(int p, int n) {
        PORT = p;
        myClientNumber = n;
    }

    @Override
    public void run() {

        while (true) {
            try {
                
                inet = InetAddress.getByName(HOST);
                
                receiver = new MulticastSocket(PORT);
                receiver.setReceiveBufferSize(57344);
                receiver.setSendBufferSize(57344);
                receiver.setSoTimeout(1000);
                receiver.joinGroup(inet);
                
                sender = new MulticastSocket();
                sender.setReceiveBufferSize(57344);
                sender.setSendBufferSize(57344);
                sender.setSoTimeout(1000);
                //System.out.println("RBS: " + socket.getReceiveBufferSize() + "  SBS: " + socket.getSendBufferSize() + "  Timeout: " + socket.getSoTimeout());

                
                byte[] rb=null;
                boolean retryConnect=true;
                
                while(retryConnect){
                    try{
                        rb = receivePacket();
                        retryConnect=false;
                    }catch(SocketTimeoutException se){
                        retryConnect = true;
                    }
                }
                
                
                int type = PacketSniffer.packetType(rb);
                
                switch(type){
                    case 5:
                        try{  
                            URP urp = new URP(rb);
                            System.out.println("RECEIVE " + urp);
                            handleURP(urp);
                        }catch(WrongPacketTypeException ex){}
                    
                }
                
                receiver.close();
                
                

            }catch (IOException ex) {
                ex.printStackTrace();
                receiver.close();
            }
        }
    }
    
    private void handleURP(URP urp){
        GTPPacket response;

        try {
            String requestedUsername = (urp.getUsername());
            if(GroupThinkServer.clients.contains(requestedUsername)){
               response = new EP(-1, 3, "'"+requestedUsername + "' is unavailable.");
            }
            else{
                GroupThinkServer.clients.add(requestedUsername);
                response = new UCP((short)-1, (short)GroupThinkServer.clients.indexOf(requestedUsername), requestedUsername);
                //System.out.println(response);
            }
            
            sendPacket(response);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UDPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            sendPacket(new EP(-1, 2, "TESSSST"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private byte[] receivePacket() throws IOException, SocketTimeoutException {
        long tempTime = System.nanoTime();
        byte revData[] = new byte[516];

        packet = new DatagramPacket(revData, revData.length);

        receiver.receive(packet);

        //address = packet.getAddress();
        //prt = packet.getPort();

        return revData;

    }
    
    private void sendPacket(GTPPacket p) throws IOException{
        System.out.println("SEND " + p);
        //printBytes(p.getBytes());
        packet = new DatagramPacket(p.getBytes(), 0, p.getBytes().length, inet, PORT);
        sender.send(packet);
    }
    
    //just prints contents of byte array, for debug purposes
    void printBytes(byte[] b){
        System.out.println("\n");
        for(int i=0;i<b.length;i++)
            System.out.print(b[i] + "  ");
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

}
