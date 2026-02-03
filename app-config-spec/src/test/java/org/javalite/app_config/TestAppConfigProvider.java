package org.javalite.app_config;

import java.util.HashMap;
import java.util.Map;

/**
 * Test implementation of AppConfigProvider for unit testing.
 */
public class TestAppConfigProvider implements AppConfigProvider {
    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("provider.property", "provider_value");
        properties.put("first.name", "ProviderName");
        return properties;
    }
}
