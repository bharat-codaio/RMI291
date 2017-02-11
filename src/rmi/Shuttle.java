package rmi;



import javafx.util.Pair;

import java.lang.reflect.Type;

class Shuttle {
    String methodName;
    Type returnType;
    Pair<Type, Object>[] args;

    Shuttle(String methodName, Type returnType, Pair<Type, Object>[] args)
    {
        this.methodName = methodName;
        this.returnType = returnType;
        this.args = args;
    }
}

