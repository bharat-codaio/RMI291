package rmi;



import javafx.util.Pair;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class Shuttle implements Serializable
{
    int hashCode;
    Pair<Type, Object>[] args;
    Class<?>[] paramTypes;
    String methodString;
    Type returnType;
    String name;

    Shuttle(Method method, Pair<Type, Object>[] args)
    {
        this.hashCode = method.hashCode();
        this.args = args;
        this.methodString = method.toString();
        this.returnType = method.getGenericReturnType();
        this.paramTypes = method.getParameterTypes();
        this.name = method.getName();
    }
}

