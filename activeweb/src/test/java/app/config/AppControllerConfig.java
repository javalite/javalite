package app.config;

import app.controllers.DbExceptionController;
import app.controllers.DoFiltersController;
import app.controllers.filters.*;
import org.javalite.activeweb.AbstractControllerConfig;
import org.javalite.activeweb.AppContext;


/**
 * @author Igor Polevoy: 3/5/12 11:58 AM
 */
public class AppControllerConfig extends AbstractControllerConfig {
    public void init(AppContext appContext) {
        add(new GlobalFilter1(), new GlobalFilter2());
        add(new ControllerFilter1(), new ControllerFilter2()).to(DoFiltersController.class);
        add(new Issue88Filter()).to(DbExceptionController.class);
    }
}
