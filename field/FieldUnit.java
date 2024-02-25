package field;

/*
 * Updated on Feb 2023
 */
import static java.lang.Thread.sleep;

import centralserver.CentralServer;
import centralserver.ICentralServer;
import common.MessageInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* You can add/change/delete class attributes if you think it would be
 * appropriate.
 *
 * You can also add helper methods and change the implementation of those
 * provided if you think it would be appropriate, as long as you DO NOT
 * CHANGE the interface.
 */

public class FieldUnit implements IFieldUnit, Remote {
  private ICentralServer central_server;

  /* Note: Could you discuss in one line of comment what do you think can be
   * an appropriate size for buffsize?
   * (Which is used to init DatagramPacket?)
   */

  private static final int buffsize = 2048;
  private int timeout = 50000;
  private List<Float> receivedMessages;
  private final List<Float> movingAverage;
  private int totalMessage;
  private int totalMissing;

  private ICentralServer centralServer;

  public FieldUnit() {
    /* TODO: Initialise data structures */
    movingAverage = new ArrayList<>();
    receivedMessages = null;
  }

  @Override
  public void addMessage(MessageInfo msg) {
    /* TODO: Save received message in receivedMessages */
    receivedMessages.add(msg.getMessage());
  }

  @Override
  public void sMovingAverage(int k) {
    /* TODO: Compute SMA and store values in a class attribute */
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

    /* TODO: Create UDP socket and bind to local port 'port' */
    DatagramSocket s = new DatagramSocket(port);
    DatagramPacket p;
    byte[] receive = new byte[buffsize];

    boolean listen = true;

    System.out.println("[Field Unit] Listening on port: " + port);

    s.setSoTimeout(timeout);

    while (listen) {
      MessageInfo messageInfo = null;
      try {
        /* TODO: Receive until all messages in the transmission (msgTot) have been received or until
        there is nothing more to be received */
        p = new DatagramPacket(receive, receive.length);

        /* TODO: If this is the first message, initialise the receive data structure before storing it. */
        if (receivedMessages == null) {
          receivedMessages = new ArrayList<>();
        }
        s.receive(p);

        /* TODO: Store the message */
        String msg = new String(p.getData()).replaceAll("[\n\r]", "");
        messageInfo = new MessageInfo(msg);
        addMessage(messageInfo);
        totalMessage = messageInfo.getTotalMessages();
        System.out.println(
            "[Field Unit] Message "
                + messageInfo.getMessageNum()
                + " out of "
                + totalMessage
                + " received. Value = "
                + messageInfo.getMessage());

        /* TODO: Keep listening UNTIL done with receiving  */
        if (messageInfo.getMessageNum() == messageInfo.getTotalMessages()) {
          listen = false;
        }
      } catch (NumberFormatException e) {
        System.err.println("NumberFormatException: " + e.getMessage());
        e.printStackTrace();
      } catch (SocketTimeoutException e) {
        System.out.println("Timer");
        listen = false;
      }
    }

    /* TODO: Close socket  */
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
    /* TODO: Attempt to send messages the specified number of times */
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
//      try {
//        sleep(100);
//      } catch (InterruptedException e) {
//        throw new RuntimeException(e);
//      }
    }
    movingAverage.clear();
  }

  @Override
  public void printStats() {
    /* TODO: Find out how many messages were missing */
    totalMissing = totalMessage - receivedMessages.size();

    /* TODO: Print stats (i.e. how many message missing? do we know their sequence number? etc.) */
    System.out.println("Total Missing Message = " + totalMissing + " out of " + totalMessage);

    /* TODO: Now re-initialise data structures for next time */
    receivedMessages.clear();
  }
}
