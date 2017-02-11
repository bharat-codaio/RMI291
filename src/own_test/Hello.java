//package own_test;//package rmi;
import own_test.*;
/**
 * Created by bharatbatra on 2/9/17.
 */
public class Hello {
    public static void main(String[] args)
    {
        AO a = new AO();
        BO b = new BO();
        PO p = new PO();

        System.out.println(a.getClass().isAssignableFrom(p.getClass()));
        System.out.println(a.getClass().isAssignableFrom(b.getClass()));
        System.out.println(p.getClass().isAssignableFrom(b.getClass()));

    }
}