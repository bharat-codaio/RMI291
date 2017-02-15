import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Created by anthonyaltieri on 2/14/17.
 */


class PingPongClient
{
    public static void main(String[] args) throws Throwable
    {
        InetSocketAddress socketAddress = getSocketAddress(args);
        System.out.println("socketAddress : " + socketAddress.toString());
        PingServerFactory pingServerFactory = new PingServerFactory();
        PingPongServer pingPongServer = pingServerFactory.makePingServer(socketAddress);
        int numFail = 0;
        for(int i = 0; i < 4; i ++)
        {
            try
            {
                String result = pingPongServer.ping(i);
                System.out.println("result: " + result);
                if(!result.equals("Pong" + i))
                {
                    numFail++;
                }
            }
            catch (Throwable throwable)
            {
                System.out.println("Throwable: " + throwable.toString());
                numFail++;
            }
        }
        System.out.println("4 Tests Completed, " + (numFail) + " Tests Failed");
    }


    private static InetSocketAddress getSocketAddress(String[] args)
        throws UnknownHostException
    {
        String addressString = args[0];
        System.out.println("connecting to ip: " + addressString);
        byte[] addressBytes = ipStringToByteArray(addressString);
        InetAddress address = InetAddress.getByAddress(addressBytes);
        int PORT = 8080;
        return new InetSocketAddress(address, PORT);
    }


    private static byte[] ipStringToByteArray(String ip)
    {
        System.out.println("ip: " + ip);
        String[] split = ip.split(Pattern.quote("."));
        System.out.println("split.length " + split.length );
        byte[] result = new byte[4];
        for (int i = 0 ; i < split.length ; i++)
        {
            int byteInt = Integer.parseInt(split[i]);
            result[i] = (byte) byteInt;
        }
        return result;
    }
}



