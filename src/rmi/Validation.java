package rmi;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Created by bharatbatra on 2/12/17.
 */
public class Validation
{
    public static boolean isRemoteInterface(Class c)
    {

        if (c == null) return false;

        if (!c.isInterface()) return false;

        Method[] methods = c.getMethods();

        if(methods.length == 0)
        {
            return true;
        }

        for(Method method : methods)
        {
            if(Validation.isStandardMethod(method))
            {
                continue;
            }
            Class<?>[] all = method.getExceptionTypes();
            if(!Arrays.asList(all).contains(RMIException.class))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean isStandardMethod(Method method)
    {
        String name = method.getName();
        switch (name)
        {
            case "equals":
                return isEquals(method);
            case "toString":
                return isToString(method);
            case "hashCode":
                return isHashCode(method);
            default:
                return false;
        }
    }

    private static boolean isEquals(Method method)
    {
        // Parameter number
        if (method.getParameterCount() != 1) return false;
        // Return Type
        Method objectEquals = getObjectMethod("equals");
        if (!isSameReturnType(method, objectEquals)) return false;
        // Parameter Type
        Type paramType = method.getGenericParameterTypes()[0];
        if (paramType != Object.class) return false;
        return true;
    }

    private static boolean isToString(Method method)
    {
        // Parameter number
        if (method.getParameterCount() != 0) return false;
        // Return Type
        Method objectToString = getObjectMethod("toString");
        if (!isSameReturnType(method, objectToString)) return false;
        return true;
    }

    private static boolean isHashCode(Method method)
    {
        // Parameter number
        if (method.getParameterCount() != 0) return false;
        // Return Type
        Method objectHashCode = getObjectMethod("hashCode");
        if (!isSameReturnType(method, objectHashCode)) return false;
        return true;
    }

    private static boolean isSameReturnType(Method method1, Method method2)
    {
        return (method1.getGenericReturnType().getTypeName()
            .equals(method2.getGenericReturnType().getTypeName()));
    }

    private static Method getObjectMethod(String name)
    {
        Method methods[] = Object.class.getMethods();
        for (Method m: methods)
        {
            if (m.getName().equals(name)) return m;
        }
        return null;
    };
}
