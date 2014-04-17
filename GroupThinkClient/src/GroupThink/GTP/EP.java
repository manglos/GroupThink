package GroupThink.GTP;


import java.nio.ByteBuffer;

public class EP extends GTPPacket {

    String message;
    int code;
    byte[] messageBytes;
    byte[] bytes;

    public EP(int c, String m) {
        super(7);
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

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] cBytes = dbuf.array();

        b[2] = cBytes[0];
        b[3] = cBytes[1];

        for (int i = 0; i < messageBytes.length; i++) {
            b[4 + i] = messageBytes[i];
        }

        b[4 + messageBytes.length] = 0;

        this.bytes = b;
        setBytes();
    }


    public EP(byte[] b){
        super(b);
        
        byte[] ec = new byte[2];
        ec[0] = b[2];
        ec[1] = b[3];
        
        ByteBuffer bb = ByteBuffer.wrap(ec);
        code = (int)bb.getShort();
        
        int stopIndex=-1;
        for(int i=4;i<b.length && stopIndex<0;i++){
            if(b[i]==0)
                stopIndex=i;
        }
        
        byte[] mb = new byte[stopIndex-4];
        
        for(int i=0;i<mb.length;i++){
            mb[i] = b[i+4];
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
        return 4 + messageBytes.length + 1;
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
}
