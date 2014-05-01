package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;

public class PacketWorker implements Runnable{

    public PacketWorker(){}

    @Override
    public void run() {

        System.out.println("Ready for action...");
        for(;;){
            GTPPacket packet = (GTPPacket)GroupThinkClient.packetQueue.pull();

            if(packet!=null){
                System.out.println(packet);
                switch(packet.getOP()){
                    case 6:
                        handleUCP((UCP)packet); //username confirm - need to add the user/id to the map
                        break;
                    case 8:
                        handleCMP((CMP)packet);
                        break;
                    case 9:
                        handleData((Data)packet);
                        break;
                }
            } else {
                try {
                    // wait until someone adding to the queue notifies all:
                    synchronized (GroupThinkClient.packetQueue) {
                        GroupThinkClient.packetQueue.wait();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Handler for 
    private void handleUCP(UCP p){
        GroupThinkClient.addUser(p.getUsername(), p.getUserID());
    }

    private void handleCMP(CMP p){
        //display if -1 or = to myID

        int intendedUser = GroupThinkClient.PacketSniffer.intendedRecipient(p.getBytes());

        if(intendedUser == -1 || intendedUser == GroupThinkClient.myID){
            GroupThinkClient.displayChatMessage(p);
        }

        System.out.println(p);
    }

    private void handleData(Data d){
        if(((GTPPacket)d).getIntendedRecipient()!=GroupThinkClient.myID)
            return;

        // note: use change list instead
        //GroupThinkClient.myDataList.addData(d);

        //GroupThinkClient.editor.append(GroupThinkClient.myDataList.getFile());

        try {
            GroupThinkClient.UDPMultiCaster.sendPacket(new ACK(0, (short)GroupThinkClient.myID, d.getBlockNum()+1));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}