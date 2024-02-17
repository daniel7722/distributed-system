package sensor;
/*
 * Updated on Feb 2023
 */
import common.MessageInfo;
import java.io.IOException;
import java.net.UnknownHostException;

public interface ISensor {
  /* sends N measurementes to the Field Unit*/
  public void run(int N) throws InterruptedException, IOException;

    /* Send the message 'msg' to 'address' on port 'port' */
    public void sendMessage(String address, int port, MessageInfo msg) throws IOException;

    /* Simulate one measurement */
    public float getMeasurement();
}
