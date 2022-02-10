package app.config;

import app.controllers.SegmentsController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfig2 extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        strictMode();
        route("/segments/{id}").to(SegmentsController.class).action("foobar");
    }
}
