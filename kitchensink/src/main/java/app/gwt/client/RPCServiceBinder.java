package app.gwt.client;

import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 *
 * @author Max Artyukhov
 */
public final class RPCServiceBinder {
    
    private RPCServiceBinder(){}
    
    public static <T> T rebind(Object asyncInstance, String path) {        
        ((ServiceDefTarget) asyncInstance).setServiceEntryPoint(path);
        return (T)asyncInstance;
    }
    
}
