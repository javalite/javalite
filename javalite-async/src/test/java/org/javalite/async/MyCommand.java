package org.javalite.async;

import java.util.List;
import java.util.Map;

public
class MyCommand extends Command {
    private Map map;
    private List list;

    public MyCommand() {}

    public MyCommand(Map map, List list) {
        this.map = map;
        this.list = list;
    }

    @Override
    public void execute() {
        System.out.println("");
    }

    public Map getMap() {
        return map;
    }

    public List getList() {
        return list;
    }

}
