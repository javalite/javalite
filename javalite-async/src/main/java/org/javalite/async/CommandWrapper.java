package org.javalite.async;

import org.javalite.json.JSONHelper;

public class CommandWrapper {
    private String type;
    private Command command;

    public CommandWrapper() {}


    public CommandWrapper(String type, Command command) {
        this.type = type;
        this.command = command;
    }

    public String getType() {
        return type;
    }

    public String toJSON(){
        String commandJson = JSONHelper.toJSON(command);
        return """
                { "type" : "%s",
                   "command": %s
                }
                """.formatted(type, commandJson);
    }
}
