package GroupThink.GTP;


import java.nio.ByteBuffer;

public class UCP extends GTPPacket {

    short userID;
    String username;
    byte[] usernameBytes;

    public UCP(short ir, short user, String un) {
        super(6, ir);
        userID = user;
        username=un;
        usernameBytes=un.getBytes();
        
        byte[] b = new byte[calcBytesLength()];

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
        
        for (int i = 0; i < usernameBytes.length; i++) {
            b[6 + i] = usernameBytes[i];
        }

        b[6 + usernameBytes.length] = 0;
       
        
        this.bytes = b;
        
        setBytes();
    }
    
    public UCP(byte[] b) throws WrongPacketTypeException{
        super(b);
        
        if(super.getOP()!=6){
            throw new WrongPacketTypeException("Not a valid UCP Packet");
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
        
        int stopIndex=-1;
        
        for(int i = 6;i<b.length && stopIndex<0;i++){
            if(b[i]==0)
                stopIndex=i;
        }
        
        
        byte[] ub = new byte[stopIndex-6];
        
        for(int i=0;i<ub.length;i++){
            if(b[i+6]!=0)
                ub[i] = b[i+6];                
        }
        
        username = new String(ub);

        this.bytes=b;
        setBytes();
    }
    
    public int calcBytesLength() {
        return 6 + usernameBytes.length + 1;
    }

    public short getUserID() {
        return userID;
    }
    
    public String getUsername(){
        return username;
    }
    
    public void setBytes() {
       super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
    
    public String toString(){
        return "UCP: ID " + getUserID() + " Username " + getUsername() + " " + super.toString();
    }

}
