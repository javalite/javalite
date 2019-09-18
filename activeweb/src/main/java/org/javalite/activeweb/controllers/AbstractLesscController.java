package org.javalite.activeweb.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.common.RuntimeUtil;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Runtime.getRuntime;
import static org.javalite.common.Collections.list;
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


    /**
     * Subclass should override this method if they want to provide custom list of arguments to LessC compiler.
     *
     * @return custom list of arguments for LessC compiler.
     */
    protected String[] getLesscArguments(){
        return null;
    }


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
            String css = lessc(lessFile, getLesscArguments());
            appContext().set("hash", freshHash);
            appContext().set("css", css);
            return css;
        }
    }



    private String lessc(File lessFile, String... arguments) throws IOException, InterruptedException {

        String command = "lessc";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            command += ".cmd";
        }

        String[] commandArray;
        if (arguments != null) {
            logInfo("Executing: " + "lessc " + Util.join(arguments, " ") + " " + lessFile.getPath());
            commandArray = new String[arguments.length + 2];
            commandArray[0] = command;
            System.arraycopy(arguments, 0, commandArray, 1, arguments.length);
            commandArray[commandArray.length - 1] = lessFile.getPath();
        }else{
            logInfo("Executing: " + "lessc "  + lessFile.getPath());
            commandArray = new String[2];
            commandArray[0] = command;
            commandArray[1] = lessFile.getPath();
        }

        Process process = getRuntime().exec(commandArray);
        String css = read(process.getInputStream(), "UTF-8");
        String error = read(process.getErrorStream(), "UTF-8");
        if (process.waitFor() != 0) {
            throw new RuntimeException(error);
        }
        return css;
    }

    private String getDirectoryHash(String directory) throws NoSuchAlgorithmException {
        StringBuilder code = new StringBuilder();
        File[] list = new File(directory).listFiles();
        if (list == null)
            return null;

        for (File f : list) {
            if (f.isDirectory()) {
                code.append(f.getName()).append(f.length()).append(f.lastModified()).append(getDirectoryHash(f.getAbsolutePath()));
            } else {
                code.append(f.getName()).append(f.length()).append(f.lastModified());
            }
        }
        return new String(MessageDigest.getInstance("MD5").digest(code.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
