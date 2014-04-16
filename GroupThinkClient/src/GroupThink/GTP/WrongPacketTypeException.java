package GroupThink.GTP;

public class WrongPacketTypeException extends Exception {
  public WrongPacketTypeException() { super(); }
  public WrongPacketTypeException(String message) { super(message); }
  public WrongPacketTypeException(String message, Throwable cause) { super(message, cause); }
  public WrongPacketTypeException(Throwable cause) { super(cause); }
}
