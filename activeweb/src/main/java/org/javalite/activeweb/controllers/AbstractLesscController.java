package org.javalite.activeweb.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.Runtime.getRuntime;
import static org.javalite.common.Util.read;

/**
 * <p>
 * Subclass will compile and serve CSS from LESS files.
 * For more information on LESS, please see <a href="http://lesscss.org/">lesscss.org</a>.
 * Usually a developer would subclass this controller to trigger and compile in development environment,
 * but the URI to CSS file would be ignored by the framework (configured in RouteConfig), so that a statically
 * compiled version is served by a container or a web server.
 * </p>
 * <p>
 * This controller does not by itself compile LESS files. It shells out to a <code>lessc</code> compiler, which
 * needs to be installed.
 * </p>
 * <p>
 * The controller will only compile LESS files if there have been any changes since the last invocation.
 * If there has been no changes, it immediately serves the cached version of CSS compiled previously.
 * </p>
 * <h3>Example usage:</h3>
 * <pre>
 *
 * public class BootstrapController extends AbstractLesscController {
        protected File getLessFile() {
            return new File("src/main/webapp/less/bootstrap.less");
        }
    }
 ...

   public class RouteConfig extends AbstractRouteConfig {
      public void init(AppContext appContext) {
           ignore("/bootstrap.css").exceptIn("development");
      }
 * }
 * </pre>
 * The line in the <code>RouteConfig</code> ensures that the URI <code>/bootstrap.css</code> is ignored by the framework
 * in every environment except "development". This is why the controller is triggering in development environment only.
 *
 * @author igor on 4/28/14
 */
public abstract class AbstractLesscController extends AppController {

    public void index() {
        try {
            respond(css()).contentType("text/css");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Subclass should return a file handle pointing to the main Less file.
     * @return file handle pointing to the main Less file
     */
    protected abstract File getLessFile();

    private synchronized String css() throws Exception {
        File lessFile = getLessFile();
        if (!lessFile.exists()) {
            throw new RuntimeException("File: " + lessFile.getPath() + " does not exist. Current directory: " + new File(".").getCanonicalPath());
        }
        String hash = (String) appContext().get("hash");
        String freshHash = getDirectoryHash(lessFile.getParentFile().getPath());

        if (hash != null && hash.equals(freshHash)) {
            return (String) appContext().get("css"); // no changes
        } else {
            String css = lessc(lessFile);
            appContext().set("hash", freshHash);
            appContext().set("css", css);
            return css;
        }
    }

    public String lessc(File lessFile) throws IOException, InterruptedException {
        logInfo("Executing: " + "lessc " + lessFile.getPath());
        String exec = "lessc";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            exec += ".cmd";
        }
        Process process = getRuntime().exec(new String[]{exec, lessFile.getPath()});
        String css = read(process.getInputStream(), "UTF-8");
        String error = read(process.getErrorStream(), "UTF-8");
        if (process.waitFor() != 0) {
            throw new RuntimeException(error);
        }
        return css;
    }

    private String getDirectoryHash(String directory) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String code = "";
        File[] list = new File(directory).listFiles();
        if (list == null)
            return null;

        for (File f : list) {
            if (f.isDirectory()) {
                code += f.getName() + f.length() + f.lastModified() + getDirectoryHash(f.getAbsolutePath());
            } else {
                code += f.getName() + f.length() + f.lastModified();
            }
        }
        return new String(MessageDigest.getInstance("MD5").digest(code.getBytes("UTF-8")));
    }
}
