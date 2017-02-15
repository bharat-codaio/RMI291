import rmi.RMIException;

/**
 * Created by anthonyaltieri on 2/14/17.
 */
public interface PingPongServer
{
    String ping(int idNumber) throws RMIException;
}
