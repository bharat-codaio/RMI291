import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by anthonyaltieri on 2/14/17.
 */
public class ActualServer
{
    public static void main(String[] args)
        throws RMIException, UnknownHostException
    {
        byte[] address = {0,0,0,0};
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByAddress(address),
            8080);
        PingServer pingServer = new PingServer();
        Skeleton<PingPongServer> skeleton = new Skeleton(PingPongServer.class, pingServer,
            socketAddress);
        skeleton.start();
    }



    private static byte[] ipStringToByteArray(String ip)
    {
        String[] split = ip.split(".");
        byte[] result = new byte[4];
        for (int i = 0 ; i < split.length ; i++)
            result[i] = Byte.parseByte(split[i]);
        return result;
    }
}
