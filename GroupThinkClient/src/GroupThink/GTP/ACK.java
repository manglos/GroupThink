package GroupThink.GTP;


import java.nio.ByteBuffer;

public class ACK extends GTPPacket {

    int blocknum;
    byte[] bytes;
    short userID;

    public ACK(byte[] b) {
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

        byte[] bn = new byte[2];
        bn[0] = b[6];
        bn[1] = b[7];

        bb = ByteBuffer.wrap(bn);
        blocknum = (int)bb.getShort();
        this.bytes=b;
        setBytes();
    }

    public ACK(int ir, short user, int bn) {
        super(10, ir);
        userID=user;
        blocknum = bn;
        byte[] b = new byte[8];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];

        num = (short) blocknum;

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

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] bnBytes = dbuf.array();

        b[6] = bnBytes[0];
        b[7] = bnBytes[1];

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

    public int getBlocknum(){
        return blocknum;
    }

    public String toString(){
        return "ACK: UserID " + userID + " blocknum " + blocknum + " " + super.toString();
    }

}