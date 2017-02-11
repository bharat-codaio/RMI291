package own_test;
import rmi.Remote;
import rmi.RMIException;
/**
 * Created by bharatbatra on 2/5/17.
 */
public interface Calc extends rmi.Remote
{
    public int square (int x) throws rmi.RMIException;
}
