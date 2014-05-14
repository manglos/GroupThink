/*
 * Sends and listens for heartbeats, also determines the leader.
 */

package groupthinkclient;

import GroupThink.GTP.CCP;
import GroupThink.GTP.HP;
import static groupthinkclient.GroupThinkClient.setEnableEditing;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angie
 */
public class CommitWorker implements Runnable {
    private long clientTimeout; // time you haven't heard from a user, remove from group



    @Override
    public void run() {

        System.out.println("Ready for votes!");
        long temp = System.nanoTime();
        boolean commit=false;

        while((System.nanoTime()-temp) < 30*1000000){

            synchronized (GroupThinkClient.voteCount){
                try {
                    GroupThinkClient.voteCount.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Current vote count = " + GroupThinkClient.voteCount.get());
            System.out.println("Current ratio = " + (double)(GroupThinkClient.voteCount.get()/GroupThinkClient.idToUser.size()));
            if((double)(GroupThinkClient.voteCount.get()/GroupThinkClient.idToUser.size()) > .75){
                try {
                    GroupThinkClient.UDPMultiCaster.sendPacket(new CCP((short)GroupThinkClient.myID.get(), true));
                    commit=true;
                } catch (IOException ex) {
                    Logger.getLogger(CommitWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }

        }

        if(!commit){
            try {
                //commit didn't happen
                GroupThinkClient.UDPMultiCaster.sendPacket(new CCP((short)GroupThinkClient.myID.get(), false));
            } catch (IOException ex) {
                Logger.getLogger(CommitWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}