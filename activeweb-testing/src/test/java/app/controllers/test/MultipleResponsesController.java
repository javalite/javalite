package app.controllers.test;

import org.javalite.activeweb.AppController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MultipleResponsesController extends AppController {

    public void index() throws IOException {
        outputStream().write("Not OK".getBytes(StandardCharsets.UTF_8));
        respond("OK");
    }
}
