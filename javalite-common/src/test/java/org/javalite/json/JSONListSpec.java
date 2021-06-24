package org.javalite.json;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.javalite.json.JSONMapSpec.JSON;
import static org.javalite.test.jspec.JSpec.the;

public class JSONListSpec {

    @Test
    public void shouldFindList() {
        String json = """
                [
                 [3],
                 [4]
                ]
                """;

        JSONList list = new JSONList(json);
        the(list.get(0)).shouldBeA(List.class);
        the(list.getList(0).size()).shouldBeEqual(1);
        the(list.getList(0).get(0)).shouldBeEqual(3);
        the(list.getList(1).get(0)).shouldBeEqual(4);
    }

    @Test
    public void shouldFindMap() {
        String json = """
                [
                 {"name": "John"}
                ]
                """;

        JSONList list = new JSONList(json);
        the(list.get(0)).shouldBeA(Map.class);
        the(list.getMap(0).get("name")).shouldBeEqual("John");
    }


    @Test
    public void shouldFindDeepList() {
        JSONMap jsonMap = new JSONMap(JSONHelper.toMap(JSON));
        JSONList glossList = jsonMap.getList("glossary.GlossDiv.GlossList.GlossEntry.GlossDef.GlossSeeAlso");

        the(glossList.size()).shouldBeEqual(2);
        the(glossList).shouldContain("GML");
        the(glossList).shouldContain("XML");
    }

}
