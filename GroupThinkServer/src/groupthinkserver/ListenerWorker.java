package groupthinkserver;

import GroupThink.GTP.*;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerWorker implements Runnable{

    int myPort;
    String myHost;
    
    public ListenerWorker(int p, String h){
        myPort=p;
        myHost=h;
    }
    
    @Override
    public void run() {
        //GroupThinkServer.UDPMultiCaster.initialize(myPort, myHost);
        System.out.println("Listening for packets...");
        for(;;){
            
            try{
                byte[] b = GroupThinkServer.UDPMultiCaster.receivePacket();
                
                int type = GroupThinkServer.PacketSniffer.packetType(b);
                int intendedUser = GroupThinkServer.PacketSniffer.intendedRecipient(b);
                
                System.out.println("Received " + type);
                
                if(intendedUser==-1 || intendedUser==0){
                    try{
                    switch(type){
                        case 1:
                            GroupThinkServer.myQueue.add(new WCP(b));
                            break;
                        case 2:
                            GroupThinkServer.myQueue.add(new DCP(b));
                            break;
                        case 3:
                            GroupThinkServer.myQueue.add(new RPP(b));
                            break;
                        case 4:
                            GroupThinkServer.myQueue.add(new CVP(b));
                            break;
                        case 5:
                            GroupThinkServer.myQueue.add(new URP(b));
                            break;
                        case 6:
                            GroupThinkServer.myQueue.add(new UCP(b));
                            break;
                        case 7:
                            GroupThinkServer.myQueue.add(new EP(b));
                            break;
                        case 8:
                            GroupThinkServer.myQueue.add(new CMP(b));
                            break;
                        case 9:
                            GroupThinkServer.myQueue.add(new Data(b));
                            break;
                        case 10:
                            GroupThinkServer.myQueue.add(new ACK(b));
                            break;
                    }
                    }catch(WrongPacketTypeException ex){
                        ex.printStackTrace();
                    }
                }
            }catch(SocketTimeoutException ex){
                //ex.printStackTrace();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    
    }
    
}
