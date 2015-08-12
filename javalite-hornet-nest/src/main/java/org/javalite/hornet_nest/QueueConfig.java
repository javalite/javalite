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
 * Simple configuration of  queue.
 *
 * @author Igor Polevoy on 4/4/15.
 */
public class QueueConfig {
    private String name;
    private Class commandListenerClass;
    private int listenerCount;

    /**
     * Creates a specification of a queue for HornetNest
     *
     * @param name human readable name of queue
     * @param commandListenerClass CommandListener class
     * @param listenerCount number of listeners to attach to a queue. Effectively this
     *                      is a number of processing threads.
     */
    public  QueueConfig(String name, Class commandListenerClass, int listenerCount) {
        this.name = name;
        this.commandListenerClass = commandListenerClass;
        this.listenerCount = listenerCount;
    }

    public String getName() {
        return name;
    }

    public Class getCommandListenerClass() {
        return commandListenerClass;
    }

    public int getListenerCount() {
        return listenerCount;
    }
}
