package org.javalite.activeweb.freemarker;


import freemarker.template.TemplateBooleanModel;
import org.javalite.common.Convert;
import org.javalite.common.Util;

import java.io.Writer;
import java.util.Map;

/**
 * AWJS tag is for adding aw.js/aw.min.js script to current page.
 * Attributes:
 * <ul>
 * <li><strong>debug</strong> - this is a flag to use compressed (false - default value) or uncompressed (true) javascript source.</li>
 * </ul>
 *
 * @author Andrey Yanchevsky (andrey@javalite.io)
 */
public class AWJSTag extends FreeMarkerTag {

    private static final String DEBUG = "debug";

    private static final String SCRIPT = Util.readResource("/js/aw.min.js");

    private static String source;

    private String getSource() {
        if (source == null) {
            synchronized (this) {
                if (source == null) {
                    source = Util.readResource("/js/aw.js");
                }
            }
        }
        return source;
    }

    @Override
    protected void render(Map params, String body, Writer writer) throws Exception {
        boolean debug = false;
        if (params.containsKey(DEBUG)) {
            Object o = params.get(DEBUG);
            if (o instanceof TemplateBooleanModel) {
                debug = ((TemplateBooleanModel) o).getAsBoolean();
            } else {
                debug = Convert.toBoolean(o);
            }
        }
        writer.write("<script type=\"text/javascript\" defer>"
                + (debug ? getSource() : SCRIPT) + "</script>");
    }
}
