package app.config;

import app.controllers.PetController;
import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

public class RouteConfigPetStore extends AbstractRouteConfig {
    @Override
    public void init(AppContext appContext) {
        strictMode(); // ban all other routes

        route("/pet").to(PetController.class).action("add").post();
        route("/pet").to(PetController.class).action("update").put();
        route("/pet/findByStatus").to(PetController.class).action("findByStatus").get();
        route("/pet/{petId}").to(PetController.class).action("getPet").get();
        route("/pet/{petId}").to(PetController.class).action("updatePet").post();
        route("/pet/{petId}").to(PetController.class).action("deletePet").delete();
    }
}
