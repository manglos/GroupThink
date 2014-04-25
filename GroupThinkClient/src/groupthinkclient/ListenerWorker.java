package groupthinkclient;

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
                                break;
                            case 2:
                                GroupThinkClient.myQueue.add(new DCP(b));
                                break;
                            case 3:
                                GroupThinkClient.myQueue.add(new RPP(b));
                                break;
                            case 4:
                                GroupThinkClient.myQueue.add(new CVP(b));
                                break;
                            case 5:
                                GroupThinkClient.myQueue.add(new URP(b));
                                break;
                            case 6:
                                GroupThinkClient.myQueue.add(new UCP(b));
                                break;
                            case 7:
                                GroupThinkClient.myQueue.add(new EP(b));
                                break;
                            case 8:
                                GroupThinkClient.myQueue.add(new CMP(b));
                                break;
                            case 9:
                                GroupThinkClient.myQueue.add(new Data(b));
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