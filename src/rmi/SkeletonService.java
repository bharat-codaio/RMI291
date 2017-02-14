package rmi;

import com.sun.corba.se.spi.activation.Server;
import javafx.util.Pair;
import rmi.RMIException;
import rmi.Shuttle;
import rmi.Stub;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class SkeletonService<T> {
    public SkeletonService() {}



    void handleMethodCall(Lock lock, Condition methodInvoking, int currentlyInvoking, Class<T> c,
                          T server, Socket socket, ObjectOutputStream oos, Shuttle shuttle)
        throws IllegalAccessException, IOException, InstantiationException,
        RMIException, InvocationTargetException, InterruptedException {
        try
        {
            // Get the method the Client wants to call
            Method method = findMethod(shuttle, c);
            System.err.println("method found - " + method.toString());
            // If the method was not found throw a RMIException
            if (shuttle == null || method == null)
            {
                RMIException rmiException = new RMIException(shuttle == null
                    ? "shuttle == null" : "method == null");
                Return ret = new Return(null, null, new InvocationTargetException(rmiException),
                    null);
                oos.writeObject(ret);
                socket.close();
                throw rmiException;
            }
            if (shuttle.args == null)
            {
                Object returnValue = method.invoke(server, new Object[0]);
                Return ret = new Return(method.getGenericReturnType(), returnValue, null, null);
                oos.writeObject(ret);
                socket.close();
                return;
            }
            else
            {
                Object[] arguments = new Object[shuttle.args.length];
                if (arguments != null)
                {
                    for (int i = 0 ; i < shuttle.args.length ; i++)
                    {
                        Object arg = shuttle.args[i].getValue();
                        System.err.println("type - " + shuttle.args[i].getKey() + " | arg - " + arg.toString() + "");

                        arguments[i] = arg;
                    }
                }
                Object returnValue = method.invoke(server, arguments);
                System.err.println("returnValue: " + ((returnValue != null) ? returnValue.toString() : null));
                Return ret = new Return(method.getGenericReturnType(), returnValue, null, null);
                oos.writeObject(ret);
                socket.close();
            }
        }
        catch (IOException e)
        {
            System.err.println("IOExceptino");
            System.err.println(e.getMessage().toString());
            System.err.println(e.getCause().toString());
            RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
            // TODO I CHANGED THIS from third praram  ITE constructor(e)
            oos.writeObject(new Return(null, null, new InvocationTargetException(e), null));
            socket.close();
            throw rmiException;
        }
        catch (RMIException e)
        {
            oos.writeObject(new Return(null, null, null, e));
            socket.close();
        }
        catch (Exception e)
        {
            System.err.println("@@@@@@@@");
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            oos.writeObject(new Return(null, null, new InvocationTargetException(e), null));
            socket.close();
            throw e;
        }
    }

    Method findMethod(Shuttle shuttle, Class<T> c)
        throws RMIException
    {
        System.err.println("findMethod()");
        Method foundMethod = null;
        try
        {
            foundMethod = c.getMethod(shuttle.name, shuttle.paramTypes);
            System.err.println("hihihihihhi");
        }
        catch (Exception e)
        {
            RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
            throw rmiException;
        }
        return foundMethod;
    }


    public static synchronized int findAvailablePort()
    {
        int STARTING_PORT = 49152;
        int ENDING_PORT = 65535;
        List<Integer> possiblePorts = new LinkedList<Integer>();
        for (int i = STARTING_PORT ; i <= ENDING_PORT ; i++) possiblePorts.add(i);
        for (Integer port : possiblePorts)
        {
            try
            {
                ServerSocket serverSocket = new ServerSocket(port);
                if (serverSocket != null)
                {
                    serverSocket.close();
                    return port;
                }
                else
                {
                    continue;
                }
            } catch (IOException e) {
                continue;
            }
        }
        return -1;
    }

}