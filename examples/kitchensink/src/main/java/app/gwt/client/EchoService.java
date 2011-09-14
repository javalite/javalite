package app.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 *
 * @author Max Artyukhov
 */
public interface EchoService extends RemoteService {
    
    String echo(String text);
    
}
