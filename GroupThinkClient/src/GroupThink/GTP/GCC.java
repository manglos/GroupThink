package GroupThink.GTP;


import java.nio.ByteBuffer;

public class GCC extends GTPPacket {

    Long globalIndex;
    int xPos, yPos;
    byte[] bytes;
    short userID;
    char myChar;
    boolean write;

    public GCC(byte[] b) {
        super(b);

        byte[] ir = new byte[2];
        ir[0] = b[2];
        ir[1] = b[3];

        ByteBuffer bb = ByteBuffer.wrap(ir);
        intendedRecipient = bb.getShort();

        byte[] ub = new byte[2];
        ub[0] = b[4];
        ub[1] = b[5];

        bb = ByteBuffer.wrap(ub);
        userID = bb.getShort();

        byte[] bn = new byte[8];
        bn[0] = b[6];
        bn[1] = b[7];
        bn[2] = b[8];
        bn[3] = b[9];
        bn[4] = b[10];
        bn[5] = b[11];
        bn[6] = b[12];
        bn[7] = b[13];

        bb = ByteBuffer.wrap(bn);
        globalIndex = bb.getLong();
        
        byte[] xb = new byte[2];
        xb[0] = b[14];
        xb[1] = b[15];

        bb = ByteBuffer.wrap(xb);
        xPos = bb.getShort();
        
        byte[] yb = new byte[2];
        yb[0] = b[16];
        yb[1] = b[17];

        bb = ByteBuffer.wrap(yb);
        yPos = bb.getShort();
        
        byte[] cb = new byte[2];
        cb[0] = b[18];
        cb[1] = b[19];

        bb = ByteBuffer.wrap(cb);
        myChar = bb.getChar();
        
        this.bytes=b;
        setBytes();
    }

    public GCC(int ir, short ui, Long gi, short x, short y, char c, boolean w) {
        super(16, ir);
        userID=ui;
        globalIndex = gi;
        xPos=x;
        yPos=y;
        myChar=c;
        write=w;
        byte[] b = new byte[21];

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

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(userID);
        n = dbuf.array();

        b[4] = n[0];
        b[5] = n[1];

        dbuf = ByteBuffer.allocate(8);
        dbuf.putLong(globalIndex);
        byte[] bnBytes = dbuf.array();

        b[6] = bnBytes[0];
        b[7] = bnBytes[1];
        b[8] = bnBytes[2];
        b[9] = bnBytes[3];
        b[10] = bnBytes[4];
        b[11] = bnBytes[5];
        b[12] = bnBytes[6];
        b[13] = bnBytes[7];
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)xPos);
        n = dbuf.array();

        b[14] = n[0];
        b[15] = n[1];
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort((short)yPos);
        n = dbuf.array();

        b[16] = n[0];
        b[17] = n[1];
        
        dbuf = ByteBuffer.allocate(2);
        dbuf.putChar(myChar);
        n = dbuf.array();

        b[18] = n[0];
        b[19] = n[1];
        
        if(write){
            b[20]=1;
        }
        else{
            b[20]=0;
        }

        this.bytes = b;
        setBytes();
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }

    public int getUserID(){
        return (int)userID;
    }

    public Long getGlobalIndex(){
        return globalIndex;
    }
    
    public int getXPos(){
        return xPos;
    }
    
    public int getYPos(){
        return yPos;
    }
    
    public char getChar(){
        return myChar;
    }
    
    public boolean isWrite(){
        return write;
    }

    public String toString(){
        return "GCC: UserID " + userID + " globalIndex " + getGlobalIndex() + " " + super.toString();
    }

}
