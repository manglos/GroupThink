package GroupThink.GTP;


import java.nio.ByteBuffer;

public class TCP extends GTPPacket {

    byte[] bytes;
    int logCount;
    int[] queue;

    public TCP(int ir, int lc, int... q) {
        super(12, ir);
        logCount = lc;
        queue=q;
        
        byte[] b = new byte[calcBytesLength()];

        

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)super.opCode);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];
        

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)super.intendedRecipient);
        byte[] n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)logCount);
        byte[] cBytes = dbuf.array();

        b[4] = cBytes[0];
        b[5] = cBytes[1];

        for(int i=0;i<queue.length;i++){
            dbuf = ByteBuffer.allocate(2);
            dbuf.putShort((short)queue[i]);
            byte[] qb = dbuf.array();
            b[(2*i)+6] = qb[0];
            b[(2*i)+7] = qb[1];
        }
        
        b[b.length-1] = -1;

        this.bytes = b;
        setBytes();
    }


    public TCP(byte[] b){
        super(b);
        
        byte[] op = new byte[2];
        op[0] = b[0];
        op[1] = b[1];
        
        ByteBuffer bb = ByteBuffer.wrap(op);
        opCode = (int)bb.getShort();
        
        byte[] ir = new byte[2];
        ir[0] = b[2];
        ir[1] = b[3];
        
        bb = ByteBuffer.wrap(ir);
        intendedRecipient = bb.getShort();
        
        byte[] lb = new byte[2];
        lb[0] = b[4];
        lb[1] = b[5];
        
        bb = ByteBuffer.wrap(lb);
        logCount = (int)bb.getShort();
        
        int stopIndex=-1;
        for(int i=6;i<b.length && stopIndex<0;i++){
            if(b[i]==-1)
                stopIndex=i;
        }
        
        int userCount=0;
        queue = new int[(stopIndex-6)/2];
        
        for(int i=6;i<stopIndex;i+=2){
            byte[] ub = new byte[2];
            ub[0] = b[i];
            ub[1] = b[i+1];

            bb = ByteBuffer.wrap(ub);
            queue[userCount++] = (int)bb.getShort();
        }
        

        this.bytes=b;
        setBytes();
    }
    
    public void addRequester(int ui){
        
        int qLength = queue.length;
        int[] newQueue = new int[qLength+1];
        
        for(int i=0;i<qLength;i++){
            newQueue[i]=queue[i];
        }
        
        newQueue[qLength] = ui;
        
        queue = newQueue;
        
        byte[] b = new byte[calcBytesLength()];

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)super.opCode);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];
        

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)super.intendedRecipient);
        byte[] n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)logCount);
        byte[] cBytes = dbuf.array();

        b[4] = cBytes[0];
        b[5] = cBytes[1];

        for(int i=0;i<queue.length;i++){
            dbuf = ByteBuffer.allocate(2);
            dbuf.putShort((short)queue[i]);
            byte[] qb = dbuf.array();
            b[(2*i)+6] = qb[0];
            b[(2*i)+7] = qb[1];
        }
        
        b[b.length-1] = -1;

        this.bytes = b;
        setBytes();
        
        
    }
    
    public int getLogCount(){
        return logCount;
    }

    public int calcBytesLength() {
        return 6 + (2*queue.length) + 1;
    }

    public void setBytes() {//https://github.com/manglos/GroupThink.git
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
    
    @Override
    public String toString(){
        String q="(" + queue[0];
        
        for(int i=1;i<queue.length;i++)
            q+=","+queue[i];
        
        q+=")";
        
        return "TCP: LogCount " + getLogCount()+ " Queue " + q + " " + super.toString();
    }
}
