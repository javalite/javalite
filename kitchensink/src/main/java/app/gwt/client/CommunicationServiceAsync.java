package app.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.GWT;
import static app.gwt.client.RPCServiceBinder.rebind;

/**
 *
 * @author Max Artyukhov
 */
public interface CommunicationServiceAsync {
    
    public static final CommunicationServiceAsync INSTANCE = rebind(GWT.create(CommunicationService.class), "/communication");
    
    void generate(String text, AsyncCallback<String> callback);
    
}
