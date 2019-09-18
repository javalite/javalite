package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/4/12 4:26 PM
 */
public class SegmentRoute3Controller extends AppController {
    public void greeting(){
        view("user_id", param("user_id"), "fav_color", param("fav_color"), "id", getId());
    }
}
