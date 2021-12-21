package app.websockets;

import org.javalite.activeweb.websockets.AppEndpoint;

public class ChatEndpoint extends AppEndpoint {
    @Override
    public void onMessage(String message) {
        try{
            sendMessage(" you sent: " + message);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
