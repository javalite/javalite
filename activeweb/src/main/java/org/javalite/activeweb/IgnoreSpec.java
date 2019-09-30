package org.javalite.activeweb;

import org.javalite.app_config.AppConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Created by igor on 4/29/14.
*/
public class IgnoreSpec {
    private List<Pattern> ignorePatterns = new ArrayList<>();
    private String exceptEnvironment;

    IgnoreSpec(String[] ignores){
        for (String ignore : ignores) {
            ignorePatterns.add(Pattern.compile(ignore));
        }
    }

    protected boolean ignores(String path){
        boolean matches = false;
        for (Pattern pattern : ignorePatterns) {
            Matcher m = pattern.matcher(path);
            matches = m.matches();
            if (matches && exceptEnvironment != null
                    && exceptEnvironment.equals(AppConfig.activeEnv())) {
                matches = false; //-- need to ignore
            }
        }
        return matches;
    }

    /**
     * Sets an environment in which NOT TO ignore a URI. Typical use is to process some URIs in
     * development environment, such as compile CSS, or do special image processing. In other environments,
     * this URL will be ignored, given that resource is pre-processed and available from container.
     *
     * @param environment name of environment in which NOT to ignore this URI.
     */
    public void exceptIn(String environment){
        this.exceptEnvironment = environment;
    }
}
