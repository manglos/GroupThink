package GroupThink.GTP;


import java.nio.ByteBuffer;

public class WCP extends GTPPacket {

    short userID, seqNumber, lineNumber, spaceNumber;
    char myChar;

    public WCP(short ir, short user, short seq, short line, short space, char c) {
        super(1, ir);
        userID = user;
        seqNumber = seq;
        lineNumber = line;
        spaceNumber = space;
        myChar = c;
        
        byte[] b = new byte[13];

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
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(seqNumber);
        n = dbuf.array();

        b[6] = n[0];
        b[7] = n[1];
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(lineNumber);
        n = dbuf.array();

        b[8] = n[0];
        b[9] = n[1];
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(spaceNumber);
        n = dbuf.array();

        b[10] = n[0];
        b[11] = n[1];
        
        dbuf = ByteBuffer.allocate(1);
        dbuf.putChar(myChar);
        n = dbuf.array();
        
        b[12] = n[0];
        
        
        this.bytes = b;
        
        setBytes();
    }
    
    public WCP(byte[] b) throws WrongPacketTypeException{
        super(b);
        
        if(super.getOP()!=1){
            throw new WrongPacketTypeException("Not a valid WCP Packet");
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
        
        byte[] sn = new byte[2];
        sn[0] = b[6];
        sn[1] = b[7];
        
        bb = ByteBuffer.wrap(sn);
        seqNumber = bb.getShort();
        
        byte[] ln = new byte[2];
        ln[0] = b[8];
        ln[1] = b[9];
        
        bb = ByteBuffer.wrap(ln);
        lineNumber = bb.getShort();
        
        byte[] spn = new byte[2];
        spn[0] = b[10];
        spn[1] = b[11];
        
        bb = ByteBuffer.wrap(spn);
        spaceNumber = bb.getShort();
        
        byte[] ch = new byte[1];
        ch[0] = b[12];
        
        bb = ByteBuffer.wrap(op);
        myChar = bb.getChar();
        

        this.bytes=b;
        setBytes();
    }

    public short getUserID() {
        return userID;
    }

    public short getSeqNumber() {
        return seqNumber;
    }

    public short getLineNumber() {
        return lineNumber;
    }
    
    public short getSpaceNumber() {
        return spaceNumber;
    }
    
    public char getChar() {
        return myChar;
    }
    
    public void setBytes() {
       super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }

}
