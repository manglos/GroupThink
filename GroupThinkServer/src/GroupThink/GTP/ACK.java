package GroupThink.GTP;


import java.nio.ByteBuffer;

public class ACK extends GTPPacket {

    int blocknum;
    byte[] bytes;

    public ACK(byte[] b) {
        super(b);

        byte[] bn = new byte[2];
        bn[0] = b[2];
        bn[1] = b[3];
        
        ByteBuffer bb = ByteBuffer.wrap(bn);
        blocknum = (int)bb.getShort();
        this.bytes=b;
        setBytes();
    }

    public ACK(int bn) {
        super(4);

        blocknum = bn;
        byte[] b = new byte[calcBytesLength()];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];

        num = (short) blocknum;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] bnBytes = dbuf.array();

        b[2] = bnBytes[0];
        b[3] = bnBytes[1];

        this.bytes = b;
        setBytes();
    }

    public int calcBytesLength() {
        return 4;
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
}
