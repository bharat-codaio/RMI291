package rmi;

import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;
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
    private T server;
    private InetSocketAddress sockAdr;
    private LinkedList<Thread> threads = new LinkedList<Thread>();
    private Thread mainThread = null;
    private boolean shouldMainKeepRunning;
    private SkeletonService<T> skeletonService = new SkeletonService<>();
    private Class<T> c; // class
    private Hashtable<Proxy, Object> mm = new Hashtable<>();

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
    public Skeleton(Class<T> c, T server) throws Error, NullPointerException
    {
        if (c == null) throw new NullPointerException("c == null");
        if (server == null) throw new NullPointerException("server == null");
        if ( !Validation.isRemoteInterface(c) )
            throw new Error("server's Class does not implement Remote");
        this.server = server;
        this.c = c;
        this.sockAdr = null;
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
            throw new Error("server's Class does not implement Remote");
        this.server = server;
        this.c = c;
        this.sockAdr = address;
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
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

     <p>
     The default implementation does nothing.

     @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
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
        if (this.sockAdr == null)
        {
            throw new RMIException("attempt to call start() with no sockAdr " +
                    "set");
        }


        // Create thread to listen for connection requests
        Thread main = new Thread(() -> {
            InetAddress address = sockAdr.getAddress();
            int port = sockAdr.getPort();
            try {
                // Create a ServerSocket
                ServerSocket serverSocket = new ServerSocket(port, 0, address);
                // Initiate endless listen loop
                while (this.shouldMainKeepRunning)
                {
                    // Get a socket connection if you can
                    Socket socket = serverSocket.accept();
                    // Generate a handler for that connection
                    Runnable runnable = createHandler(lock, c, server, mm,
                            socket);
                    // Create a thread for that handler
                    Thread thread = new Thread(runnable);
                    // Add the thread to the linked list of threads
                    threads.add(thread);
                    // Start the thread for the handler
                    thread.start();
                }
            } catch (IOException e) {
                listen_error(e);
            }
        });

        this.mainThread = main;
        this.shouldMainKeepRunning = true;
        main.run();
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
        this.shouldMainKeepRunning = false;
    }

    Runnable createHandler(Lock lock, Class<T> c, T server,
                           Hashtable<Proxy, Object> mm, Socket socket)
    {
        Runnable runnable = () -> {
            try {
                // Create an ObjectOutputStream
                ObjectOutputStream oos = new
                        ObjectOutputStream(socket.getOutputStream());
                // Create an ObjectInputStream
                ObjectInputStream ois = new
                        ObjectInputStream(socket.getInputStream());
                // Flush the ObjectOutputStream
                oos.flush();
                // Get the Shuttle
                Shuttle shuttle = (Shuttle) ois.readObject();

                // handle a call from Stub for a methodCall
                skeletonService.handleMethodCall(lock, c, server, mm, socket,
                        oos, shuttle);

            }
            catch (RMIException e)
            {
                service_error(e);
            }
            catch (Exception e) {
                service_error(new RMIException(e.getCause()));
            }
        };
        return runnable;
    }


    /**
     * Gets the InetSocketAddress associated with the server the skeleton is
     * working on
     *
     * @return the <code>InetSocketAddress</code> associated with the server
     */
    public InetSocketAddress getSockAdr()
    {
        return this.sockAdr;
    }
}
