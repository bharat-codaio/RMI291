import rmi.RMIException;

/**
 * Created by anthonyaltieri on 2/14/17.
 */
public class PingServer implements PingPongServer
{
    @Override
    public String ping(int idNumber) throws RMIException {
        return "Pong" + idNumber;
    }
}
