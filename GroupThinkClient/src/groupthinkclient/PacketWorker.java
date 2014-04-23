

package groupthinkclient;

import GroupThink.GTP.*;

public class PacketWorker implements Runnable{

    public PacketWorker(){}
    
    @Override
    public void run() {
    
        System.out.println("Ready for action...");
        for(;;){
            GTPPacket p = (GTPPacket)GroupThinkClient.myQueue.pull();
            
            if(p!=null){
                System.out.println(p);
            }
        }
        
    }
    
}
