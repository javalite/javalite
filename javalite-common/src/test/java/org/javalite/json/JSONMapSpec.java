package org.javalite.json;

import org.junit.Test;

import java.util.Map;

import static org.javalite.test.jspec.JSpec.the;

public class JSONMapSpec {
    static String JSON =  """
                {
                    "glossary": {
                        "title": "example glossary",
                		"GlossDiv": {
                            "title": "S",
                			"GlossList": {
                                "GlossEntry": {
                                    "ID": "SGML",
                					"SortAs": "SGML",
                					"GlossTerm": "Standard Generalized Markup Language",
                					"Acronym": "SGML",
                					"Abbrev": "ISO 8879:1986",
                					"GlossDef": {
                                        "para": "A meta-markup language, used to create markup languages such as DocBook.",
                						"GlossSeeAlso": ["GML", "XML"]
                                    },
                					"GlossSee": "markup"
                                }
                            }
                        }
                    }
                }""";

    @Test
    public void shouldFindImmediateChild(){
        JSONMap jsonMap = new JSONMap(JSONHelper.toMap(JSON));
        Map glossaryMap = (Map) jsonMap.get("glossary");
        the(glossaryMap.keySet().size()).shouldBeEqual(2);
        the(glossaryMap).shouldContain("title");
    }

    @Test
    public void shouldFindDeepMap(){
        JSONMap jsonMap = new JSONMap(JSONHelper.toMap(JSON));
        Map glossMap =  jsonMap.getMap("glossary.GlossDiv.GlossList");
        the(glossMap.keySet().size()).shouldBeEqual(1);
        the(glossMap).shouldContain("GlossEntry");
        the(jsonMap.getMap("glossary.GlossDiv.GlossList.GlossEntry").get("ID")).shouldBeEqual("SGML");
    }
}
