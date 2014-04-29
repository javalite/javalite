package org.javalite.activeweb.controller_filters;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.Runtime.getRuntime;
import static org.javalite.common.Util.read;

/**
 * <p>
 * Controller filter will compile and serve CSS from LESS files.
 * For more information on LESS, please see <a href="http://lesscss.org/">lesscss.org</a>.
 * Usually a developer would configure this filter to trigger and compile in development environment,
 * but the URI to CSS file would be ignored by the framework (configured in RouteConfig), so that a statically
 * compiled version is served by a container or a web server.
 * </p>
 *
 * <p>
 *  This filter does not by itself compile LESS files. It shells out to a <code>lessc</code> compiler, which
 *  needs to be installed.
 * </p>
 *
 * <p>
 *  The filter will only compile LESS files if there have been any changes since the last invocation.
 *  If there has been no changes, it immediately serves the cached version of CSS compiled previously.
 * </p>
 *
 * <h3>Example usage:</h3>
 * <pre>
public class AppControllerConfig extends AbstractControllerConfig {
    public void init(AppContext context) {
        addGlobalFilters(new TimingFilter(),  new LesscFilter("/css/bootstrap.css", "src/main/webapp/less/bootstrap.less"));
    }
}

...

public class RouteConfig extends AbstractRouteConfig {
    public void init(AppContext appContext) {
        ignore("/css/bootstrap.css").exceptIn("development");
    }
}
 * </pre>
 * The line in the <code>RouteConfig</code> ensures that the URI <code>/css/bootstrap.css</code> is ignored by the framework
 * in every environment except "development". This is why the filter is triggering in development environment only.
 *
 * @author igor on 4/28/14
 */
public class LesscFilter extends HttpSupportFilter {

    private String uri;
    private File lessFile;

    /**
     * Use this constructor to configure the filter to compile
     *
     * @param uri - URI pointing to CSS, something like this: <code>/css/main.css</code>. Developers need
     *            to ensure that the actual file <code>/css/main.css</code> will be available in non-development
     *            environment (statically compiled during the build)
     * @param lessFile - this is a main LESS file to compile. Example:
     */
    public LesscFilter(String uri, String lessFile) {
        this.uri = uri;
        this.lessFile = new File(lessFile);
    }

    @Override
    public void before() {
        if (uri().equals(uri)) {
            try {
                respond(css()).contentType("text/css");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized String css() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String hash = (String) appContext().get("hash");

        String freshHash = getDirectoryHash(lessFile.getParentFile().getPath());

        if (hash != null && hash.equals(freshHash)) {
            return (String) appContext().get("css"); // no changes
        } else {
            String css = lessc();
            appContext().set("hash", freshHash);
            appContext().set("css", css);
            return css;
        }
    }

    private String lessc() {
        String css = "blank";
        try {
            Process process = getRuntime().exec("lessc " + lessFile.getPath());
            css = read(process.getInputStream());
            if (process.waitFor() != 0) {
                java.util.Scanner s = new java.util.Scanner(process.getErrorStream()).useDelimiter("\\A");
                css = s.hasNext() ? "/* " + s.next() + "*/" : "/* Unknown LESS Error */";
                logError(css);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logError(e);
            css = "/*" + e.getMessage() + "*/";
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
