package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;

public class PacketWorker implements Runnable{

    public PacketWorker(){}

    @Override
    public void run() {

        System.out.println("Ready for action...");
        for(;;){
            GTPPacket p = (GTPPacket)GroupThinkClient.myQueue.pull();

            if(p!=null){
                System.out.println(p);
                switch(p.getOP()){
                    case 6:
                        handleUCP((UCP)p); //username confirm - need to add the user/id to the map
                        break;
                    case 8:
                        handleCMP((CMP)p);
                        break;
                    case 9:
                        handleData((Data)p);
                        break;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

    }

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

        GroupThinkClient.myDataList.addData(d);

        GroupThinkClient.editor.append(GroupThinkClient.myDataList.getFile());

        try {
            GroupThinkClient.UDPMultiCaster.sendPacket(new ACK(0, (short)GroupThinkClient.myID, d.getBlockNum()+1));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}