package rmi;

import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by bharatbatra on 2/5/17.
 */
public class StubInvocationHandler implements InvocationHandler
{
    InetSocketAddress address;

    public StubInvocationHandler(InetSocketAddress address)
    {
        this.address = address;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Exception
    {
        try {
            Socket socket = createSocketFromAddress(this.address);

            Object result = null;

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // Flush it
            oos.flush();



            Type[] types = m.getGenericParameterTypes();


            Pair<Type, Object>[] params = new Pair[args.length];

            for(int i = 0; i < args.length; i++)
            {
                Type type = types[i]; //TODO: Check if this type works
                params[i] = new Pair<Type, Object>(type, args[i]);
            }

            Shuttle shuttle = new Shuttle(m.toString(), m.getReturnType(), params);

            // Create ObjectInputStream from socket
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(shuttle);

            while(!socket.isClosed())
            {
                if(ois.available() > 0)
                {
                    result = ois.readObject();
                }
            }
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RMIException("Error Getting Return From Server");
        }
    }

    private static Socket createSocketFromAddress(InetSocketAddress address) throws IOException
    {
        InetAddress adr = address.getAddress();
        int port = address.getPort();
        return new Socket(adr, port);
    }



    public String toString()
    {
        return "";
    }

    public boolean equals(Object obj)
    {
        //TODO:
        //Check if objects implement same interface
        Class<?> otherInterfaces[] =  obj.getClass().getInterfaces();
        Class<?> myInterfactes[] =  this.getClass().getInterfaces();

        boolean sameInterface = false;

        for(Class m : myInterfactes)
        {
            for (Class o : otherInterfaces)
            {
                if(o.isAssignableFrom(m) || m.isAssignableFrom(o))
                {
                    sameInterface = true;
                }
            }
        }
        //TODO:
        //Check if both objects possess the same skeleton
        //TODO: This can be done using the socket address but is this a valid method? Or do we need to get skeleton

        boolean sameSkeleton = false;
        if(this.address == ( (StubInvocationHandler) obj ).address)
        {
            sameSkeleton = true;
        }

        return (sameInterface && sameSkeleton);
    }

    public int hashCode()
    {
        return 0;
    }
}
