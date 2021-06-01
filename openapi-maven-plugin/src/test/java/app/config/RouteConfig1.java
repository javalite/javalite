package app.config;

import app.controllers.CustomController;
import app.controllers.SegmentsController;
import app.controllers.TestController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfig1 extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        strictMode();
        route("/hello").to(TestController.class).action("foo");
        route("/person_save").to(CustomController.class).action("save_person").post();
        route("/segments/{id}").to(SegmentsController.class).action("index");
    }
}
