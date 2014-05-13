package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;

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
                    case 11: // handle TRP (request for leadership)
                        handleTRP((TRP)packet);
                        break;
                    case 12: // handle TCP (Confirm Leadership)
                        handleTCP((TCP)packet);
                        break;
                    case 13: // handle HP (heartbeats)
                        handleHP((HP) packet);
                        break;
                    case 15: // handle GCP (Global Change Request)
                        handleGCP((GCP) packet);
                        break;
                    case 16: // handle GCC (Global Change Packet)
                        handleGCC((GCC) packet);
                        break;
                    case 17: // handle TDP (Token Declined)
                        handleTDP((TDP) packet);
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
        if (wcp.getUserID() != (short) GroupThinkClient.myID.get()) {
            System.out.println("=>RECEIVING PACKET!");
            int position = wcp.getPosition();
            String s = "" + wcp.getChar();
            synchronized (GroupThinkClient.editor) {
                GroupThinkClient.logger.setActivation(false);
                try {
                    GroupThinkClient.editor.getDocument().insertString(position, s, null);
                } catch (BadLocationException ex) {
                    Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                GroupThinkClient.logger.setActivation(true);
            }
        }
    }

    private void handleDCP(DCP dcp) {
        if (dcp.getUserID() != (short) GroupThinkClient.myID.get()) {
            System.out.println("=>RECEIVING DELETE PACKET!");
            int position = dcp.getPosition();
            synchronized (GroupThinkClient.editor) {
                GroupThinkClient.logger.setActivation(false);
                try {
                    GroupThinkClient.editor.getDocument().remove(position, 1);
                } catch (BadLocationException ex) {
                    Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                GroupThinkClient.logger.setActivation(true);
            }
        }
    }

    private void handleRPP(RPP rpp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleCVP(CVP cvp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleURP(URP urp) {
        System.out.println("Handeling " + urp + "   " + GroupThinkClient.username.get());
        if(!urp.getUsername().equals(GroupThinkClient.username.get())){
            System.out.println("not me");
            if(GroupThinkClient.leader.get()){
                System.out.println("I'm the leader");
                int highest=-1;
                for(int k : GroupThinkClient.idToUser.keySet()){
                    
                    if(k>highest){
                        highest=k;
                    }
                }
                try{
                    //short s = highest+1;
                    highest+=1;
                    System.out.println("sending ucp for " + highest);
                    GroupThinkClient.UDPMultiCaster.sendPacket(new UCP((short)-1, (short)highest, urp.getUsername()));
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        }
        
        for(int k : GroupThinkClient.idToUser.keySet()){
            try{
            GroupThinkClient.UDPMultiCaster.sendPacket(new UCP((short)-1, (short)k, GroupThinkClient.idToUser.get(k).getUsername()));
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        
    }
    
    // Handler for username confirmation:
    private void handleUCP(UCP p){
        
        if(p.getUsername().equals(GroupThinkClient.username.get())){
            synchronized(GroupThinkClient.myID){
                GroupThinkClient.myID.compareAndSet(-1, p.getUserID());
                
                GroupThinkClient.addUser(GroupThinkClient.username.get(), GroupThinkClient.myID.get());
                GroupThinkClient.myID.notifyAll();
                return;
            }
        }
        
        GroupThinkClient.addUser(p.getUsername(), p.getUserID());
        
    }

    private void handleEP(EP ep) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // Handler for chat message packets:
    private void handleCMP(CMP p){
        //display if -1 or = to myID

        int intendedUser = GroupThinkClient.PacketSniffer.intendedRecipient(p.getBytes());

        if(intendedUser == -1 || intendedUser == GroupThinkClient.myID.get()){
            GroupThinkClient.displayChatMessage(p, !(intendedUser==-1));
        }

        //System.out.println(p);
    }
    
    // Handler for receiving the entire document:
    private void handleData(Data d){
        if(((GTPPacket)d).getIntendedRecipient()!=GroupThinkClient.myID.get())
            return;

        // note: use change list instead
        //GroupThinkClient.myDataList.addData(d);

        //GroupThinkClient.editor.append(GroupThinkClient.myDataList.getFile());

        try {
            GroupThinkClient.UDPMultiCaster.sendPacket(new ACK(0, (short)GroupThinkClient.myID.get(), d.getBlockNum()+1));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Handler for heart beats
    private void handleHP(HP hp){
        setActive((int)hp.getUserID());
        
        if(hp.getUserID()==GroupThinkClient.myID.get())
            return;
        
        //User thinks they're the leader
        if(hp.isLeader()){
            
            System.out.println("Got packet from 'leader', checking it they have " + hp.getLogCount() + ", I have " + GroupThinkClient.highestSequentialChange);
            //Wait a sec, I think I'm the leader!!!
            if(GroupThinkClient.leader.get()){
                
                //I must be wrong
                if(GroupThinkClient.highestSequentialChange<hp.getLogCount()){
                    System.out.println("I was wrong...");
                    GroupThinkClient.currentLeader=hp.getUserID();
                    GroupThinkClient.leader.compareAndSet(true, false);
                }
                else{//User must be wrong
                    System.out.println("They're wrong!!!");
                    try {
                        //Send a token request (don't care if I receive it)
                        GroupThinkClient.UDPMultiCaster.sendPacket(new TRP(hp.getUserID(), GroupThinkClient.highestSequentialChange));
                    } catch (IOException ex) {
                        Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
    }
    
    private void handleTRP(TRP trp){
        
        //if i'm not the leader, I really don't care about this
        if(!GroupThinkClient.leader.get())
            return;
        
        //ONLY send a Token if our logCounts match
        if(GroupThinkClient.highestSequentialChange==trp.getLogCount()){
            try {
                GroupThinkClient.token.changeRecipient(trp.getUserID());
                GroupThinkClient.leader.compareAndSet(true,false);
                TCP t = GroupThinkClient.token;
                GroupThinkClient.token=null;
                //System.out.println("Sending Token " + GroupThinkClient.token);
                GroupThinkClient.UDPMultiCaster.sendPacket(t);
                //System.out.println("Sent.");
                //GroupThinkClient.UDPMultiCaster.printBytes(GroupThinkClient.token.getBytes());
                return;
            } catch (IOException ex) {
                Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            //Otherwise, every client needs to update their global counts,
            //and can't get a token unless they're fully up-to-date
            System.out.println("Rejecting Token Request");
            GroupThinkClient.UDPMultiCaster.sendPacket(new TDP(trp.getUserID(), (short)GroupThinkClient.myID.get(), GroupThinkClient.highestSequentialChange));
        } catch (IOException ex) {
            Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void setActive(int id){
        User user = GroupThinkClient.idToUser.get(id);
        
        if(user!=null){
            user.setLastHeartbeat(System.nanoTime());
            user.setActive(true);
        }
    }
    
    private void handleLOP(LOP p){
        GroupThinkClient.removeUser(p.getUserID());
        //TODO handle the case of the leader logging out...
    }
    
    private void handleGCP(GCP gcp){
        if(GroupThinkClient.gChanges.containsKey(gcp.getGlobalIndex())){
            GlobalChange g = GroupThinkClient.gChanges.get(gcp.getGlobalIndex());
            GCC gcc = new GCC((short)gcp.getUserID(), (short)GroupThinkClient.myID.get(), gcp.getGlobalIndex(), g.getPosition(), g.getChar(), g.isWrite());
            try {
                GroupThinkClient.UDPMultiCaster.sendPacket(gcc);
            } catch (IOException ex) {
                Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void handleGCC(GCC gcc){
        GlobalChange gc = new GlobalChange(gcc.getGlobalIndex(), gcc.getPos(), gcc.getChar(), gcc.isWrite());
        GroupThinkClient.gChanges.put(gcc.getGlobalIndex(), gc);
    }
    
    private void handleTDP(TDP tdp){
        Iterator it = GroupThinkClient.gChanges.entrySet().iterator();
        
        ArrayList<Long> missing = new ArrayList<Long>();
        
        //get index for each missing global change
        for(long i=0;i<tdp.getLeadersIndex();i++){
            if(!GroupThinkClient.gChanges.containsKey(i)){
                missing.add(i);
            }
        }
        
        //for each missing change, send out a request to everyone for that change
        for(long index : missing){
            try {
                GroupThinkClient.UDPMultiCaster.sendPacket(new GCP(-1, (short)GroupThinkClient.myID.get(), index));
            } catch (IOException ex) {
                Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void handleTCP(TCP tcp){
        GroupThinkClient.token = tcp;
        GroupThinkClient.leader.compareAndSet(false, true);
    }
}
