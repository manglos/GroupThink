package groupthinkserver;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import GroupThink.GTP.*;

public class UDPWorker implements Runnable {

    private DatagramSocket socket;
    private DatagramPacket packet;
    int myClientNumber;
    int PORT;
    InetAddress address;
    int prt;
    
    

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
                    
                socket = new DatagramSocket(PORT);
                socket.setReceiveBufferSize(57344);
                socket.setSendBufferSize(57344);
                socket.setSoTimeout(1000);
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
                       response = new EP(3, requestedUsername + " is unavailable.");
                    }
                    else{
                        GroupThinkServer.clients.add(requestedUsername);
                        response = new UCP((short)GroupThinkServer.clients.indexOf(requestedUsername));
                    }
                    sendPacket(response);
                } catch (WrongPacketTypeException ex) {
                    System.out.println(ex);
                }
                
                

                socket.close();
                
                

            }catch (IOException ex) {
                ex.printStackTrace();
                socket.close();
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

        socket.receive(packet);

        address = packet.getAddress();
        prt = packet.getPort();

        return revData;

    }
    
    private void sendPacket(GTPPacket p) throws IOException{
        packet = new DatagramPacket(p.getBytes(), 0, p.getBytes().length, address, prt);
        socket.send(packet);
    }

}
