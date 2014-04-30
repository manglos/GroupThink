/*
 * Sends and listens for heartbeats, also determines the leader.
 */

package groupthinkclient;

/**
 *
 * @author angie
 */
public class HeartbeatWorker implements Runnable {
    private long clientTimeout; // time you haven't heard from a user, remove from group
    

    @Override
    public void run() {
        // listen for heartbeats (w/ Zs)
        // any heartbeat leader?
        // if (! leader) {
          // are any when did I last hear from a leader
        //}
        
        // send heartbeat w/ leader-flag
    }  
}
