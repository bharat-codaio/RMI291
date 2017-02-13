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
            if (shuttle == null)
            {
                throw new RMIException("shuttle == null");
            }
            // Get the method the Client wants to call
            Method method = findMethod(shuttle, c);
            // If the method was not found throw a RMIException
            if (method == null)
            {
                throw new RMIException("method is null");
            }
            currentlyInvoking++;
            if (shuttle.args == null)
            {
                Object returnValue = method.invoke(server, new Object[0]);
                Return ret = new Return(method.getGenericReturnType(), returnValue, null);
                oos.writeObject(ret);
                oos.flush();
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
                System.err.println("aaaaa");
                Return ret = new Return(method.getGenericReturnType(), returnValue, null);
                oos.writeObject(ret);

            }
        }
        catch (IOException e)
        {
            RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
            oos.writeObject(new Return(null, null, new InvocationTargetException(e)));
            oos.flush();
            throw rmiException;
        }
        catch (Exception e)
        {
            System.err.println("@@@@@@@@");
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            oos.writeObject(new Return(null, null, new InvocationTargetException(e)));
            oos.flush();
            throw e;
        }
    }

    Method findMethod(Shuttle shuttle, Class<T> c)
    {
        Method[] classMethods = c.getDeclaredMethods();
        for (Method method : classMethods)
        {
            if (method.hashCode() == shuttle.hashCode)
            {
                return method;
            }
        }
        return null;
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