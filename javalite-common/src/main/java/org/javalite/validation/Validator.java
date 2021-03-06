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


package org.javalite.validation;

import java.util.Locale;


public interface Validator {
    /**
     * Called by framework when validation  is required
     * 
     * @param validatable map being validated.
     */
    void validate(Validatable validatable);

    /**
     * Sets an message on this validator.
     *
     * @param message error message.
     */
    void setMessage(String message);

    /**
     * Formats a message with locale.
     *
     * @param locale locale to use.
     * @param params parameters in case a message is parametrized.
     * @return formatted message
     */
    String formatMessage(Locale locale, Object ... params);

    /**
     * Will pin the current message. It will ignore any messages generated duringt validation and
     * will use a current message.
     */
    void pinMessage();
}
