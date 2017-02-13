package own_test;//package rmi;
import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

import java.net.InetSocketAddress;

/**
 * Created by anthonyaltieri on 2/4/17.
 */
public class TestSetup
{
    public static void main(String[] args)
    {
        try
        {
            Calculator calculator = new Calculator();
            InetSocketAddress sockAdr = new InetSocketAddress(8080);
            Skeleton<Calculator> skeleton = new Skeleton(Calc.class, calculator, sockAdr);
            skeleton.start();
//            Calc c = Stub.create(Calc.class, sockAdr);
//            System.out.println("SQUARE OF 4 = " + c.square(4));

        }
        catch (RMIException e)
        {
            System.out.println("RMI EXCEPTION");
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

class Calculator implements Calc
{
    public int square (int x) throws RMIException
    {
        return (x^2);
    }
}