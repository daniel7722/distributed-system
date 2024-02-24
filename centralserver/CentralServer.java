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

/* You can add/change/delete class attributes if you think it would be
 * appropriate.
 *
 * You can also add helper methods and change the implementation of those
 * provided if you think it would be appropriate, as long as you DO NOT
 * CHANGE the provided interface.
 */

/* TODO extend appropriate classes and implement the appropriate interfaces */
public class CentralServer extends UnicastRemoteObject implements ICentralServer {

  private List<MessageInfo> receivedMessages;
  private int counter;

  protected CentralServer() throws RemoteException {
    super();

    /* TODO: Initialise Array receivedMessages */
    receivedMessages = new ArrayList<>();
  }

  public void main(String[] args) throws RemoteException {
    ICentralServer cs = new CentralServer();
    ICentralServer stub = (ICentralServer) UnicastRemoteObject.exportObject(cs, 0);
    /* TODO: Create (or Locate) Registry */
    Registry r = LocateRegistry.getRegistry();

    /* TODO: Bind to Registry */
    r.rebind("CentralService", stub);

    System.out.println("Central Server is running...");
  }

  @Override
  public void receiveMsg(MessageInfo msg) {
    System.out.println(
        STR."[Central Server] Received message \{msg.getMessageNum()} out of \{msg.getTotalMessages()}. Measure = \{msg.getMessage()}");

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
