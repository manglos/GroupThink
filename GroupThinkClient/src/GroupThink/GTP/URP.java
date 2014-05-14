package GroupThink.GTP;


import java.nio.ByteBuffer;

public class URP extends GTPPacket {

    String username;
    byte[] usernameBytes;
    byte[] bytes;

    public URP(String un) {
        super(5, -1);
        username = un;
        usernameBytes = username.getBytes();
        
        byte[] b = new byte[calcBytesLength()];
        
        

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];
        
        short iu = (short) super.intendedRecipient;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)super.intendedRecipient);
        byte[] n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];

        for (int i = 0; i < usernameBytes.length; i++) {
            b[4 + i] = usernameBytes[i];
        }

        b[b.length-1] = 0;

        this.bytes = b;
        setBytes();
    }


    public URP(byte[] b) throws WrongPacketTypeException{
        super(b);
        
        if(super.getOP()!=5){
            throw new WrongPacketTypeException("Not a valid URP Packet");
        }
        
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
        
        int stopIndex=-1;
        
        for(int i = 4;i<b.length && stopIndex<0;i++){
            if(b[i]==0)
                stopIndex=i;
        }
        
        
        byte[] ub = new byte[stopIndex-4];
        
        for(int i=0;i<ub.length;i++){
            if(b[i+4]!=0)
                ub[i] = b[i+4];                
        }
        
        
        
        username = new String(ub);

        this.bytes=b;
        setBytes();
    }
    
    public String getUsername() {
        return username;
    }

    public int calcBytesLength() {
        return 4 + usernameBytes.length + 1;
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
    
    public String toString(){
        return "URP: " + getUsername() + " " + super.toString();
    }
}
