package groupthinkclient;

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
                    GlobalChange g = new GlobalChange(GroupThinkClient.highestSequentialChange.get(), lc);
                    GroupThinkClient.gChanges.put(GroupThinkClient.highestSequentialChange.get(), g);
                    GroupThinkClient.highestSequentialChange.getAndIncrement();
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
