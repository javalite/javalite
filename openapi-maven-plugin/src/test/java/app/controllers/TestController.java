package app.controllers;


import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

public class TestController extends AbstractController {

    @GET @POST
    public void index(){}

    @Override
    public void foo() {}

    @POST
    public void savePerson(Person person) {}
}
