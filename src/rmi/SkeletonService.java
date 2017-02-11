package rmi;

import javafx.util.Pair;
import rmi.RMIException;
import rmi.Shuttle;
import rmi.Stub;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

public class SkeletonService<T> {
    public SkeletonService() {}



    void handleMethodCall(Lock lock, Class<T> c, T server,
                          Hashtable<Proxy, Object> mm, Socket socket,
                          ObjectOutputStream oos, Shuttle shuttle)
            throws IllegalAccessException, IOException, InstantiationException,
            RMIException, InvocationTargetException
    {
        try
        {
            lock.lock();
            if (shuttle == null)
            {
                throw new RMIException("shuttle == null");
            }
            // Get the method the Client wants to call
            Method method = findMethod(shuttle, c);
            // If the method was not found throw a RMIException
            if (method == null)
            {
                throw new RMIException("method == null after trying to find " +
                        "the method in the Class");
            }
            Object[] arguments = new Object[shuttle.args.length];
            for (int i = 0 ; i < shuttle.args.length ; i++)
            {
                Object arg = shuttle.args[i].getValue();
                arguments[i] = arg;
            }
            Object returnValue = method.invoke(server, arguments);
            Return ret = new Return(method.getGenericReturnType(), returnValue);
            oos.writeObject(ret);
            oos.flush();
            oos.close();
            socket.close();
            lock.unlock();
        }
        catch (Exception e)
        {
            lock.unlock();
            if (!(e instanceof IOException))
            {
                Exception toWrite = e instanceof RMIException
                        ? e
                        : new RMIException(e.getCause());
                oos.writeObject(toWrite);
                oos.flush();
                oos.close();
                socket.close();
            }
            throw e;
        }

    }

    Method findMethod(Shuttle shuttle, Class<T> c)
    {
        // Get the method name
        String methodName = shuttle.methodName;

        // Get the arguments
        Pair<Type, Object>[] args = shuttle.args;

        // Get the methods for the class
        Method[] classMethods = c.getDeclaredMethods();

        // Iterate through all of the methods for the class
        for (Method m : classMethods)
        {
            String mName = m.getName();
            // See if the desired method has the same name as the current method
            if (Objects.equals(methodName, mName))
            {
                // Get the parameter types for the current method
                Type[] pTypes = m.getGenericParameterTypes();

                // If both parameter lists have the same length they might be
                // a match
                if (pTypes.length == args.length)
                {
                    for (int i = 0 ; i < args.length ; i++)
                    {
                        // Compare the current method's params in-order to the
                        // desired method's params
                        Type pType = pTypes[i];
                        Type paramType = args[i].getKey();
                        // If they are not the same at any point, this is not
                        // the desired method
                        if (!pType.equals(paramType)) break;
                        // If we have evaluated every param and we haven't
                        // used break yet, this is the method we want
                        if (i == args.length - 1) return m;
                    }
                }
            }
        }
        // Return null if we haven't found anything, this might be an error
        return null;
    }

    // TODO: implement
    public Object[] getArgs(Stub stub)
    {
        return new Object[5];
    }

    // TODO: implement
    public String getStubId(Stub stub)
    {
        return "to implement";
    }


}