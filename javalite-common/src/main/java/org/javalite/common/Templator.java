/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.common;


import java.util.Map;

import static org.javalite.common.Util.readResource;

/**
 * Simple implementation of small quick templates filled with dynamic data.<b></b>
 * An example of a template:
 *
 *
 * <pre>
 *     Hello, {{name}}!
 * </pre>
 *
 *
 * Usage:
 *
 * <pre>
 *     Templator templator = new Templator("/hello_template.txt");
 *     System.out.println(templator.merge(map("name", "Kyla"))); // prints: Hello, Kyla!
 * </pre>
 *
 * <p></p>
 * Note: Yes, I'm aware of existence of Mustache, Freemarker, Velocity, etc.
 * This implementation is for quickly loading small snippets  of text merged with data.
 * The goal is to eliminate code pollution. If you need more power, use bigger frameworks.
 *
 * @author Igor Polevoy on 9/30/16.
 */
public class Templator {

    private String template;

    /**
     * @param templatePath path to a template on classpath
     */
    public Templator(String templatePath){
        template = readResource(templatePath);
    }

    /**
     * This method is used in repeated operations, since it will load a resource once.
     *
     * @param values values to merge into a template
     * @return result of merging
     */
    public String merge(Map<String, ?> values){
        return mergeFromTemplate(template, values);
    }

    /**
     * This method  is used in one-off operations, where it is OK to load a template every time.
     *
     * Example:
     * <code>
     *     String result = Templator.mergeFromPath(readResource("/message_template.txt", valuesMap));
     * </code>
     *
     * @param templatePath template to merge
     * @param values values to merge into a template
     * @return result of merging
     */
    public static String mergeFromPath(String templatePath, Map<String, ?> values) {
        return mergeFromTemplate(readResource(templatePath), values);
    }

    /**
     * Merges from string as template.
     *
     * @param template template content, with placeholders like: {{name}}
     * @param values  map with values to merge
     */
    public static String mergeFromTemplate(String template, Map<String, ?> values) {
        for (String param : values.keySet()) {
            template = template.replace("{{" + param + "}}", values.get(param) == null ? "" : values.get(param).toString());
        }
        return template.replaceAll("\n|\r|  ", "");
    }
}
