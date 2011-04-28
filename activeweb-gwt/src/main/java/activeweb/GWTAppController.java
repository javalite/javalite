/*
Copyright 2010-2011 Max Artyukhov

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package activeweb;


import activeweb.annotations.POST;

import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.gwt.user.client.rpc.RpcRequestBuilder.STRONG_NAME_HEADER;

/**
 * This class was adapted for ActiveWeb from com.google.gwt.user.server.rpc.RemoteServiceServlet (GWT SDK 2.2.0)
 * 
 * @author Max Artyukhov
 */
public class GWTAppController extends AppController implements SerializationPolicyProvider {    

    @POST
    public void index() {
        try {
            processPost();
        } catch (Throwable e) {                        
            // Give a subclass a chance to either handle the exception or rethrow it
            //
            doUnexpectedFailure(e);
        }
    }

    /**
     * Override this method to control what should happen when an exception
     * escapes the {@link #doPost} method. The default implementation will log the
     * failure and send a generic failure response to the client.
     * <p>
     * An "expected failure" is an exception thrown by a service method that is
     * declared in the signature of the service method. These exceptions are
     * serialized back to the client, and are not passed to this method. This
     * method is called only for exceptions or errors that are not part of the
     * service method's signature, or that result from SecurityExceptions,
     * SerializationExceptions, or other failures within the RPC framework.
     * <p>
     * Note that if the desired behavior is to both send the GENERIC_FAILURE_MSG
     * response AND to rethrow the exception, then this method should first send
     * the GENERIC_FAILURE_MSG response itself (using getThreadLocalResponse), and
     * then rethrow the exception. Rethrowing the exception will cause it to
     * escape into the servlet container.
     *
     * @param e the exception which was thrown
     */
    void doUnexpectedFailure(Throwable e) {
        try {
            ContextAccess.getHttpResponse().reset();
        } catch (IllegalStateException ex) {
            /*
             * If we can't reset the request, the only way to signal that something
             * has gone wrong is to throw an exception from here. It should be the
             * case that we call the user's implementation code before emitting data
             * into the response, so the only time that gets tripped is if the object
             * serialization code blows up.
             */
            throw new RuntimeException("Unable to report failure", e);
        }
        
        logError(e.getMessage(), e);        
        RPCControllerUtils.writeResponseForUnexpectedFailure(e);
    }

    /**
     * Returns the strong name of the permutation, as reported by the client that
     * issued the request, or <code>null</code> if it could not be determined.
     * This information is encoded in the
     * {@value com.google.gwt.user.client.rpc.RpcRequestBuilder#STRONG_NAME_HEADER}
     * HTTP header.
     */
    private String getPermutationStrongName() {
        return ContextAccess.getHttpRequest().getHeader(STRONG_NAME_HEADER);
    }            

    /**
     * Override this method in order to control the parsing of the incoming
     * request. For example, you may want to bypass the check of the Content-Type
     * and character encoding headers in the request, as some proxies re-write the
     * request headers. Note that bypassing these checks may expose the servlet to
     * some cross-site vulnerabilities. Your implementation should comply with the
     * HTTP/1.1 specification, which includes handling both requests which include
     * a Content-Length header and requests utilizing <code>Transfer-Encoding:
     * chuncked</code>.
     *
     * @param request the incoming request
     * @return the content of the incoming request encoded as a string.
     */
    private String readContent() throws  IOException, RPCException {
        return RPCControllerUtils.readContentAsGwtRpc();
    }

    /**
     * Used by HybridServiceServlet.
     */
    private SerializationPolicy loadSerializationPolicy(String moduleBaseURL, String strongName) {
        // The request can tell you the path of the web app relative to the
        // container root.
        String contextPath = ContextAccess.getHttpRequest().getContextPath();

        String modulePath = null;
        if (moduleBaseURL != null) {
            try {
                modulePath = new URL(moduleBaseURL).getPath();
            } catch (MalformedURLException ex) {
                // log the information, we will default
                logError("Malformed moduleBaseURL: " + moduleBaseURL, ex);
            }
        }

        SerializationPolicy serializationPolicy = null;

        /*
         * Check that the module path must be in the same web app as the servlet
         * itself. If you need to implement a scheme different than this, override
         * this method.
         */
        if (modulePath == null || !modulePath.startsWith(contextPath)) {
            String message = "ERROR: The module path requested, "
                    + modulePath
                    + ", is not in the same web application as this servlet, "
                    + contextPath
                    + ".  Your module may not be properly configured or your client and server code maybe out of date.";
            logError(message);
        } else {
            // Strip off the context path from the module base URL. It should be a
            // strict prefix.
            String contextRelativePath = modulePath.substring(contextPath.length());

            String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(contextRelativePath
                    + strongName);

            // Open the RPC resource file and read its contents.
            InputStream is = ContextAccess.getFilterConfig().getServletContext().getResourceAsStream(
                    serializationPolicyFilePath);
            try {
                if (is != null) {
                    try {
                        serializationPolicy = SerializationPolicyLoader.loadFromStream(is,
                                null);
                    } catch (ParseException e) {
                        logError("ERROR: Failed to parse the policy file '"
                                + serializationPolicyFilePath + "'", e);
                    } catch (IOException e) {
                        logError("ERROR: Could not read the policy file '"
                                + serializationPolicyFilePath + "'", e);
                    }
                } else {
                    String message = "ERROR: The serialization policy file '"
                            + serializationPolicyFilePath
                            + "' was not found; did you forget to include it in this deployment?";
                    logError(message);
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // Ignore this error
                    }
                }
            }
        }

        return serializationPolicy;
    }
    /**
     * A cache of moduleBaseURL and serialization policy strong name to
     * {@link SerializationPolicy}.
     */
    private final Map<String, SerializationPolicy> serializationPolicyCache = new HashMap<String, SerializationPolicy>();

    public final SerializationPolicy getSerializationPolicy(String moduleBaseURL,
            String strongName) {

        SerializationPolicy serializationPolicy = getCachedSerializationPolicy(
                moduleBaseURL, strongName);
        if (serializationPolicy != null) {
            return serializationPolicy;
        }

        serializationPolicy = doGetSerializationPolicy(moduleBaseURL, strongName);

        if (serializationPolicy == null) {
            // Failed to get the requested serialization policy; use the default
            logWarning(
                    "WARNING: Failed to get the SerializationPolicy '"
                    + strongName
                    + "' for module '"
                    + moduleBaseURL
                    + "'; a legacy, 1.3.3 compatible, serialization policy will be used.  You may experience SerializationExceptions as a result.");
            serializationPolicy = RPC.getDefaultSerializationPolicy();
        }

        // This could cache null or an actual instance. Either way we will not
        // attempt to lookup the policy again.
        putCachedSerializationPolicy(moduleBaseURL, strongName, serializationPolicy);

        return serializationPolicy;
    }

    /**
     * Process a call originating from the given request. Uses the
     * {@link RPC#invokeAndEncodeResponse(Object, java.lang.reflect.Method, Object[])}
     * method to do the actual work.
     * <p>
     * Subclasses may optionally override this method to handle the payload in any
     * way they desire (by routing the request to a framework component, for
     * instance). The {@link HttpServletRequest} and {@link HttpServletResponse}
     * can be accessed via the {@link #getThreadLocalRequest()} and
     * {@link #getThreadLocalResponse()} methods.
     * </p>
     * This is public so that it can be unit tested easily without HTTP.
     *
     * @param payload the UTF-8 request payload
     * @return a string which encodes either the method's return, a checked
     *         exception thrown by the method, or an
     *         {@link IncompatibleRemoteServiceException}
     * @throws SerializationException if we cannot serialize the response
     * @throws UnexpectedException if the invocation throws a checked exception
     *           that is not declared in the service method's signature
     * @throws RuntimeException if the service method throws an unchecked
     *           exception (the exception will be the one thrown by the service)
     */
    private String processCall(String payload) throws SerializationException {
        // First, check for possible XSRF situation
        checkPermutationStrongName();

        try {
            RPCRequest rpcRequest = RPC.decodeRequest(payload, this.getClass(), this);            
            return RPC.invokeAndEncodeResponse(this, rpcRequest.getMethod(),
                    rpcRequest.getParameters(), rpcRequest.getSerializationPolicy(),
                    rpcRequest.getFlags());
        } catch (IncompatibleRemoteServiceException ex) {
            logError(
                    "An IncompatibleRemoteServiceException was thrown while processing this call.",
                    ex);
            return RPC.encodeResponseForFailure(null, ex);
        } catch (RpcTokenException tokenException) {
            logError("An RpcTokenException was thrown while processing this call.",
                    tokenException);
            return RPC.encodeResponseForFailure(null, tokenException);
        }
    }

    /**
     * Standard HttpServlet method: handle the POST.
     *
     * This doPost method swallows ALL exceptions, logs them in the
     * ServletContext, and returns a GENERIC_FAILURE_MSG response with status code
     * 500.
     *     
     * @throws SerializationException
     */    
    private final void processPost() throws IOException, 
            RPCException, SerializationException {
        // Read the request fully.
        //
        String requestPayload = readContent();        

        // Invoke the core dispatching logic, which returns the serialized
        // result.
        //
        String responsePayload = processCall(requestPayload);        

        // Write the response.
        //
        writeResponse(responsePayload);
    }

    /**
     * This method is called by {@link #processCall(String)} and will throw a
     * SecurityException if {@link #getPermutationStrongName()} returns
     * <code>null</code>. This method can be overridden to be a no-op if there are
     * clients that are not expected to provide the
     * {@value com.google.gwt.user.client.rpc.RpcRequestBuilder#STRONG_NAME_HEADER}
     * header.
     *
     * @throws SecurityException if {@link #getPermutationStrongName()} returns
     *           <code>null</code>
     */
    private void checkPermutationStrongName() throws SecurityException {
        if (getPermutationStrongName() == null) {
            throw new SecurityException(
                    "Blocked request without GWT permutation header (XSRF attack?)");
        }
    }

    /**
     * Gets the {@link SerializationPolicy} for given module base URL and strong
     * name if there is one.
     *
     * Override this method to provide a {@link SerializationPolicy} using an
     * alternative approach.
     *
     * @param request the HTTP request being serviced
     * @param moduleBaseURL as specified in the incoming payload
     * @param strongName a strong name that uniquely identifies a serialization
     *          policy file
     * @return a {@link SerializationPolicy} for the given module base URL and
     *         strong name, or <code>null</code> if there is none
     */
    private SerializationPolicy doGetSerializationPolicy(String moduleBaseURL, String strongName) {
        return loadSerializationPolicy(moduleBaseURL, strongName);
    }    

    /**
     * Determines whether the response to a given servlet request should or should
     * not be GZIP compressed. This method is only called in cases where the
     * requester accepts GZIP encoding.
     * <p>
     * This implementation currently returns <code>true</code> if the response
     * string's estimated byte length is longer than 256 bytes. Subclasses can
     * override this logic.
     * </p>
     *
     * @param request the request being served
     * @param response the response that will be written into
     * @param responsePayload the payload that is about to be sent to the client
     * @return <code>true</code> if responsePayload should be GZIP compressed,
     *         otherwise <code>false</code>.
     */
    private boolean shouldCompressResponse(String responsePayload) {
        return RPCControllerUtils.exceedsUncompressedContentLengthLimit(responsePayload);
    }

    private SerializationPolicy getCachedSerializationPolicy(
            String moduleBaseURL, String strongName) {
        synchronized (serializationPolicyCache) {
            return serializationPolicyCache.get(moduleBaseURL + strongName);
        }
    }

    private void putCachedSerializationPolicy(String moduleBaseURL,
            String strongName, SerializationPolicy serializationPolicy) {
        synchronized (serializationPolicyCache) {
            serializationPolicyCache.put(moduleBaseURL + strongName,
                    serializationPolicy);
        }
    }

    private void writeResponse(String responsePayload) throws IOException {
        boolean gzipEncode = RPCControllerUtils.acceptsGzipEncoding()
                && shouldCompressResponse(responsePayload);
        
        RPCControllerUtils.writeResponse(responsePayload, gzipEncode);
    }    
}

