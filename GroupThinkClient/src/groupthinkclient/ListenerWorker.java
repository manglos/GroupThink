package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class ListenerWorker implements Runnable {

    int myPort;
    String myHost;

    public ListenerWorker(int p, String h){
        myPort=p;
        myHost=h;
    }

    @Override
    public void run() {
        System.out.println("Listening for packets...");
        for(;;){
            try{
                byte[] b = GroupThinkClient.UDPMultiCaster.receivePacket(); // blocks here
                int type = GroupThinkClient.PacketSniffer.packetType(b);
                int intendedUser = GroupThinkClient.PacketSniffer.intendedRecipient(b);
                
                //GroupThinkClient.UDPMultiCaster.printBytes(b);
                
                // <ANG> To do: Also check if WE are the sender!!
                if(intendedUser==-1 || intendedUser==GroupThinkClient.myID.get()){
                    try{
                        switch(type){
                            case 1:
                                GroupThinkClient.packetQueue.add(new WCP(b));
                                break;
                            case 2:
                                GroupThinkClient.packetQueue.add(new DCP(b));
                                break;
                            case 3:
                                GroupThinkClient.packetQueue.add(new RPP(b));
                                break;
                            case 4:
                                GroupThinkClient.packetQueue.add(new CVP(b));
                                break;
                            case 5:
                                GroupThinkClient.packetQueue.add(new URP(b));
                                break;
                            case 6:
                                GroupThinkClient.packetQueue.add(new UCP(b));
                                break;
                            case 7:
                                GroupThinkClient.packetQueue.add(new EP(b));
                                break;
                            case 8:
                                GroupThinkClient.packetQueue.add(new CMP(b));
                                break;
                            case 9:
                                GroupThinkClient.packetQueue.add(new Data(b));
                                break;
                            case 11:
                                GroupThinkClient.packetQueue.add(new TRP(b));
                                break;
                            case 12:
                                GroupThinkClient.packetQueue.add(new TCP(b));
                                break;
                            case 13:
                                GroupThinkClient.packetQueue.add(new HP(b));
                                break;
                            case 14:
                                GroupThinkClient.packetQueue.add(new LOP(b));
                                break;
                            case 15:
                                GroupThinkClient.packetQueue.add(new GCP(b));
                                break;
                            case 16:
                                GroupThinkClient.packetQueue.add(new GCC(b));
                                break;
                            case 17:
                                GroupThinkClient.packetQueue.add(new TDP(b));
                                break;
                            case 18:
                                GroupThinkClient.packetQueue.add(new CRP(b));
                                break;
                            case 19:
                                GroupThinkClient.packetQueue.add(new CCP(b));
                                break;
                        }
                        synchronized (GroupThinkClient.packetQueue) {
                            GroupThinkClient.packetQueue.notifyAll();
                        }
                    }catch(WrongPacketTypeException ex){
                        ex.printStackTrace();
                    }
                }
            }catch(SocketTimeoutException ex){
                ex.printStackTrace();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }
}