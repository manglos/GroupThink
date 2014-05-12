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
public class HeartbeatWorker implements Runnable {
    private long clientTimeout; // time you haven't heard from a user, remove from group
    

    public HeartbeatWorker(Long to){
        clientTimeout=to;
    }
    
    @Override
    public void run() {
        
        
        for(;;){
            

            //First, send my own hearbeat to everyone

            try{
                GroupThinkClient.UDPMultiCaster.sendPacket(new HP((short)GroupThinkClient.myID.get(), GroupThinkClient.leader.get(), GroupThinkClient.highestSequentialChange));
            }catch(IOException ex){
                ex.printStackTrace();
            }

            //Next, iterate through my user map and decide if user is active or inactive
            Iterator<Map.Entry<Integer, User>> it = GroupThinkClient.idToUser.entrySet().iterator();

            while(it.hasNext()){
                Map.Entry<Integer, User> userEntry = it.next();
                User user = userEntry.getValue();

                if((System.nanoTime() - user.getLastHeartbeat()) > clientTimeout){
                    user.setActive(false);
                    GroupThinkClient.chatNameList.setInactive(user.getUsername());
                }
                else{
                    user.setActive(true);
                    GroupThinkClient.chatNameList.setActive(user.getUsername());
                }

            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
    }  
}
