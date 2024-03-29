package field;
/*
 * Updated on Feb 2023
 */
import common.MessageInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.RemoteException;

public interface IFieldUnit {
    /* Save message into local data structure */
    public void addMessage (MessageInfo m);

    /* Compute the k-points moving averages of all messages */
    public void sMovingAverage (int k);

  /* Listen on UDP port UNTIL there is no more to receive */
  public void receiveMeasures(int port, int timeout) throws Exception;

    /* Set up RMI client */
    public void initRMI (String address) throws RemoteException, MalformedURLException;

    /* In this function we call CentralServer.receiveMsg() to send the message to the Central Server via RMI */
    public void sendAverages ();

    /* Print Stats */
    public void printStats ();
}
