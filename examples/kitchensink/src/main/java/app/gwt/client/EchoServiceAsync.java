package app.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.GWT;
import static app.gwt.client.RPCServiceBinder.rebind;

/**
 *
 * @author Max Artyukhov
 */
public interface EchoServiceAsync {
    
    public static final EchoServiceAsync INSTANCE = rebind(GWT.create(EchoService.class), "/echo");

    void echo(String text, AsyncCallback<String> async);
}
