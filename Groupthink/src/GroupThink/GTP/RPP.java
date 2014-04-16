package GroupThink.GTP;


import java.nio.ByteBuffer;

public class RPP extends Packet {

    short userID, lineNumber, spaceNumber;

    public RPP(short user, short line, short space) {
        super(3);
        userID = user;
        lineNumber = line;
        spaceNumber = space;
        
        byte[] b = new byte[8];

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
        
        //ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(lineNumber);
        n = dbuf.array();

        b[4] = n[0];
        b[5] = n[1];
        
        //ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(spaceNumber);
        n = dbuf.array();

        b[6] = n[0];
        b[7] = n[1];
        
        this.bytes = b;
        
        setBytes();
    }
    
    public RPP(byte[] b) throws WrongPacketTypeException{
        super(b);
        
        if(super.getOP()!=3){
            throw new WrongPacketTypeException("Not a valid RPP Packet");
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
        
        byte[] ln = new byte[2];
        ln[0] = b[4];
        ln[1] = b[5];
        
        bb = ByteBuffer.wrap(ln);
        lineNumber = bb.getShort();
        
        byte[] spn = new byte[2];
        spn[0] = b[6];
        spn[1] = b[7];
        
        bb = ByteBuffer.wrap(spn);
        spaceNumber = bb.getShort();

        this.bytes=b;
        setBytes();
    }

    public short getUserID() {
        return userID;
    }

    public short getLineNumber() {
        return lineNumber;
    }
    
    public short getSpaceNumber() {
        return spaceNumber;
    }
    
    public void setBytes() {
       super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }

}
