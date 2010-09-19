/*
Copyright 2009-2010 Igor Polevoy

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

package activejdbc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class is used to pull messages from a resource bundle called <code>activejdbc_messages</code>.
 * It is primarily used by the validation framework, but client code can use it as well for other things.
 *
 * @author Igor Polevoy
 */

public class Messages {

    private static final String BUNDLE = "activejdbc_messages";

    /**
     * Looks for a localized property/message in <code>activejdbc_messages</code> bundle.
     *
     * @param key key of the property.
     * @param locale locale of a bundle. 
     * @param params list of parameters for a message. The order of parameters in this list will correspond to the
     * numeric order in the parameters listed in the message and has nothing to do with a physical order. This means
     * that the 0th parameter in the list will correspond to <code>{0}</code>, 1st to <code>{1}</code> and so on.
     * @return localized  message merged with parameters (if provided), or key if message not found.
     */
    public static String message(String key, Locale locale, Object... params) {
        return getMessage(key, locale, params);
    }

    /**
     * Looks for a property/message in <code>activejdbc_messages</code> bundle.
     *
     * @param key key of the property.
     * @param params list of substitution parameters for a message.
     * @return message merged with parameters (if provided), or key if message not found.
     */
    public static String message(String key, Object... params) {
        return getMessage(key, null, params);
    }

    private static String getMessage(String key, Locale locale, Object... params){
        MessageFormat mf = new MessageFormat("");
        try{
            if(locale == null){
                mf.applyPattern(ResourceBundle.getBundle(BUNDLE).getString(key));
            }else{
                mf.applyPattern(ResourceBundle.getBundle(BUNDLE, locale).getString(key));
            }
        }catch(Exception e){
            return key;
        }
        return mf.format(params);
    }
}