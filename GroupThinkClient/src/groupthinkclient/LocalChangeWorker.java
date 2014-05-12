package groupthinkclient;

public class LocalChangeWorker implements Runnable{

    @Override
    public void run() {
        
        //wait for local change
        try {
            GroupThinkClient.lChange.wait();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        //Local Change Occurs
        
        //If Leader
        if(GroupThinkClient.leader.get()){
            //For each local change in queue
            LocalChange lc;
            while((lc = GroupThinkClient.lChange.poll())!=null){
                
                //make global change
                
                
            }
        }
        else{
            //request token
        }
        
        
        
    }
    
    
    
}
