package rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by anthonyaltieri on 2/12/17.
 */
public class ROR extends Proxy
{
    /**
     * Constructs a new {@code Proxy} instance from a subclass
     * (typically, a dynamic proxy class) with the specified value
     * for its invocation handler.
     *
     * @param h the invocation handler for this proxy instance
     * @throws NullPointerException if the given invocation handler, {@code h},
     *                              is {@code null}.
     */
    protected ROR(InvocationHandler h)
    {
        super(h);
        System.err.println("h.tostring() : " + h.toString());
    }
}
