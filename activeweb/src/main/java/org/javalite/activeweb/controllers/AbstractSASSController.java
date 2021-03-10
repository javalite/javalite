package org.javalite.activeweb.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.Runtime.getRuntime;
import static org.javalite.common.Util.read;

/**
 * <p>
 * Subclass will compile and serve CSS from SASS files.
 * For more information on SASS, please see <a href="https://sass-lang.com/">https://sass-lang.com/</a>.
 * Usually a developer would subclass this controller to trigger and compile in development environment,
 * but the URI to CSS file would be ignored by the framework (configured in RouteConfig), so that a statically
 * compiled version is served by a container or a web server.
 * </p>
 * <p>
 * This controller does not by itself compile SASS files. It shells out to a <code>sass</code> compiler, which
 * needs to be installed.
 * </p>
 * <p>
 * The controller will only compile SASS files if there have been any changes since the last invocation.
 * If there has been no changes, it immediately serves the cached version of CSS compiled previously.
 * </p>
 * <h3>Example usage:</h3>
 * <pre>
 *
 * public class BootstrapController extends AbstractSASSController {
        protected File getSASSFile() {
            return new File("src/main/webapp/sass/bootstrap.sass");
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
public abstract class AbstractSASSController extends AppController {

    public void index() {
        try {
            respond(css()).contentType("text/css");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Subclass should return a file handle pointing to the main SASS file.
     * @return file handle pointing to the main SASS file
     */
    protected abstract File getSASSFile();


    /**
     * Subclass should override this method if they want to provide custom list of arguments to SASS compiler.
     *
     * @return custom list of arguments for SASS compiler.
     */
    protected String[] getSASSArguments(){
        return null;
    }


    private synchronized String css() throws Exception {
        File sassFile = getSASSFile();
        if (!sassFile.exists()) {
            throw new RuntimeException("File: " + sassFile.getPath() + " does not exist. Current directory: " + new File(".").getCanonicalPath());
        }
        String hash = (String) appContext().get("hash");
        String freshHash = getDirectoryHash(sassFile.getParentFile().getPath());

        if (hash != null && hash.equals(freshHash)) {
            return (String) appContext().get("css"); // no changes
        } else {
            String css = sass(sassFile, getSASSArguments());
            appContext().set("hash", freshHash);
            appContext().set("css", css);
            return css;
        }
    }



    private String sass(File sassFile, String... arguments) throws IOException, InterruptedException {

        String command = "sass";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            command += ".cmd";
        }

        String[] commandArray;
        if (arguments != null) {
            logInfo("Executing: " + "sass " + Util.join(arguments, " ") + " " + sassFile.getPath());
            commandArray = new String[arguments.length + 2];
            commandArray[0] = command;
            System.arraycopy(arguments, 0, commandArray, 1, arguments.length);
            commandArray[commandArray.length - 1] = sassFile.getPath();
        }else{
            logInfo("Executing: " + "sass "  + sassFile.getPath());
            commandArray = new String[2];
            commandArray[0] = command;
            commandArray[1] = sassFile.getPath();
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
