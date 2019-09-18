package org.javalite.activeweb;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class is used to pull messages from a resource bundle called <code>activeweb_messages</code>.
 * It is primarily used by the validation framework, but client code can use it as well for other things.
 *
 * @author Igor Polevoy
 */

public class Messages {

    private static final String BUNDLE = "activeweb_messages";
    
    private Messages() {}

    /**
     * Looks for a localized property/message in <code>activeweb_messages</code> bundle.
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
     * Looks for a property/message in <code>activeweb_messages</code> bundle.
     * Uses Locale supplied by request.
     *
     * @param key key of the property.
     * @param params list of substitution parameters for a message.
     * @return message merged with parameters (if provided), or key if message not found.
     */
    public static String message(String key, Object... params) {
        return getMessage(key, RequestContext.getHttpRequest().getLocale(), params);
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
            mf.applyPattern(key);
        }
        return mf.format(params);
    }
}