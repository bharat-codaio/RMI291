package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by bharatbatra on 2/5/17.
 */
public class RemoteInvocationHandler<T> implements InvocationHandler, Serializable
{
    InetSocketAddress socketAddress;
    Class<T> c;
    Skeleton<T> skeleton;

    public RemoteInvocationHandler(Class<T> c, Skeleton<T> skeleton, InetSocketAddress socketAddress)
    {
        if (c == null) throw new NullPointerException("class is null");
        if (socketAddress == null) throw new NullPointerException("InetSocketAddress is null");
        this.c = c;
        this.skeleton = skeleton;
        this.socketAddress = socketAddress;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
        throws Throwable {
        System.out.println("method: " + m.toString());

        String standardMethodResult = isStandardMethod(m);
        if (standardMethodResult != null)
        {
            switch (standardMethodResult)
            {
                case "equals":
                    return this.equals(args[0] == null ? null : args[0]);
                case "toString": return this.toString();
                case "hashCode": return this.hashCode();
            }
        }
        Socket socket = null;
        try
        {
            socket = createSocketFromAddress(this.socketAddress);
        }
        catch (IOException e)
        {
            throw new RMIException("could not create socket from address");
        }

        Object result = null;


        Type[] types = m.getGenericParameterTypes();

        Pair[] params = (args != null && args.length != 0)
            ? new Pair[args.length]
            : null;

        if (params != null)
        {
            for (int i = 0 ; i < args.length ; i++)
            {
                Type type = types[i];
                params[i] = new Pair<Type, Object>(type, args[i]);
            }
        }

        Shuttle shuttle = new Shuttle(m, params);

        try {
            // Create ObjectInputStream from socket
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(shuttle);
            oos.flush();

            result = ois.readObject();
            Return ret = (Return) result;
            if (ret.invocationTargetException != null)
            {
                throw ret.invocationTargetException;
            }
            if (ret.rmiException != null)
            {
                throw ret.rmiException;
            }
            socket.close();
            return ((Return) result).value;
        }
        catch (RMIException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw new RMIException("stream problem", e.getCause());
        }
        catch (ClassNotFoundException e)
        {
            throw new RMIException("class not found", e.getCause());
        }
        catch (InvocationTargetException e)
        {
            throw e.getTargetException() != null
                ? e.getTargetException().getCause()
                : e;
        }
    }

    private static Socket createSocketFromAddress(InetSocketAddress socketAddress)
        throws IOException
    {
        InetAddress adr = socketAddress.getAddress();
        int port = socketAddress.getPort();
        return new Socket(adr, port);
    }



    public String toString()
    {
        String remoteInterfaceName = c.getName();
        String address = socketAddress.getAddress().toString();
        String port = "" + socketAddress.getPort();
        return remoteInterfaceName + " @ " + address + ":" + port;
    }

    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        try
        {
            RemoteInvocationHandler rih = (RemoteInvocationHandler) ROR.getInvocationHandler(obj);
            if (c != rih.c) return false;
            return this.socketAddress.equals(rih.socketAddress);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    private String isStandardMethod(Method method)
    {
        String name = method.getName();
        switch (name)
        {
            case "equals":
                return isEquals(method) ? "equals" : null;
            case "toString":
                return isToString(method) ? "toString" : null;
            case "hashCode":
                return isHashCode(method) ? "hashCode" : null;
            default:
                return null;
        }
    }

    private boolean isEquals(Method method)
    {
        // Parameter number
        if (method.getParameterCount() != 1) return false;
        // Return Type
        Method objectEquals = getObjectMethod("equals");
        if (!isSameReturnType(method, objectEquals)) return false;
        // Parameter Type
        Type paramType = method.getGenericParameterTypes()[0];
        if (paramType != Object.class) return false;
        return true;
    }

    private boolean isToString(Method method)
    {
        // Parameter number
        if (method.getParameterCount() != 0) return false;
        // Return Type
        Method objectToString = getObjectMethod("toString");
        if (!isSameReturnType(method, objectToString)) return false;
        return true;
    }

    private boolean isHashCode(Method method)
    {
        // Parameter number
        if (method.getParameterCount() != 0) return false;
        // Return Type
        Method objectHashCode = getObjectMethod("hashCode");
        if (!isSameReturnType(method, objectHashCode)) return false;
        return true;
    }

    private boolean isSameReturnType(Method method1, Method method2)
    {
        return (method1.getGenericReturnType().getTypeName()
            .equals(method2.getGenericReturnType().getTypeName()));
    }

    private Method getObjectMethod(String name)
    {
        Method methods[] = Object.class.getMethods();
        for (Method m: methods)
        {
            if (m.getName().equals(name)) return m;
        }
        return null;
    };

    public int hashCode()
    {
        return this.toString().hashCode();
    }
}
