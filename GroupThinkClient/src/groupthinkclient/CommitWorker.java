/*
 * Sends and listens for heartbeats, also determines the leader.
 */

package groupthinkclient;

import GroupThink.GTP.HP;
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

        long temp = System.nanoTime();
        boolean commit=false;

        while((System.nanoTime()-temp) > 30*1000000){

            synchronized (GroupThinkClient.voteCount){
                try {
                    GroupThinkClient.voteCount.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            if((double)(GroupThinkClient.voteCount.get()/GroupThinkClient.idToUser.size()) > .75){
                //commit file
                //send out ccp
                commit=true;
            }

        }

        if(!commit){
            //commit didn't happen
        }


    }
}
