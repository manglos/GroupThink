package GroupThink.GTP;


import java.nio.ByteBuffer;

public class UCP extends Packet {

    short userID;

    public UCP(short user) {
        super(6);
        userID = user;
        
        byte[] b = new byte[4];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] n = dbuf.array();

        b[0] = n[0];
        b[1] = n[1];
        
        //dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(userID);
        n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];
        
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
        
        byte[] ui = new byte[2];
        ui[0] = b[2];
        ui[1] = b[3];
        
        bb = ByteBuffer.wrap(ui);
        userID = bb.getShort();

        this.bytes=b;
        setBytes();
    }

    public short getUserID() {
        return userID;
    }
    
    public void setBytes() {
       super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }

}
