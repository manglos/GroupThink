package GroupThink.GTP;


import java.nio.ByteBuffer;

public class TRP extends GTPPacket {

    short userID;
    Long logCount;

    public TRP(short user, Long lc) {
        super(11, -1);
        userID = user;
        logCount=lc;
        
        byte[] b = new byte[14];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] n = dbuf.array();

        b[0] = n[0];
        b[1] = n[1];
        
        short iu = (short) super.intendedRecipient;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(iu);
        n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(userID);
        n = dbuf.array();

        b[4] = n[0];
        b[5] = n[1];
        
        dbuf = ByteBuffer.allocate(8);
        dbuf.putLong(logCount);
        n = dbuf.array();

        b[6] = n[0];
        b[7] = n[1];
        b[8] = n[2];
        b[9] = n[3];
        b[10] = n[4];
        b[11] = n[5];
        b[12] = n[6];
        b[13] = n[7];
       
        
        this.bytes = b;
        
        setBytes();
    }
    
    public TRP(byte[] b) throws WrongPacketTypeException{
        super(b);
        
        if(super.getOP()!=11){
            throw new WrongPacketTypeException("Not a valid TRP Packet");
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
        
        byte[] ui = new byte[2];
        ui[0] = b[4];
        ui[1] = b[5];
        
        bb = ByteBuffer.wrap(ui);
        userID = bb.getShort();
        
        byte[] lc = new byte[8];
        lc[0] = b[6];
        lc[1] = b[7];
        lc[2] = b[8];
        lc[3] = b[9];
        lc[4] = b[10];
        lc[5] = b[11];
        lc[6] = b[12];
        lc[7] = b[13];
        
        bb = ByteBuffer.wrap(lc);
        logCount = bb.getLong();

        this.bytes=b;
        setBytes();
    }
    
    public short getUserID() {
        return userID;
    }
    
    public Long getLogCount(){
        return logCount;
    }
    
    public void setBytes() {
       super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
    
    public String toString(){
        return "TRP: ID " + getUserID() + " " + super.toString();
    }

}
