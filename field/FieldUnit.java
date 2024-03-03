package field;

/*
 * Updated on Feb 2023
 */
import centralserver.ICentralServer;
import common.MessageInfo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class FieldUnit implements IFieldUnit, Remote {
  private ICentralServer central_server;

  // The bufferSize chosen here as 24 because each packet has maximum 21 bytes long.
  // It is enough to set the bufferSize slightly higher than the maximum to ensure
  // efficient memory usage.

  private static final int buffSize = 2048;
  private int timeout = 50000;
  private List<Float> receivedMessages;
  private final List<Float> movingAverage;
  private int totalMessage;
  private int totalMissing;

  private ICentralServer centralServer;

  public FieldUnit() {
    /* Initialise data structures */
    movingAverage = new ArrayList<>();
    receivedMessages = null;
  }

  @Override
  public void addMessage(MessageInfo msg) {
    /* Save received message in receivedMessages */
    receivedMessages.add(msg.getMessage());
  }

  @Override
  public void sMovingAverage(int k) {
    /* Compute SMA and store values in a class attribute */
    System.out.println("Computing SMAs");
    float average = 0;
    int rear = 0;
    // i < k
    for (; rear < k - 1 && rear < receivedMessages.size(); rear++) {
      movingAverage.add(receivedMessages.get(rear));
      average += receivedMessages.get(rear) / k;
    }

    // i >= k
    for (int front = 0; rear < receivedMessages.size(); rear++, front++) {
      average += receivedMessages.get(rear) / k;
      movingAverage.add(average);
      average -= receivedMessages.get(front) / k;
    }
  }

  @Override
  public void receiveMeasures(int port, int timeout) throws Exception {

    this.timeout = timeout;

    /* Create UDP socket and bind to local port 'port' */
    DatagramSocket s = new DatagramSocket(port);
    byte[] receive = new byte[buffSize];
    boolean listen = true;
    s.setSoTimeout(timeout);
    long startTime = 0;
    System.out.println("[Field Unit] Listening on port: " + port);

    while (listen) {
      try {
        /* Receive until all messages in the transmission (msgTot) have been received or until
        there is nothing more to be received */
        DatagramPacket p = new DatagramPacket(receive, receive.length);
        s.receive(p);

        /* If this is the first message, initialise the received data structure before storing it. */
        if (receivedMessages == null) {
          receivedMessages = new ArrayList<>();
          startTime = System.nanoTime();
        }

        /* Store the message */
        String msg = new String(p.getData()).replaceAll("[\n\r]", "");
        MessageInfo messageInfo = new MessageInfo(msg);
        addMessage(messageInfo);
        totalMessage = messageInfo.getTotalMessages();
        System.out.println(
            "[Field Unit] Message "
                + messageInfo.getMessageNum()
                + " out of "
                + totalMessage
                + " received. Value = "
                + messageInfo.getMessage());

        /* Keep listening UNTIL done with receiving  */
        if (messageInfo.getMessageNum() == messageInfo.getTotalMessages()) {
          long estimatedTime = System.nanoTime() - startTime;
          System.out.println(
              "Time taken to receive these packets is " + estimatedTime / 1000000 + "ms");
          listen = false;
        }
      } catch (NumberFormatException e) {
        System.err.println("NumberFormatException: " + e.getMessage());
        e.printStackTrace();
      } catch (SocketTimeoutException e) {
        System.out.println("Timer");
        listen = false;
      } finally {
        s.close();
      }
    }

    /* Close socket  */
    s.close();
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("Usage: ./fieldunit.sh <UDP rcv port> <RMI server HostName/IPAddress>");
      return;
    }

    /* TODO: Parse arguments */
    int port = Integer.parseInt(args[0]);
    String address = args[1];

    /* TODO: Construct Field Unit Object */
    FieldUnit fieldUnit = new FieldUnit();

    /* TODO: Call initRMI on the Field Unit Object */
    fieldUnit.initRMI(address);

    /* TODO: Wait for incoming transmission */
    while (true) {
      fieldUnit.receiveMeasures(port, fieldUnit.timeout);

      /* TODO: Compute Averages - call sMovingAverage() on Field Unit object */
      fieldUnit.sMovingAverage(7);

      /* TODO: Compute and print stats */
      fieldUnit.printStats();

      /* TODO: Send data to the Central Serve via RMI and
       *        wait for incoming transmission again
       */
      fieldUnit.sendAverages();
    }
  }

  @Override
  public void initRMI(String address) {

    /* TODO: Initialise Security Manager (If JAVA version earlier than version 17) */
    /* TODO: Bind to RMIServer */
    try {
      centralServer = (ICentralServer) Naming.lookup("rmi://" + address + "/CentralService");
      System.out.println("FieldUnit is ready to listen on " + address);
    } catch (RemoteException e) {
      System.err.println("RemoteException:" + e.getMessage());
      e.printStackTrace();
    } catch (MalformedURLException e) {
      System.err.println("MalformedURLException: " + e.getMessage());
      e.printStackTrace();
    } catch (NotBoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendAverages() {
    /* Attempt to send messages the specified number of times */
    System.out.println("Sending SMAs to RMI");
    for (float f : movingAverage) {
      MessageInfo messageInfo =
          new MessageInfo(movingAverage.size(), movingAverage.indexOf(f) + 1, f);
      try {
        centralServer.receiveMsg(messageInfo);
      } catch (RemoteException e) {
        System.err.println("RemoteException: " + e.getMessage());
        e.printStackTrace();
      }
    }
    movingAverage.clear();
  }

  @Override
  public void printStats() {
    /* Find out how many messages were missing */
    totalMissing = totalMessage - receivedMessages.size();

    /* Print stats (i.e. how many message missing? do we know their sequence number? etc.) */
    System.out.println("Total Missing Message = " + totalMissing + " out of " + totalMessage);

    /* Now re-initialise data structures for next time */
    receivedMessages.clear();
  }
}
