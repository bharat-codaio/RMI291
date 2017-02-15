package rmi;

import java.io.Serializable;

/**
 * Created by anthonyaltieri on 2/14/17.
 */
public class Pair<T,E> implements Serializable
{
    private T key;
    private E value;

    Pair(T key, E value)
    {
        this.key = key;
        this.value = value;
    }

    public T getKey()
    {
        return this.key;
    }

    public E getValue()
    {
        return this.value;
    }

}
