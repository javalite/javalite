package app.controllers;

import org.javalite.activejdbc.DBException;
import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 3/5/12 11:18 AM
 */
public class DbExceptionController  extends AppController{

    public void index(){
        throw new DBException("this is an issue 88");
    }
}
