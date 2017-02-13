package rmi;


import java.io.Serializable;
import java.lang.reflect.*;
import java.net.*;

/** RMI stub factory.

 <p>
 RMI stubs hide network communication with the remote server and provide a
 simple object-like interface to their users. This class provides methods for
 creating stub objects dynamically, when given pre-defined interfaces.

 <p>
 The network address of the remote server is set when a stub is created, and
 may not be modified afterwards. Two stubs are equal if they implement the
 same interface and carry the same remote server address - and would
 therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub implements Serializable
{
    /** Creates a stub, given a skeleton with an assigned adress.

     <p>
     The stub is assigned the address of the skeleton. The skeleton must
     either have been created with a fixed address, or else it must have
     already been started.

     <p>
     This method should be used when the stub is created together with the
     skeleton. The stub may then be transmitted over the network to enable
     communication with the skeleton.

     @param c A <code>Class</code> object representing the interface
     implemented by the remote object.
     @param skeleton The skeleton whose network address is to be used.
     @return The stub created.
     @throws IllegalStateException If the skeleton has not been assigned an
     address by the user and has not yet been
     started.
     @throws UnknownHostException When the skeleton address is a wildcard and
     a port is assigned, but no address can be
     found for the local host.
     @throws NullPointerException If any argument is <code>null</code>.
     @throws Error If <code>c</code> does not represent a remote interface
     - an interface in which each method is marked as throwing
     <code>RMIException</code>, or if an object implementing
     this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
            throws Throwable
    {
        try
        {
            if (c == null) throw new NullPointerException("c is null");
            if (!Validation.isRemoteInterface(c))
                throw new Error("c is not a remote interface");
            if (skeleton == null) throw new NullPointerException("skeleton is null");
            InetAddress address = skeleton.getAddress();
            String ip = address.getHostAddress();
            if (ip.equals("0.0.0.0") && skeleton.getPort() != -1) {
                InetAddress localhostAddress = InetAddress.getLocalHost();
                if (localhostAddress == null)
                {
                    throw new UnknownHostException("skeleton has wildcard");
                }
            }
            if (skeleton.getAddress() == null)
                throw new IllegalStateException("skeleton doesn't address");
            if (skeleton.getPort() == -1)
                throw new IllegalStateException("skeleton doesn't have port");
            return Stub.performCreate(c, skeleton, new InetSocketAddress(address,
                skeleton.getPort()));
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
     which overrides the skeleton's hostname.

     <p>
     The stub is assigned the port of the skeleton and the given hostname.
     The skeleton must either have been started with a fixed port, or else
     it must have been started to receive a system-assigned port, for this
     method to succeed.

     <p>
     This method should be used when the stub is created together with the
     skeleton, but firewalls or private networks prevent the system from
     automatically assigning a valid externally-routable address to the
     skeleton. In this case, the creator of the stub has the option of
     obtaining an externally-routable address by other means, and specifying
     this hostname to this method.

     @param c A <code>Class</code> object representing the interface
     implemented by the remote object.
     @param skeleton The skeleton whose port is to be used.
     @param hostname The hostname with which the stub will be created.
     @return The stub created.
     @throws IllegalStateException If the skeleton has not been assigned a
     port.
     @throws NullPointerException If any argument is <code>null</code>.
     @throws Error If <code>c</code> does not represent a remote interface
     - an interface in which each method is marked as throwing
     <code>RMIException</code>, or if an object implementing
     this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton, String hostname)
        throws Throwable
    {
        try {
            if (c == null) throw new NullPointerException("c is null");
            if (!Validation.isRemoteInterface(c))
                throw new Error("c is not a remote interface");
            if (skeleton == null) throw new NullPointerException("skeleton is null");
            if (hostname == null) throw new NullPointerException("hostname is null");

            //createSocket
            InetAddress address = InetAddress.getByName(hostname);
            int port = skeleton.getPort();
            if (port == -1) throw new IllegalStateException("skeleton has no port");
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);

            return Stub.performCreate(c, skeleton, socketAddress);
        }
        catch (Throwable t)
        {
            throw  t;
        }
    }

    /** Creates a stub, given the address of a remote server.

     <p>
     This method should be used primarily when bootstrapping RMI. In this
     case, the server is already running on a remote host but there is
     not necessarily a direct way to obtain an associated stub.

     @param c A <code>Class</code> object representing the interface
     implemented by the remote object.
     @param address The network address of the remote skeleton.
     @return The stub created.
     @throws NullPointerException If any argument is <code>null</code>.
     @throws Error If <code>c</code> does not represent a remote interface
     - an interface in which each method is marked as throwing
     <code>RMIException</code>, or if an object implementing
     this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address) throws Throwable {
        if (c == null) throw new NullPointerException("c is null");
        if (address == null) throw new NullPointerException("address is null");
        try
        {
            return Stub.performCreate(c, null, address);
        }
        catch (Throwable t)
        {
            throw t;
        }
    }


    private static <T> T performCreate(Class<T> c, Skeleton<T> skeleton,
                                       InetSocketAddress socketAddress)
        throws InvocationTargetException, Throwable
    {
        System.err.println("therad[" + Thread.currentThread().getId() + "] performCreate()");
        try
        {
            if( !Validation.isRemoteInterface(c) )
            {
                throw new Error("Class Does Not Represent a Remote Interface");
            }
            InvocationHandler handler = new RemoteInvocationHandler(c, skeleton, socketAddress);
            ROR ror = new ROR(handler);
            Class<?> proxyRemoteInterface = ROR.getProxyClass(c.getClassLoader(), c);
            T t = (T) proxyRemoteInterface.getConstructor(InvocationHandler.class)
                .newInstance(handler);
            return t;
        }
        catch (InvocationTargetException t)
        {
            throw  t.getTargetException();
        }
        catch (Throwable t)
        {
            throw t;
        }
    }



}
