package org.javalite.activeweb.mojo;

import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

public class PrintMojoSpec {

    @Test
    public void shouldPrintTables(){
        SystemStreamUtil.replaceOut();
        PrintMojo printMojo = new PrintMojo();
        printMojo.print(ClassLoader.getSystemClassLoader());

        String sysOut = SystemStreamUtil.getSystemOut();

        the(sysOut).shouldContain("""
                                **
                                **  STANDARD END POINTS
                                **
                                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                                | Number | Path                     | HTTP Methods | Controller                            | Method                             |
                                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                                | 1      | /custom/index            | POST         | app.controllers.CustomController      | index()                            |
                                | 2      | /custom/save_person      | POST         | app.controllers.CustomController      | savePerson(app.controllers.Person) |
                                | 3      | /home/about              | GET          | app.controllers.HomeController        | about()                            |
                                | 4      | /home/solutions          | GET          | app.controllers.HomeController        | solutions()                        |
                                | 5      | /http_methods/index      | POST         | app.controllers.HttpMethodsController | index()                            |
                                | 6      | /http_methods/do_post    | POST         | app.controllers.HttpMethodsController | doPost()                           |
                                | 7      | /http_methods/do_put     | PUT          | app.controllers.HttpMethodsController | doPut()                            |
                                | 8      | /http_methods/do_head    | HEAD         | app.controllers.HttpMethodsController | doHead()                           |
                                | 9      | /http_methods/do_options | OPTIONS      | app.controllers.HttpMethodsController | doOptions()                        |
                                | 10     | /http_methods/do_patch   | PATCH        | app.controllers.HttpMethodsController | doPatch()                          |
                                | 11     | /pet/add                 | POST         | app.controllers.PetController         | add()                              |
                                | 12     | /pet/update              | PUT          | app.controllers.PetController         | update()                           |
                                | 13     | /pet/find_by_status      | GET          | app.controllers.PetController         | findByStatus()                     |
                                | 14     | /pet/get_pet             | GET          | app.controllers.PetController         | getPet()                           |
                                | 15     | /pet/update_pet          | POST         | app.controllers.PetController         | updatePet()                        |
                                | 16     | /pet/delete_pet          | DELETE       | app.controllers.PetController         | deletePet()                        |
                                | 17     | /pet_rest                | GET          | app.controllers.PetRestController     | index()                            |
                                | 18     | /pet_rest/new_form       | GET          | app.controllers.PetRestController     | new_form()                         |
                                | 19     | /pet_rest                | POST         | app.controllers.PetRestController     | create()                           |
                                | 20     | /pet_rest/{id}           | GET          | app.controllers.PetRestController     | show()                             |
                                | 21     | /pet_rest/{id}/edit_form | GET          | app.controllers.PetRestController     | edit_form()                        |
                                | 22     | /pet_rest/{id}           | PUT          | app.controllers.PetRestController     | update()                           |
                                | 23     | /pet_rest/{id}           | DELETE       | app.controllers.PetRestController     | destroy()                          |
                                | 24     | /segments/index          | GET          | app.controllers.SegmentsController    | index()                            |
                                | 25     | /segments/foobar         | GET          | app.controllers.SegmentsController    | foobar()                           |
                                | 26     | /segments/foobar_2       | GET          | app.controllers.SegmentsController    | foobar2()                          |
                                | 27     | /test/index              | POST         | app.controllers.TestController        | index()                            |
                                | 28     | /test/foo                | POST         | app.controllers.TestController        | foo()                              |
                                | 29     | /test/save_person        | POST         | app.controllers.TestController        | savePerson(app.controllers.Person) |
                                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                                
                                **
                                **  CUSTOM END POINTS
                                **
                                +--------+--------------+--------------+----------------------------------+------------------------------------+
                                | Number | Path         | HTTP Methods | Controller                       | Method                             |
                                +--------+--------------+--------------+----------------------------------+------------------------------------+
                                | 1      | /hello       | POST         | app.controllers.TestController   | foo()                              |
                                | 2      | /person_save | POST         | app.controllers.CustomController | savePerson(app.controllers.Person) |
                                +--------+--------------+--------------+----------------------------------+------------------------------------+""");

        SystemStreamUtil.restoreSystemOut();

        System.out.println(sysOut);
    }
}
