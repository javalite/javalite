package app.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Max Artyukhov
 */
public class GwtExampleEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        VerticalPanel verticalPanel = new VerticalPanel();
        RootPanel.get().add(verticalPanel);
        
        final TextBox textBox = new TextBox();
        verticalPanel.add(textBox);
        
        Button button = new Button("Try");
        verticalPanel.add(button);
        
        final Label resultLabel = new Label();
        verticalPanel.add(resultLabel);
        
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String textValue = textBox.getValue();
                CommunicationServiceAsync.INSTANCE.generate(textValue, new AsyncCallback<String>() {

                    public void onFailure(Throwable caught) {
                        Window.alert("Exception occured on the server side; Please check logs");
                    }

                    public void onSuccess(String result) {
                        resultLabel.setText(result);
                    }
                });
            }
        });
        
    }
    
}
