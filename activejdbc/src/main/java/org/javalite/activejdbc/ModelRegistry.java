/*
Copyright 2009-2019 Igor Polevoy

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

package org.javalite.activejdbc;

import org.javalite.validation.ValidationSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Stores metadata for a Model: converters, etc.
 *
 * @author ericbn
 */
public class ModelRegistry  extends ValidationSupport {
    private final List<CallbackListener> callbacks = new ArrayList<>();

    void callbackWith(CallbackListener... listeners) {
        callbackWith(Arrays.asList(listeners));
    }

    void callbackWith(Collection<CallbackListener> callbacks) {
        this.callbacks.clear();
        this.callbacks.addAll(callbacks);
    }

    List<CallbackListener> callbacks() {
        return callbacks;
    }
}
