package app.config;

import app.controllers.HelloController;
import app.controllers.HomeController;
import app.controllers.PostsController;
import app.controllers.RpostsController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

/**
 * @author Igor Polevoy: 1/2/12 4:48 PM
 */
public class RouteConfig extends AbstractRouteConfig {
    public void init(AppContext appContext) {
        route("/myposts").to(PostsController.class);
        route("/greeting1").to(HomeController.class);
        route("/rposts_internal/{action}").to(RpostsController.class);
        route("/{action}/greeting/{name}").to(HelloController.class);
    }
}
