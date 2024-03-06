package centralserver;

import common.*;

/*
 * Updated on Feb 2023
 */
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/* extend appropriate classes and implement the appropriate interfaces */
public class CentralServer extends UnicastRemoteObject implements ICentralServer {

  private List<MessageInfo> receivedMessages;
  private int totalMessages = -1;
  private int counter;
  private long startTime;

  protected CentralServer() throws RemoteException {
    super();

    /* Initialise Array receivedMessages */
    receivedMessages = new ArrayList<>();
  }

  public static void main(String[] args) throws RemoteException {
    ICentralServer cs = new CentralServer();

    /* Create (or Locate) Registry */
    Registry r = LocateRegistry.createRegistry(1099);
    System.out.println("RMI Registry started on port 1099.");

    /* Bind to Registry */
    r.rebind("CentralService", cs);

    System.out.println("Central Server is running...");
  }

  @Override
  public void receiveMsg(MessageInfo msg) {
    totalMessages = msg.getTotalMessages();

    System.out.println(
        "[Central Server] Received message "
            + msg.getMessageNum()
            + " out of "
            + msg.getTotalMessages()
            + ". Measure = "
            + msg.getMessage());

    /* If this is the first message, reset counter and initialise data structure. */
    if (msg.getMessageNum() == 1) {
      counter = 0;
      receivedMessages = new ArrayList<>();
      startTime = System.nanoTime();
    }
    counter++;

    /* Save current message */
    receivedMessages.add(msg);

    /* If done with receiveing prints stats. */
    printStats();
  }

  public void printStats() {
    /* Find out how many messages were missing */
    int totalMissing = totalMessages - counter;

    /* Print stats (i.e. how many message missing?
     * do we know their sequence number? etc.) */
    System.out.println("Total missing messages: " + totalMissing + " out of " + totalMessages);
    System.out.println(
        "Time taken to receive these packets is " + (System.nanoTime() - startTime) / 1000000
            + "ms");

    /* TODO: Now re-initialise data structures for next time */
    receivedMessages.clear();
  }
}
