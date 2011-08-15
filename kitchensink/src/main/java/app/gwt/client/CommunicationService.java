package app.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 *
 * @author Max Artyukhov
 */
public interface CommunicationService extends RemoteService {
    
    String generate(String text);
    
}
