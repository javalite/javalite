package app.config;

import app.controllers.CustomController;
import app.controllers.TestController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfig extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        route("/hello").to(TestController.class).action("foo");
        route("/person_save").to(CustomController.class).action("save_person").post();
    }
}
