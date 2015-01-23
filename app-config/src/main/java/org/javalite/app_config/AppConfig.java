package org.javalite.app_config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Allows configuration of applications that is specific for different deployment environments.
 * <p/>
 * Applications could have environment-specific files, whose names follow this pattern:
 * <code>environment.properties</code>, where <code>environment</code> is a name of a deployment environment, such as "development",
 * "staging", "production", etc.

 * You can also provide a global file, properties from which will be loaded in all environments: <code>global.properties</code>.
 * <p/>
 * In all cases the files need to be on the classpath under directory/package <code>/app_config</code>.
 * <p/>
 * Environment-specific file will have an "environment" part of the file name match to an environment variable called "ACTIVE_ENV".
 * Such configuration is easy to achieve in Unix shell:
 * <p/>
 * <code>
 * export ACTIVE_ENV=test
 * </code>
 * <p/>
 * If environment variable <code>ACTIVE_ENV</code> is missing, it defaults to "development".
 *
 * <p>
 *     You can also provide an environment as a system property <code>active_env</code>. System property overrides environment
 *     variable <code>ACTIVE_ENV</code>
 * </p>
 *
 * @author Igor Polevoy
 */
public class AppConfig implements Map<String, String> {

    private static Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private static HashMap<String, Property> props = new HashMap<>();
    private static HashMap<String, String> plainProps = new HashMap<>();

    public static synchronized void init() {
        if (isInited()) return;

        try {
            URL globalUrl = AppConfig.class.getResource("/app_config/global.properties");
            if (globalUrl != null) {
                registerProperties(globalUrl);
            }

            //get env - specific file, first from a system property, than from env var.
            String  activeEnv = System.getProperty("active_env");
            if(activeEnv == null){
                activeEnv = System.getenv("ACTIVE_ENV");
            }

            if (activeEnv == null) {
                LOGGER.warn("Environment variable 'ACTIVE_ENV' not found, defaulting to 'development'");
                activeEnv = "development";
            }

            String file = "/app_config/" + activeEnv + ".properties";
            URL url = AppConfig.class.getResource(file);
            if (url == null) {
                LOGGER.warn("Property file not found: '" + file + "'");
            } else {
                registerProperties(url);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static boolean isInited() {
        return !(props.isEmpty());
    }

    private static void registerProperties(URL url) {

        LOGGER.info("Registering properties from: " + url.getPath());

        Properties temp = new Properties();
        try {
            temp.load(url.openStream());
        } catch (IOException e) {
            throw new ConfigInitException(e);
        }
        Enumeration keys = temp.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = temp.getProperty(key);
            Property property = new Property(key, value, url.getPath());

            Property previous = props.put(key, property);
            plainProps.put(key, value);
            if (previous != null) {
                LOGGER.warn("\n************************************************************\n"
                        + "Duplicate property defined. Property: '" + key + "' found in files: \n"
                        + previous.getPropertyFile() + ", \n" + url.getPath()
                        + "\nUsing value '" + property.getValue() + "' from:\n"
                        + property.getPropertyFile()
                        + "\n************************************************************");
            }
        }
    }

    /**
     * Returns property instance corresponding to key.
     *
     * @param key key for property.
     * @return Property for this key.
     */
    public static Property getAsProperty(String key) {
        if (!isInited()) {
            init();
        }
        return props.get(key);
    }

    /**
     * Returns property value for a key.
     *
     * @param key key of property.
     * @return value for this key, <code>null</code> if not found.
     */
    public static String getProperty(String key) {
        if (!isInited()) {
            init();
        }
        Property p = props.get(key);
        return p == null ? null : p.getValue();
    }


    /**
     * Gets property, synonym for {@link #getProperty(String)}.
     *
     * @param key key of property
     * @return property value
     */
    public static String p(String key) {
        return getProperty(key);
    }


    public static Map<String, String> getAllProperties() {
        if (!isInited()) {
            init();
        }
        return plainProps;
    }


    /////////// Implementation of Map interface below ///////////////////


    public AppConfig() {
        init();
    }

    @Override
    public int size() {
        return props.size();
    }

    @Override
    public boolean isEmpty() {
        return props.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return props.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return props.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return p(key.toString());
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }

    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException("Operation not supported, not a real map");
    }
}
