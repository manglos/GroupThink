package groupthinkserver;


import GroupThink.GTP.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

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
                GTPPacket request;
                GTPPacket response;
                try {
                    request = new URP(rb);
                    String requestedUsername = ((URP)request).getUsername();
                    System.out.println("Got Request for username : " + requestedUsername);
                    if(GroupThinkServer.clients.contains(requestedUsername)){
                       response = new EP(-1, 3, requestedUsername + " is unavailable.");
                    }
                    else{
                        GroupThinkServer.clients.add(requestedUsername);
                        response = new UCP((short)-1, (short)GroupThinkServer.clients.indexOf(requestedUsername), requestedUsername);
                        //System.out.println(response);
                    }
                    sendPacket(response);
                } catch (WrongPacketTypeException ex) {
                    System.out.println(ex);
                }
                
                

                receiver.close();
                
                

            }catch (IOException ex) {
                ex.printStackTrace();
                receiver.close();
            }
        }
    }
    
    private int getPacketType(byte[] b){
        int result;
        byte[] op = new byte[2];
        op[0] = b[0];
        op[1] = b[1];
        
        ByteBuffer bb = ByteBuffer.wrap(op);
        result = (int)bb.getShort();
        return result;
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
        System.out.println("Sending packet... " + p);
        printBytes(p.getBytes());
        packet = new DatagramPacket(p.getBytes(), 0, p.getBytes().length, inet, PORT);
        sender.send(packet);
        System.out.println("...sent.");
    }
    
    //just prints contents of byte array, for debug purposes
    void printBytes(byte[] b){
        System.out.println("\n");
        for(int i=0;i<b.length;i++)
            System.out.print(b[i] + "  ");
    }

}
