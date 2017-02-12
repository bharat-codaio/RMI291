package rmi;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by bharatbatra on 2/12/17.
 */
public class Validation
{
    public static boolean isRemoteInterface(Class c)
    {
        if (!c.isInterface()) return false;

        Method[] methods = c.getMethods();

        if(methods.length == 0)
        {
            return false;
        }

        boolean throwsRemotes = true;

        for(Method method : methods)
        {
            Class<?>[] all = method.getExceptionTypes();
            if(!Arrays.asList(all).contains(RMIException.class))
            {
                throwsRemotes = false;
            }
        }
        return  throwsRemotes ;
    }
}
