package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;

public class PacketWorker implements Runnable {

    public PacketWorker(){}

    @Override
    public void run() {
        // Basically a switch for incoming packets to go to the proper subsystem
        System.out.println("Ready for action...");
        for(;;){
            GTPPacket packet = (GTPPacket)GroupThinkClient.packetQueue.pull();
            // While there are still packets, send to the correct place:
            if(packet!=null){
                System.out.println(packet);
                
                switch(packet.getOP()){
                    case 1: // handle WCP (write)
                        handleWCP((WCP) packet);
                        break;
                    case 2: // handle DCP (delete)
                        handleDCP((DCP) packet);
                        break;
                    case 3: // handle RPP (report position)
                        handleRPP((RPP) packet);
                        break;
                    case 4: // handle CVP (commit vote)
                        handleCVP((CVP) packet);
                        break;
                    case 5: // handle URP (username request packet)
                        handleURP((URP) packet);
                        break;
                    case 6: // handle UCP (username confirm)
                        handleUCP((UCP) packet); // need to add the user/id
                        break;
                    case 7: // handle EP  (error)
                        handleEP((EP) packet);
                        break;
                    case 8: // handle CMP (chat message packet)
                        handleCMP((CMP) packet);
                        break;
                    case 9: // handle DATA (transmit entire document)
                        handleData((Data) packet);
                        break;
                    case 13: // handle HP (transmit entire document)
                        handleHP((HP) packet);
                        break;
                }
            } 
            // If no packets in queue, block until something is added:
            else {
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

    private void handleWCP(WCP wcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleDCP(DCP dcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleRPP(RPP rpp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleCVP(CVP cvp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleURP(URP urp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // Handler for username confirmation:
    private void handleUCP(UCP p){
        GroupThinkClient.addUser(p.getUsername(), p.getUserID());
    }

    private void handleEP(EP ep) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // Handler for chat message packets:
    private void handleCMP(CMP p){
        //display if -1 or = to myID

        int intendedUser = GroupThinkClient.PacketSniffer.intendedRecipient(p.getBytes());

        if(intendedUser == -1 || intendedUser == GroupThinkClient.myID){
            GroupThinkClient.displayChatMessage(p);
        }

        System.out.println(p);
    }
    
    // Handler for receiving the entire document:
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

    // Handler for heart beats
    private void handleHP(HP hp){
        setActive((int)hp.getUserID());
    }
    
    private void setActive(int id){
        User user = GroupThinkClient.idToUser.get(id);
        
        if(user!=null){
            user.setLastHeartbeat(System.nanoTime());
            user.setActive(true);
        }
    }
}