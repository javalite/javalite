package app.controllers;

import org.javalite.activeweb.AppController;

//controller contains method that are not actions
public class BadController extends AppController {

    public int getAge() { return -1; }  //return type is not void

    public static void foo(){}          // static

    public void bar(int x, int y){}     // too many arguments

}
