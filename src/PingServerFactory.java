import rmi.Stub;

import java.net.InetSocketAddress;

/**
 * Created by anthonyaltieri on 2/14/17.
 */
public class PingServerFactory extends Stub
{
    PingPongServer makePingServer(InetSocketAddress socketAddress)
        throws Throwable
    {
        return create(PingPongServer.class, socketAddress);

    }
}
