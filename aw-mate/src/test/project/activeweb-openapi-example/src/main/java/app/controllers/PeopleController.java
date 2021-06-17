package app.controllers;

import app.models.Person;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import java.io.IOException;


public class PeopleController extends MyAbstractController {

    /** @api
     * parameters
     *
      description: "Returns all people from the system that the user has access to"
      responses:
        "200":
          description: "A list of pets."
     */
    public void index(){}

    /**
     * @api [post] /people/create
     * description: "creates new person"
     * responses:
     *   "200":
     *     description: "A list of pets."
     */
    public void create() throws IOException {}

    /**
     *
      summary: Returns a list of users.
      description: Optional extended description in CommonMark or HTML.
      responses:
        '200':    # status code
          description: A JSON array of user names
          content:
            application/json:
              schema: 
                type: array
                items: 
                  type: string


     */
    public void show(){

    }


    /**
     * dsfdfgv
     * @param person
     */
    @POST
    public void savePerson(Person person){}

    /**
     * asfasdfv
     */
    private void getSomething(){}

    /**
     * hello
     * @param person
     * @return
     */
    public int getNumber(Person person){
        return 0;
    }

    /**
     * hello
     */
    public void getNumber2(String name, String name2){}

    public void system(){}

    @Override
    public void foo() {}
}
