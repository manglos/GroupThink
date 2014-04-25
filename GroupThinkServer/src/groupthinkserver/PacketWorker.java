

package groupthinkserver;

import GroupThink.GTP.*;
import java.io.IOException;

public class PacketWorker implements Runnable{

    public PacketWorker(){}
    
    @Override
    public void run() {
    
        System.out.println("Ready for action...");
        for(;;){
            GTPPacket p = (GTPPacket)GroupThinkServer.myQueue.pull();
            
            if(p!=null){
                System.out.println("PACKET WORKER: " + p);
                switch(p.getOP()){
                    case 5:
                        handleURP((URP)p);
                        break;
                    case 10:
                        handleACK((ACK)p);
                        break;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    private void handleACK(ACK ack){
        GTPPacket response;
        System.out.println(ack);
        try {
            if(GroupThinkServer.existingDocument){
                DataList dl = new DataList(ack.getUserID(), GroupThinkServer.BLOCKSIZE, GroupThinkServer.DocumentSerializer.document.getBytes());
                Data d = dl.get(ack.getBlocknum());
                
                response = d;
            }
            else{
                response = new EP(ack.getUserID(), 4, "No Existing Document.");
            }
            
            if(response!=null)
                GroupThinkServer.UDPMultiCaster.sendPacket(response);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void handleURP(URP urp){
        GTPPacket response;

        try {
            String requestedUsername = (urp.getUsername());
            if(GroupThinkServer.clients.contains(requestedUsername)){
               response = new EP(-1, 3, "'"+requestedUsername + "' is unavailable.");
               GroupThinkServer.UDPMultiCaster.sendPacket(response);
            }
            else{
                GroupThinkServer.clients.add(requestedUsername);
                response = new UCP((short)-1, (short)GroupThinkServer.clients.indexOf(requestedUsername), requestedUsername);
                GroupThinkServer.UDPMultiCaster.sendPacket(response);
                
                for(int i=1;i<GroupThinkServer.clients.size();i++){
                    String un = GroupThinkServer.clients.get(i);
                    if(!un.equals(requestedUsername)){
                        response = new UCP((short)-1, (short)i, un);
                        GroupThinkServer.UDPMultiCaster.sendPacket(response);
                    }
                }
                //System.out.println(response);
            }
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
