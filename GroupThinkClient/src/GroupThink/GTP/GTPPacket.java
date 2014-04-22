package GroupThink.GTP;

import java.nio.ByteBuffer;

public abstract class GTPPacket {

    int opCode;
    int intendedRecipient;
    byte[] bytes;

    GTPPacket(int o, int ir) {
        opCode = o;
        intendedRecipient=ir;
    }
    
    GTPPacket(byte[] b) {
        byte[] op = new byte[2];
        op[0] = b[0];
        op[1] = b[1];
        
        ByteBuffer bb = ByteBuffer.wrap(op);
        opCode = (int)bb.getShort();
        
        byte[] irb = new byte[2];
        irb[0] = b[2];
        irb[1] = b[3];
        
        bb = ByteBuffer.wrap(irb);
        intendedRecipient = (int)bb.getShort();
    }

    public int getOP() {
        return opCode;
    }

    public abstract byte[] getBytes();

    abstract void setBytes();

}
