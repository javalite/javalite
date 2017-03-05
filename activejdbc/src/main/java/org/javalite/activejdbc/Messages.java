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

package org.javalite.activejdbc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.javalite.common.Util.*;

/**
 * This class is used to pull messages from a resource bundle called <code>activejdbc_messages</code>.
 * It is primarily used by the validation framework, but client code can use it as well for other things.
 *
 * @author Igor Polevoy
 */

public class Messages {

    private static final String BUNDLE = "activejdbc_messages";

    private Messages() {
        
    }
    
    /**
     * Looks for a localized property/message in <code>activejdbc_messages</code> bundle.
     *
     * @param key key of the property.
     * @param locale locale of a bundle, or null for default locale
     * @param params list of parameters for a message. The order of parameters in this list will correspond to the
     * numeric order in the parameters listed in the message and has nothing to do with a physical order. This means
     * that the first parameter in the list will correspond to <code>{0}</code>, second to <code>{1}</code> and so on.
     * @return localized  message merged with parameters (if provided), or key if message not found.
     */
    public static String message(String key, Locale locale, Object... params) {
        String pattern;
        try {
            pattern = ResourceBundle.getBundle(BUNDLE, locale == null ? Locale.getDefault() : locale).getString(key);
        } catch (MissingResourceException e) {
            pattern = key;
        }
        return empty(params) ? pattern : new MessageFormat(pattern).format(params);
    }

    /**
     * Looks for a property/message in <code>activejdbc_messages</code> bundle.
     *
     * @param key key of the property.
     * @param params list of substitution parameters for a message.
     * @return message merged with parameters (if provided), or key if message not found.
     */
    public static String message(String key, Object... params) {
        return message(key, null, params);
    }
}
