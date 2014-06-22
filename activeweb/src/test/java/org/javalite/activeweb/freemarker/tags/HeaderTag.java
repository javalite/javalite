package org.javalite.activeweb.freemarker.tags;

import org.javalite.activeweb.freemarker.FreeMarkerTag;

import java.io.Writer;
import java.util.Map;

/**
 * @author igor on 6/22/14.
 */
public class HeaderTag extends FreeMarkerTag {
    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {
        writer.write("...and the header message is: " + header("message"));
    }
}
