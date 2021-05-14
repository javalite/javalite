
/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javalite.openapi;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Adjusted from:
 *   http://www.java2s.com/Tutorial/Java/0125__Reflection/AnalyzeClassLoaderhierarchyforanygivenobjectorclassloader.htm
 */
public abstract class ClassLoaderUtils {

    /**
     * Show the class loader hierarchy for this class. Uses default line break and
     * tab text characters.
     *
     * @param obj
     *            object to analyze loader hierarchy for
     * @param role
     *            a description of the role of this class in the application (e.g.,
     *            "servlet" or "EJB reference")
     * @return a String showing the class loader hierarchy for this class
     */
    public static String showClassLoaderHierarchy(Object obj, String role) {
        return showClassLoaderHierarchy(obj, role, "\n", "\t");
    }

    /**
     * Show the class loader hierarchy for this class.
     *
     * @param obj
     *            object to analyze loader hierarchy for
     * @param role
     *            a description of the role of this class in the application (e.g.,
     *            "servlet" or "EJB reference")
     * @param lineBreak
     *            line break
     * @param tabText
     *            text to use to set tabs
     * @return a String showing the class loader hierarchy for this class
     */
    public static String showClassLoaderHierarchy(Object obj, String role, String lineBreak, String tabText) {
        String s = "object of " + obj.getClass() + ": role is " + role + lineBreak;
        return s + showClassLoaderHierarchy(obj.getClass().getClassLoader(), lineBreak, tabText, 0);
    }

    /**
     * Show the class loader hierarchy for the given class loader. Uses default line
     * break and tab text characters.
     *
     * @param cl
     *            class loader to analyze hierarchy for
     * @return a String showing the class loader hierarchy for this class
     */
    public static String showClassLoaderHierarchy(ClassLoader cl) {
        return showClassLoaderHierarchy(cl, "\n", "\t");
    }

    /**
     * Show the class loader hierarchy for the given class loader.
     *
     * @param cl
     *            class loader to analyze hierarchy for
     * @param lineBreak
     *            line break
     * @param tabText
     *            text to use to set tabs
     * @return a String showing the class loader hierarchy for this class
     */
    public static String showClassLoaderHierarchy(ClassLoader cl, String lineBreak, String tabText) {
        return showClassLoaderHierarchy(cl, lineBreak, tabText, 0);
    }

    /**
     * Show the class loader hierarchy for the given class loader.
     *
     * @param cl
     *            class loader to analyze hierarchy for
     * @param lineBreak
     *            line break
     * @param tabText
     *            text to use to set tabs
     * @param indent
     *            nesting level (from 0) of this loader; used in pretty printing
     * @return a String showing the class loader hierarchy for this class
     */
    private static String showClassLoaderHierarchy(ClassLoader cl, String lineBreak, String tabText, int indent) {
        StringBuffer countedIdent = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            countedIdent.append(tabText);
        }
        String currentIdent = countedIdent.toString();

        if (cl == null) {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            return currentIdent + "context class loader=[" + ccl + "] hashCode=" + ccl.hashCode();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(currentIdent).append("[").append(cl).append("] hashCode=").append(cl.hashCode()).append(lineBreak);

        buf.append(currentIdent).append("-> loader resource: ").append(cl.getResource("/")).append(lineBreak);
        try {
            // showing 'directories'
            Enumeration<URL> resources = cl.getResources(".");
            while (resources.hasMoreElements())
                buf.append(currentIdent).append("-> loader resource element: ").append(resources.nextElement())
                        .append(lineBreak);
            // showing 'jar files'
            resources = cl.getResources("META-INF");
            while (resources.hasMoreElements())
                buf.append(currentIdent).append("-> loader resource element: ").append(resources.nextElement())
                        .append(lineBreak);
        } catch (IOException ioe) {
            buf.append(currentIdent).append("-> err: can't get resource elements: ").append(ioe.getMessage())
                    .append(lineBreak);
        }

        ClassLoader parent = cl.getParent();
        return buf.toString() + showClassLoaderHierarchy(parent, lineBreak, tabText, indent + 1);
    }

}