package GroupThink.GTP;

import groupthinkserver.GroupThinkServer;

public class DataList{
    
    byte[] fileBytes;
    int blocksize;
    int currentPiece;
    int intendedRecipient;
    //String file;
    
    public DataList(int ir, int bs, byte[] f){
        blocksize=bs;
        currentPiece=0;
        //file=f;
        fileBytes = f;
        intendedRecipient = ir;
    }    
    public int getPacketNum() {
        return (int) Math.ceil((double) fileBytes.length / (double) blocksize);
    }
    
    public Data get(int bn){
        
        if((bn*blocksize)>=fileBytes.length){
            return null;
        }
        
        byte[] db = new byte[blocksize];
        
        for(int i=0;i<db.length;i++){
            if((i+(bn*blocksize))<fileBytes.length){
                db[i] = fileBytes[i+(bn*blocksize)];
            }
        }
        
        return new Data(intendedRecipient, bn, db);
    }
    
}