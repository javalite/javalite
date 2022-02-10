package app.config;

import app.controllers.CustomController;
import app.controllers.HomeController;
import app.controllers.SegmentsController;
import app.controllers.TestController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfig1 extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        strictMode();
        route("/hello").to(TestController.class).action("foo");                         // has annotation API
        route("/person_save").to(CustomController.class).action("save_person").post();  // has annotation API
        route("/custom").to(CustomController.class).action("index").get();              // has file API
        route("/segments/{id}").to(SegmentsController.class).action("index");           // no API
        route("/about").to(HomeController.class).action("about");                       // no API
        route("/solutions").to(HomeController.class).action("solutions");               // no API
        route("/team").to(HomeController.class).action("team");                         //action does not exist
    }
}
