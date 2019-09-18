package org.javalite.async;

import com.google.inject.Inject;
import org.javalite.async.services.GreetingService;

/**
 * @author Igor Polevoy on 2/2/16.
 */
public class HelloCommandListener  extends CommandListener{

    @Inject
    private GreetingService greetingService;

    @Override
    public <T extends Command> void onCommand(T command) {
        assert greetingService != null;
        super.onCommand(command);
    }
}
