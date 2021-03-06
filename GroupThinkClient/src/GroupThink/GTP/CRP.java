package GroupThink.GTP;

import java.nio.ByteBuffer;

/**
 * Created by wilhelmi on 5/14/14.
 */
public class CRP extends GTPPacket {

    short userID;

    public CRP( short user) {
        super(18, -1);
        userID = user;

        byte[] b = new byte[7];

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

        this.bytes = b;

        setBytes();
    }

    public CRP(byte[] b) throws WrongPacketTypeException{
        super(b);

        if(super.getOP()!=18){
            throw new WrongPacketTypeException("Not a valid CVP Packet");
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
