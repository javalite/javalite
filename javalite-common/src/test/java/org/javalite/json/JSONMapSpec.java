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
                			"GlossMap": {
                                "GlossEntry": {
                                    "ID": "SGML",
                					"SortAs": "SGML",
                					"ItemsNumber": 123,
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
    public void shouldFindImmediateChild() {
        JSONMap jsonMap = JSONHelper.toMap(JSON);
        Map glossaryMap = (Map) jsonMap.get("glossary");
        the(glossaryMap.keySet().size()).shouldBeEqual(2);
        the(glossaryMap).shouldContain("title");
    }

    @Test
    public void shouldFindDeepMap() {
        JSONMap jsonMap = JSONHelper.toMap(JSON);
        Map glossMap = jsonMap.getMap("glossary.GlossDiv.GlossMap");
        the(glossMap.keySet().size()).shouldBeEqual(1);
        the(glossMap).shouldContain("GlossEntry");
        the(jsonMap.getMap("glossary.GlossDiv.GlossMap.GlossEntry").get("ID")).shouldBeEqual("SGML");
    }

    @Test
    public void shouldFindDeepAttribute() {
        JSONMap jsonMap = JSONHelper.toMap(JSON);
        String title = jsonMap.getString("glossary.GlossDiv.GlossMap.GlossEntry.Acronym");
        the(title).shouldBeEqual("SGML");
    }

    @Test
    public void shouldFindDeepList() {
        JSONMap jsonMap = JSONHelper.toMap(JSON);
        JSONList seeAlso = jsonMap.getList("glossary.GlossDiv.GlossMap.GlossEntry.GlossDef.GlossSeeAlso");
        the(seeAlso.size()).shouldBeEqual(2);
        the(seeAlso.get(0)).shouldBeEqual("GML");
        the(seeAlso.get(1)).shouldBeEqual("XML");
    }

    @Test
    public void shouldFindDeepInt() {
        JSONMap jsonMap = JSONHelper.toMap(JSON);
        Integer items = jsonMap.getInteger("glossary.GlossDiv.GlossMap.GlossEntry.ItemsNumber");
        the(items).shouldBeEqual(123);
    }


    @Test
    public void shouldTestConstructor() {
        JSONMap jsonMap = new JSONMap("name", "John");
        the(jsonMap.get("name")).shouldBeEqual("John");
    }
}
