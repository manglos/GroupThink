package groupthinkclient;

import GroupThink.GTP.GCC;
import GroupThink.GTP.TRP;
import GroupThink.GTP.WCP;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalChangeWorker implements Runnable{
    
    @Override
    public void run() {
        
        for(;;){
            //wait for local change
            try {
                synchronized(GroupThinkClient.lChanges){
                    GroupThinkClient.lChanges.wait();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Local Change Occurs

            //If Leader
            if(GroupThinkClient.leader.get()){
                //For each local change in queue
                LocalChange lc;
                while((lc = GroupThinkClient.lChanges.poll())!=null){
                    //myLogger.addGlobally();
                    GlobalChange g = new GlobalChange(GroupThinkClient.highestSequentialChange.incrementAndGet(), lc);
                    GroupThinkClient.gChanges.put(GroupThinkClient.highestSequentialChange.get(), g);
                    //GlobalChange g = GroupThinkClient.gChanges.get(gcp.getGlobalIndex());
                    GCC gcc = new GCC((short)-1, (short)GroupThinkClient.myID.get(), GroupThinkClient.highestSequentialChange.get(), g.getPosition(), g.getChar(), g.isWrite());
                    try {
                        GroupThinkClient.UDPMultiCaster.sendPacket(gcc);
                    } catch (IOException ex) {
                        Logger.getLogger(PacketWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("ADDED LOCAL CHANGE TO GLOBAL " + lc);
                    
                }
            }
            else{
                try {
                    //request token
                    GroupThinkClient.UDPMultiCaster.sendPacket(new TRP((short)GroupThinkClient.myID.get(), GroupThinkClient.highestSequentialChange.get()));
                } catch (IOException ex) {
                    Logger.getLogger(LocalChangeWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
