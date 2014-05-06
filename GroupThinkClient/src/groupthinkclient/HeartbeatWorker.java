/*
 * Sends and listens for heartbeats, also determines the leader.
 */

package groupthinkclient;

import GroupThink.GTP.HP;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author angie
 */
public class HeartbeatWorker implements Runnable {
    private long clientTimeout; // time you haven't heard from a user, remove from group
    

    @Override
    public void run() {
        
        //First, send my own hearbeat to everyone
        try{
            GroupThinkClient.UDPMultiCaster.sendPacket(new HP((short)GroupThinkClient.myID, GroupThinkClient.leader.get()));
        }catch(IOException ex){
            ex.printStackTrace();
        }
        
        //Next, iterate through my user map and decide if user is active or inactive
        Iterator<Map.Entry<Integer, User>> it = GroupThinkClient.idToUser.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<Integer, User> userEntry = it.next();
            User user = userEntry.getValue();
            
            if((System.nanoTime() - user.getLastHeartbeat()) > GroupThinkClient.ACTIVE_TIMEOUT){
                user.setActive(false);
            }
            else{
                user.setActive(true);
            }
            
        }
        
    }  
}
