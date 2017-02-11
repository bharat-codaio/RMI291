package rmi;

import java.lang.reflect.Type;

/**
 * Created by anthonyaltieri on 2/4/17.
 */
public class Return {
    Type type;
    Object value;

    Return(Type type, Object value)
    {
        this.type = type;
        this.value = value;
    }

}
