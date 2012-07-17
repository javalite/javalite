package app.controllers;

import org.javalite.activeweb.AppController;

/**
 * @author Igor Polevoy: 1/5/12 9:03 AM
 */
public class HelloController extends AppController{

   public void show(){
       respond("Hello.. " + param("name"));
   }
}
