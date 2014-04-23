/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 *
 * @author montynewman
 */
public class ListenerWorker implements Runnable{

    int myPort;
    String myHost;
    
    public ListenerWorker(int p, String h){
        myPort=p;
        myHost=h;
    }
    
    @Override
    public void run() {
        //GroupThinkClient.UDPMultiCaster.initialize(myPort, myHost);
        System.out.println("Listening for packets...");
        for(;;){
            
            try{
                byte[] b = GroupThinkClient.UDPMultiCaster.receivePacket();
                
                int type = GroupThinkClient.PacketSniffer.packetType(b);
                int intendedUser = GroupThinkClient.PacketSniffer.intendedRecipient(b);
                
                if(intendedUser==-1 || intendedUser==GroupThinkClient.myID){
                    try{
                    switch(type){
                        case 1:
                            GroupThinkClient.myQueue.add(new WCP(b));
                        case 2:
                            GroupThinkClient.myQueue.add(new DCP(b));
                        case 3:
                            GroupThinkClient.myQueue.add(new RPP(b));
                        case 4:
                            GroupThinkClient.myQueue.add(new CVP(b));
                        case 5:
                            GroupThinkClient.myQueue.add(new URP(b));
                        case 6:
                            GroupThinkClient.myQueue.add(new UCP(b));
                        case 7:
                            GroupThinkClient.myQueue.add(new EP(b));
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
            
        }
    
    }
    
}
