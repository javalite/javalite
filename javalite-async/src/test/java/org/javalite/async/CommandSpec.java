package org.javalite.async;

import org.javalite.json.JSONList;
import org.javalite.json.JSONMap;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 1/31/16.
 */
public class CommandSpec {

    @Test
    public void shouldSerializeDeserializeXML(){
        HelloCommand helloCommand = new HelloCommand("Thanks for all the fish...");
        String xml = helloCommand.toXml();
        HelloCommand helloCommand1 = Command.fromXml(xml);
        a(helloCommand1.getMessage()).shouldBeEqual("Thanks for all the fish...");
        a(helloCommand).shouldNotBeTheSameAs(helloCommand1);
    }

    @Test
    public void shouldSerializeDeserializeBinary() throws IOException {
        HelloCommand helloCommand = new HelloCommand("Thanks for all the fish...");
        byte[] bytes  = helloCommand.toBytes();

        HelloCommand helloCommand1 = Command.fromBytes(bytes);
        a(helloCommand1.getMessage()).shouldBeEqual("Thanks for all the fish...");
        a(helloCommand).shouldNotBeTheSameAs(helloCommand1);
    }

    @Test
    public void shouldSerializeJSONClasses(){

        class MyCommand extends Command{
            private JSONMap map;
            private JSONList list;
            public MyCommand(JSONMap map, JSONList list) {
                this.map = map;
                this.list = list;
            }

            @Override
            public void execute() {
                System.out.println("");
            }

            Map getMap(){
                return map;
            }

            List getList(){
                return list;
            }

        }
        JSONMap map = new JSONMap(map("name", "John"));
        MyCommand command1 = new MyCommand(map, new JSONList(list(1, 2,3)));

        the(command1.getList()).shouldContain(1);
        the(command1.getList()).shouldContain(2);
        the(command1.getList()).shouldContain(3);

        MyCommand  command = Command.fromXml(command1.toXml());

        the(command.getList()).shouldContain(1);
        the(command.getList()).shouldContain(2);
        the(command.getList()).shouldContain(3);
        the(command.getMap().get("name")).shouldEqual("John");
    }
}
