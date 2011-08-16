package app.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 *
 * @author Max Artyukhov
 */
public class GwtExampleEntryPoint implements EntryPoint {

    public void onModuleLoad() {

        final TextBox textBox = new TextBox();
        RootPanel.get("text_div").add(textBox);
        
        Button button = new Button("Try");
        RootPanel.get("button_div").add(button);
        
        final Label resultLabel = new Label();
        RootPanel.get("label_div").add(resultLabel);
        
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String textValue = textBox.getValue();
                EchoServiceAsync.INSTANCE.echo(textValue, new AsyncCallback<String>() {

                    public void onFailure(Throwable caught) {
                        Window.alert("Exception occured on the server side; Please check server log: " + caught.toString());
                    }

                    public void onSuccess(String result) {
                        resultLabel.setText(result);
                    }
                });
            }
        });
        
    }
    
}
