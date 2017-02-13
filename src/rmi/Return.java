package rmi;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Created by anthonyaltieri on 2/4/17.
 */
public class Return<T> implements Serializable
{
    Type type;
    Object value;
    InvocationTargetException invocationTargetException;

    Return(Type type, Object value, InvocationTargetException invocationTargetException)
    {
        this.type = type;
        this.value = value;
        this.invocationTargetException = invocationTargetException;
    }

}
