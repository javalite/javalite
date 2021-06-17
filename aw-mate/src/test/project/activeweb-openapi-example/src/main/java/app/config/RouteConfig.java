package app.config;

import app.controllers.CustomController;
import app.controllers.PeopleController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfig extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        route("/people").to(CustomController.class).action("get_people").get();
        route("/person_save").to(PeopleController.class).action("save_person").post();
    }
}
