package org.javalite.app_config;

import java.util.Map;

public interface AppConfigProvider {
    Map<String, String> getProperties();
}
