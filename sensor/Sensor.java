package sensor;

/*
 * Updated on Feb 2023
 */
import common.MessageInfo;

import java.io.IOException;
import java.net.*;
import java.util.Random;

/* You can add/change/delete class attributes if you think it would be
 * appropriate.
 *
 * You can also add helper methods and change the implementation of those
 * provided if you think it would be appropriate, as long as you DO NOT
 * CHANGE the interface.
 */

public class Sensor implements ISensor {

  private float measurement;

  private static final int max_measure = 50;
  private static final int min_measure = 10;

  private DatagramSocket s;
  private byte[] buffer;

  /* Note: Could you discuss in one line of comment what you think can be
   * an appropriate size for buffsize?
   * (Which is used to init DatagramPacket?)
   */
  private static final int buffsize = 2048;

  public Sensor(String address, int port, int totMsg) throws SocketException, UnknownHostException {

    /* TODO: Build Sensor Object */
    buffer = new byte[buffsize];
    try {
      InetAddress addr = InetAddress.getByName(address);
      s = new DatagramSocket(port, addr);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String addressCleaner(String address) {
    return address.substring(1);
  }

  @Override
  public void run(int N) throws InterruptedException, IOException {
    /* TODO: Send N measurements */
    for (int i = 0; i < N; i++) {
      measurement = this.getMeasurement();
      MessageInfo messageInfo = new MessageInfo(N, i, measurement);
      sendMessage(String.valueOf(s.getLocalAddress()), s.getLocalPort(), messageInfo);
      System.out.println(
          "[Sensor] Sending message " + i + " out of " + N + ". Measure = " + measurement);
    }
  }

  public static void main(String[] args) throws InterruptedException, IOException {
    if (args.length < 3) {
      System.out.println("Usage: ./sensor.sh field_unit_address port number_of_measures");
      return;
    }

    /* Parse input arguments */
    String address = args[0];
    int port = Integer.parseInt(args[1]);
    int totMsg = Integer.parseInt(args[2]);

    /* TODO: Call constructor of sensor to build Sensor object*/
    Sensor sensor = new Sensor(address, port, totMsg);

    /* TODO: Use Run to send the messages */
    sensor.run(totMsg);
  }

  @Override
  public void sendMessage(String address, int port, MessageInfo msg) throws IOException {
    String toSend = msg.toString();

    /* TODO: Build destination address object */
    InetAddress addr = InetAddress.getByName(addressCleaner(address));

    /* TODO: Build datagram packet to send */
    byte[] sendData = toSend.getBytes();
    DatagramPacket p = new DatagramPacket(sendData, sendData.length, addr, port);

    /* TODO: Send packet */
    s.send(p);
  }

  @Override
  public float getMeasurement() {
    Random r = new Random();
    measurement = r.nextFloat() * (max_measure - min_measure) + min_measure;

    return measurement;
  }
}
