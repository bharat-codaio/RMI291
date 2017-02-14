package conformance2.rmi;

import test.*;
import rmi.*;

public class OverloadTest
    extends BasicTestBase<OverloadTest.OverloadTestInterface>
{
    public static final String  notice = "checking method overloading";

    public OverloadTest()
    {
        super(OverloadTestInterface.class);
        setServer(new OverloadTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling overloaded local methods");

        try
        {
//            boolean test1 = !stub.equals(3);
//            if (test1) {
//                System.err.println("ERROR 1");
//                throw new TestFailed("unexpected result from method call - test1");
//            }
//            System.err.println("test 1 success!!!!!!!!!!!!!!!!!!!!");
//            boolean test2 = !stub.equals(stub, 3);
//            if (test2) {
//                System.err.println("ERROR 2");
//                throw new TestFailed("unexpected result from method call - test2");
//            }
//            System.err.println("test 2 success!!!!!!!!!!!!!!!!!!!!");
//            boolean test3 = !stub.toString(3).equals(Integer.toString(3));
//            if (test3)
//            {
//                System.err.println("ERROR 3");
//                throw new TestFailed("unexpected result from method call - test3");
//            };
//            System.err.println("test 3 success!!!!!!!!!!!!!!!!!!!!");
//            boolean test4 = stub.hashCode(3) != 3;
//            if (test4) {
//                System.err.println("ERROR 4");
//                throw new TestFailed("unexpected result from method call - test4");
//            }
//            System.err.println("test 4 success!!!!!!!!!!!!!!!!!!!!");
            if(!stub.equals(3) ||
               !stub.equals(stub, 3) ||
               !stub.toString(3).equals(Integer.toString(3)) ||
               (stub.hashCode(3) != 3))
            {
                throw new TestFailed("unexpected result from method call");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling " +
                                 "overloaded method", t);
        }

        task("calling overloaded regular methods");

        try
        {
            if((stub.add(3, 4) != 7) ||
               (stub.add(3, 4, 5) != 12) ||
               (stub.multiply(3, 4, 5) != 60) ||
               (stub.multiply(3, 4) != 12))
            {
                throw new TestFailed("unexpected result from method call");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling " +
                                 "overloaded method", t);
        }

        task();
    }

    public interface OverloadTestInterface
    {
        public boolean equals(int x) throws RMIException;
        public boolean equals(OverloadTestInterface other, int x)
            throws RMIException;
        public String toString(int x) throws RMIException;
        public int hashCode(int x) throws RMIException;
        public int add(int x, int y) throws RMIException;
        public int add(int x, int y, int z) throws RMIException;
        public int multiply(int x, int y, int z) throws RMIException;
        public int multiply(int x, int y) throws RMIException;
    }

    private static class OverloadTestServer implements OverloadTestInterface
    {
        @Override
        public boolean equals(int x)
        {
            return true;
        }

        @Override
        public boolean equals(OverloadTestInterface other, int x)
        {
            return true;
        }

        @Override
        public String toString(int x)
        {
            return Integer.toString(x);
        }

        @Override
        public int hashCode(int x)
        {
            return x;
        }

        @Override
        public int add(int x, int y)
        {
            return x + y;
        }

        @Override
        public int add(int x, int y, int z)
        {
            return x + y + z;
        }

        @Override
        public int multiply(int x, int y, int z)
        {
            return x * y * z;
        }

        @Override
        public int multiply(int x, int y)
        {
            return x * y;
        }
    }
}
