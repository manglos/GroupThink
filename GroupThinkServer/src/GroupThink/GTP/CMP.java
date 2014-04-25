package GroupThink.GTP;

import java.nio.ByteBuffer;

public class CMP extends GTPPacket {

    byte[] messageBytes;
    int userID;
    String message;
    byte[] bytes;
   
    public CMP(int ir, short ui, String m) {
        super(8, ir);
        userID = ui;
        message = m;
        messageBytes = m.getBytes();
        
        byte[] b = new byte[calcBytesLength()];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];
        
        short iu = (short) super.intendedRecipient;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(iu);
        byte[] n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];

        num = (short)userID;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] bnBytes = dbuf.array();

        b[4] = bnBytes[0];
        b[5] = bnBytes[1];

        for (int i = 0; i < messageBytes.length; i++) {
            b[6 + i] = messageBytes[i];
        }
        
        b[6+messageBytes.length]=0;
        
        this.bytes = b;
        setBytes();
    }
    
    public CMP(byte[] b) throws WrongPacketTypeException{
        super(b);
        if(super.getOP()!=8)
            throw new WrongPacketTypeException("Invalid CMP Packet.");
        
        
        byte[] ir = new byte[2];
        ir[0] = b[2];
        ir[1] = b[3];
        
        ByteBuffer bb = ByteBuffer.wrap(ir);
        intendedRecipient = bb.getShort();
        
        
        byte[] bn = new byte[2];
        bn[0] = b[4];
        bn[1] = b[5];
        
        bb = ByteBuffer.wrap(bn);
        userID = (int)bb.getShort();
        
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
    
    public int calcBytesLength() {
        return 6 + messageBytes.length + 1;
    }

    public void setBytes() {
        
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
    
    public String getMessage(){
        return message;
    }
    
    public int getUserID(){
        return userID;
    }
    
    @Override
    public String toString(){
        return "CMP : USER " + getUserID()+ " MESSAGE: " + getMessage() + " " + super.toString();
    }
  
}
