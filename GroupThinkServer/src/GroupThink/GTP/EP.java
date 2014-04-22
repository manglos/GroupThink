package GroupThink.GTP;


import java.nio.ByteBuffer;

public class EP extends GTPPacket {

    String message;
    int code;
    byte[] messageBytes;
    byte[] bytes;

    public EP(int ir, int c, String m) {
        super(7, ir);
        message = m;
        messageBytes = message.getBytes();
        code = c;
        
        byte[] b = new byte[calcBytesLength()];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];

        num = (short) code;
        
        short iu = (short) super.intendedRecipient;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(iu);
        byte[] n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] cBytes = dbuf.array();

        b[4] = cBytes[0];
        b[5] = cBytes[1];

        for (int i = 0; i < messageBytes.length; i++) {
            b[6 + i] = messageBytes[i];
        }

        b[6 + messageBytes.length] = 0;

        this.bytes = b;
        setBytes();
    }


    public EP(byte[] b){
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
        
        byte[] ec = new byte[2];
        ec[0] = b[4];
        ec[1] = b[5];
        
        bb = ByteBuffer.wrap(ec);
        code = (int)bb.getShort();
        
        int stopIndex=-1;
        for(int i=6;i<b.length && stopIndex<0;i++){
            if(b[i]==0)
                stopIndex=i;
        }
        
        byte[] mb = new byte[stopIndex-6];
        
        for(int i=0;i<mb.length;i++){
            mb[i] = b[i+6];
        }
        
        message = new String(mb);

        this.bytes=b;
        setBytes();
    }
    
    public int getErrorCode(){
        return code;
    }
    
    public String getMessage() {
        return message;
    }

    public int calcBytesLength() {
        return 6 + messageBytes.length + 1;
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
    
    @Override
    public String toString(){
        return "EP : " + getErrorCode()+ " - " + getMessage();
    }
}
