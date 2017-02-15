package rmi;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** RMI skeleton

 <p>
 A skeleton encapsulates a multithreaded TCP server. The server's clients are
 intended to be RMI stubs created using the <code>Stub</code> class.

 <p>
 The skeleton class is parametrized by a type variable. This type variable
 should be instantiated with an interface. The skeleton will accept from the
 stub requests for calls to the methods of this interface. It will then
 forward those requests to an object. The object is specified when the
 skeleton is constructed, and must implement the remote interface. Each
 method in the interface should be marked as throwing
 <code>RMIException</code>, in addition to any other exceptions that the user
 desires.

 <p>
 Exceptions may occur at the top level in the listening and service threads.
 The skeleton's response to these exceptions can be customized by deriving
 a class from <code>Skeleton</code> and overriding <code>listen_error</code>
 or <code>service_error</code>.
 */
public class Skeleton<T>
{
    private Lock lock = new ReentrantLock();
    private Condition methodInvoking = lock.newCondition();
    private int currentlyInvoking = 0;
    private T server;
    private InetSocketAddress socketAddress;
    private LinkedList<Thread> threads = new LinkedList<Thread>();
    private Thread listenerThread = null;
    private boolean isStarted = false;
    private boolean shouldListenerRun;
    private ServerSocket serverSocket;
    private SkeletonService<T> skeletonService = new SkeletonService<>();
    private Class<T> c; // class
    private int port = -1;
    private boolean isLocalHost = false;
    private String whichConstructor = null;

    /** Creates a <code>Skeleton</code> with no initial server address. The
     address will be determined by the system when <code>start</code> is
     called. Equivalent to using <code>Skeleton(null)</code>.

     <p>
     This constructor is for skeletons that will not be used for
     bootstrapping RMI - those that therefore do not require a well-known
     port.

     @param c An object representing the class of the interface for which the
     skeleton server is to handle method call requests.
     @param server An object implementing said interface. Requests for method
     calls are forwarded by the skeleton to this object.
     @throws Error If <code>c</code> does not represent a remote interface -
     an interface whose methods are all marked as throwing
     <code>RMIException</code>.
     @throws NullPointerException If either of <code>c</code> or
     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server)
        throws Error, NullPointerException
    {
        if (c == null) throw new NullPointerException("c == null");
        if (server == null) throw new NullPointerException("server == null");
        if (!Validation.isRemoteInterface(c))
            throw new Error("server's Class does not implement Remote");
        this.server = server;
        this.c = c;
        this.isLocalHost = true;
        this.whichConstructor = "Skeleton(Class<T> c, T server)";
    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

     <p>
     This constructor should be used when the port number is significant.

     @param c An object representing the class of the interface for which the
     skeleton server is to handle method call requests.
     @param server An object implementing said interface. Requests for method
     calls are forwarded by the skeleton to this object.
     @param address The address at which the skeleton is to run. If
     <code>null</code>, the address will be chosen by the
     system when <code>start</code> is called.
     @throws Error If <code>c</code> does not represent a remote interface -
     an interface whose methods are all marked as throwing
     <code>RMIException</code>.
     @throws NullPointerException If either of <code>c</code> or
     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        if (c == null) throw new NullPointerException("c == null");
        if (server == null) throw new NullPointerException("server == null");
        if (!Validation.isRemoteInterface(c))
            throw new Error("server's Class does not implement Remote : " + c);
        if (address == null)
        {
            this.isLocalHost = true;
        }
        else
        {
            this.socketAddress = address;
            this.port = address.getPort();
        }
        this.server = server;
        this.c = c;
        this.whichConstructor = "Skeleton(Class<T> c, T server, InetSocketAddress address)";
    }

    /** Called when the listening thread exits.

     <p>
     The listening thread may exit due to a top-level exception, or due to a
     call to <code>stop</code>.

     <p>
     When this method is called, the calling thread owns the lock on the
     <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
     calling <code>start</code> or <code>stop</code> from different threads
     during this call.

     <p>
     The default implementation does nothing.

     @param cause The exception that stopped the skeleton, or
     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
        this.isStarted = false;
    }

    /** Called when an exception occurs at the top level in the listening
     thread.

     <p>
     The intent of this method is to allow the user to report exceptions in
     the listening thread to another thread, by a mechanism of the user's
     choosing. The user may also ignore the exceptions. The default
     implementation simply stops the server. The user should not use this
     method to stop the skeleton. The exception will again be provided as the
     argument to <code>stopped</code>, which will be called later.

     @param exception The exception that occurred.
     @return <code>true</code> if the server is to resume accepting
     connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        this.isStarted = false;
        exception.printStackTrace();
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

     <p>
     The default implementation does nothing.

     @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
        exception.printStackTrace();
    }

    /** Starts the skeleton server.

     <p>
     A thread is created to listen for connection requests, and the method
     returns immediately. Additional threads are created when connections are
     accepted. The network address used for the server is determined by which
     constructor was used to create the <code>Skeleton</code> object.

     @throws RMIException When the listening socket cannot be created or
     bound, when the listening thread cannot be created,
     or when the server has already been started and has
     not since stopped.
     */
    public synchronized void start() throws RMIException
    {
        print();
        if (this.isStarted()) throw new RMIException("skeleton already started");
        try
        {
            InetSocketAddress sockAddress = determineAddress(isLocalHost, socketAddress);
            this.serverSocket = new ServerSocket(sockAddress.getPort(), 0, sockAddress.getAddress());
            if (socketAddress == null && isLocalHost) socketAddress = sockAddress;
            this.port = sockAddress.getPort();
        }
        catch (Exception e)
        {
            RMIException rmiException = (e instanceof RMIException)
                ? (RMIException) e
                : new RMIException(e.getMessage(), e.getCause());
            listen_error(rmiException);
        }

        // Create thread to listen for connection requests
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    while (!Thread.currentThread().isInterrupted())
                    {
                        Socket socket = serverSocket.accept();
                        Runnable clientRunnable = createHandler(lock, methodInvoking,
                            currentlyInvoking, c, server, socket);
                        Thread thread = new Thread(clientRunnable);
                        threads.add(thread);
                        thread.start();
                    }
                }
                catch (IOException e)
                {
                    if (Thread.currentThread().isInterrupted())
                    {
                        // We caused this exception
                        return;
                    }
                    RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
                    rmiException.printStackTrace();
                    listen_error(rmiException);
                    isStarted = false;
                    return;
                }

            }
        });
        this.isStarted = true;
        this.listenerThread = listener;
        listener.start();
    }

    private InetSocketAddress determineAddress(boolean isLocalHost, InetSocketAddress socketAddress)
        throws UnknownHostException, RMIException
    {
        if (isLocalHost)
        {
            int port = SkeletonService.findAvailablePort();
            if (port == -1) throw new RMIException("could not find available port");
            return new InetSocketAddress(InetAddress.getLocalHost(), port);
        }
        return socketAddress;
    }


    /** Stops the skeleton server, if it is already running.

     <p>
     The listening thread terminates. Threads created to service connections
     may continue running until their invocations of the <code>service</code>
     method return. The server stops at some later time; the method
     <code>stopped</code> is called at that point. The server may then be
     restarted.
     */
    public synchronized void stop()
    {
        if (!this.isStarted()) {
            return;
        }
        try
        {
            for (Thread thread : this.threads) thread.join();
            this.threads.clear();
            if (this.listenerThread != null)
            {
                if (this.serverSocket != null)
                {
                    this.serverSocket.close();
                }
                else
                {
                }
                this.listenerThread.interrupt();
                this.listenerThread.join();
            }
            else
            {
            }
            this.isStarted = false;
            stopped(null);
        }
        catch (Exception e)
        {
            RMIException rmiException = new RMIException(e);
            service_error(rmiException);
        }

    }

    Runnable createHandler(Lock lock, Condition methodInvoking, int currentlyInvoking, Class<T> c,
                           T server, Socket socket)
    {
        Runnable runnable = () -> {
            try {
                Thread current = Thread.currentThread();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Shuttle shuttle = (Shuttle) ois.readObject();

                // handle a call from Stub for a methodCall
                skeletonService.handleMethodCall(lock, methodInvoking, currentlyInvoking, c,
                    server, socket, oos, shuttle);
            }
            catch (ClassNotFoundException e)
            {
                RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
                service_error(rmiException);
            }
            catch (IOException e)
            {
                RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
                service_error(rmiException);
            }
            catch (RMIException e)
            {
                service_error(e);
            }
            catch (Exception e)
            {

            }
        };
        return runnable;
    }

    InetAddress getAddress()
    {
        try
        {
            if (this.isLocalHost) return InetAddress.getLocalHost();
            if (this.socketAddress == null)
                throw new RMIException("no socketAddress but not localhost");
            return this.socketAddress.getAddress();
        }
        catch  (Exception e)
        {
            RMIException rmiException = new RMIException(e.getMessage(), e.getCause());
            service_error(rmiException);
            return null;
        }
    }

    public String toString()
    {
        String address = this.socketAddress.getAddress().toString();
        String port = "" + this.socketAddress.getPort();
        return "Skeleton - " + address + ":" + port;
    }

    int getPort() {
        return this.port;
    }

    boolean isStarted()
    {
        return this.isStarted;
    }

    private void print()
    {
        String heading = "[Skeleton]==========================================================|";
        int lineLength = heading.length();

        String serverSocketString = this.serverSocket == null
            ? "null"
            : this.serverSocket.toString();
    }

    private String getLine(String string, int lineLength)
    {
        String line = "|";
        line += string;
        line += " ";
        for (int i = 0 ; i < (lineLength - string.length() - 3) ; i++)
        {
            line += "-";
        }
        line += "|";
        return line;
    }
//    private Thread listenerThread = null;
//    private boolean isStarted = false;
//    private boolean shouldListenerRun;
//    private ServerSocket serverSocket;
}
