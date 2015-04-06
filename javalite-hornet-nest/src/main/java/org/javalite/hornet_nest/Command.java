/*
Copyright 2009-2015 Igor Polevoy

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


package org.javalite.hornet_nest;

/**
 * Implementations of this interface will have to know how to serialize themselves into
 * a string using a <code>toString()</code> method and how to de-serialize from string using <code>fromString(String)</code>
 * method.
 *
 * @author Igor Polevoy on 4/4/15.
 */
public interface Command  {
    /**
     * Implementation will have to de-serialize from string.
     *
     * @param commandString String representation of a command. Can be XML, JSON or whatever you like,
     *                      so long as <code>toString()</code> produces a string and this method initializes a new
     *                      object with the same exact values.
     */
    void fromString(String commandString);
    void execute();
}
