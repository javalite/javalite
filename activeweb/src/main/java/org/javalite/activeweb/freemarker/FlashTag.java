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
package org.javalite.activeweb.freemarker;


import freemarker.core.Environment;
import freemarker.template.*;
import org.javalite.activeweb.ViewException;

import java.io.IOException;
import java.util.Map;

import static org.javalite.common.Util.blank;

/**
 * This tag allows to display a message on a page that lives for one more request.
 * For more information, please refer to <a href="http://javalite.io/flash_tag">FlashTag</a>.
 *
 * @author Igor Polevoy
 */
public class FlashTag implements TemplateDirectiveModel {

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        try {

            /**
             * ******  Use case 1 ******
             *
             * //Send flash message:
             * flash("message", "Your changes were saved");
             *
             * Display flash data:
             * <@flash name="message"/>
             *
             *
             *
             * ******  Use case 2  ******
             *
             * flash("warning");
             *
             * Display flash data:
             * <@flash name="warning">
             *  <div>this is a warning body</div>
             * </@flash>
             *
             *
             *  ****** Use case three ****
             *  flash()
             *
             *  Display:
             *  <@flash> body here <@flash>
             *
             */
            SimpleHash flasher = (SimpleHash) env.getVariable("flasher");

            if (flasher != null) {
                if (blank(body)) {  // case one: flash("warning", "warning message") ==>>  <@flash name="warning"/>
                    Util.validateParamsPresence(params, "name");
                    Object flashMessage = flasher.get(params.get("name").toString());
                    if (flashMessage != null) {
                        env.getOut().write(flashMessage.toString());
                    }
                } else if (!blank(body) && params.containsKey("name") // case two: flash("warning") ==>>  <@flash name="warning"> body here <@flash>
                        && flasher.containsKey(params.get("name").toString())) {
                    body.render(env.getOut());
                } else if (!blank(body) && !params.containsKey("name")) { //case three: flash() ==>>  <@flash> body here <@flash>
                    body.render(env.getOut());
                } else {
                    //ignore

                }
            }
        }catch(ViewException e){
            throw e;
        }
        catch (Exception e) {
            throw new ViewException(e);
        }
    }
}