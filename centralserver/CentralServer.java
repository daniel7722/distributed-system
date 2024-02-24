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

/* TODO extend appropriate classes and implement the appropriate interfaces */
public class CentralServer extends UnicastRemoteObject implements ICentralServer {

  private List<MessageInfo> receivedMessages;
  private int counter;

  protected CentralServer() throws RemoteException {
    super();

    /* TODO: Initialise Array receivedMessages */
    receivedMessages = new ArrayList<>();
  }

  public static void main(String[] args) throws RemoteException {
    ICentralServer cs = new CentralServer();

    /* TODO: Create (or Locate) Registry */
    Registry r = LocateRegistry.createRegistry(1099);
    System.out.println("RMI Registry started on port 1099.");

    /* TODO: Bind to Registry */
    r.rebind("CentralService", cs);

    System.out.println("Central Server is running...");
  }

  @Override
  public void receiveMsg(MessageInfo msg) {
    System.out.println("[Central Server] Received message" + msg.getMessageNum() + "out of" + msg.getTotalMessages() + ". Measure = " + "msg.getMessage()");

    /* TODO: If this is the first message, reset counter and initialise data structure. */
    if (msg.getMessageNum() == 0) {
      counter = 0;
      receivedMessages = new ArrayList<>();
    }
    counter++;

    /* TODO: Save current message */
    receivedMessages.add(msg);

    /* TODO: If done with receiveing prints stats. */
    printStats();

  }

  public void printStats() {
    /* TODO: Find out how many messages were missing */

    /* TODO: Print stats (i.e. how many message missing?
     * do we know their sequence number? etc.) */

    /* TODO: Now re-initialise data structures for next time */

  }
}
