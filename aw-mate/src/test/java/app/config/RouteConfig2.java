package app.config;

import app.controllers.CustomController;
import app.controllers.HomeController;
import app.controllers.SegmentsController;
import app.controllers.TestController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfig2 extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        strictMode();
        route("/segments/{id}").to(SegmentsController.class).action("foobar");
    }
}
