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
package org.javalite.activeweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was adapted for ActiveWeb from com.google.gwt.user.server.rpc.RPCServletUtils (GWT SDK 2.2.0)
 * 
 * @author Max Artyukhov
 */
final class RPCControllerUtils { 
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCControllerUtils.class);

    private RPCControllerUtils() {
    }
    /**
     * Package protected for use in tests.
     */
    static final int BUFFER_SIZE = 4096;
    private static final String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * Used both as expected request charset and encoded response charset.
     */
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_ENCODING_GZIP = "gzip";

    private static final String GENERIC_FAILURE_MSG = "The call failed on the server; see server log for details";
    private static final String GWT_RPC_CONTENT_TYPE = "text/x-gwt-rpc";
    /**
     * Controls the compression threshold at and below which no compression will
     * take place.
     */
    private static final int UNCOMPRESSED_BYTE_SIZE_LIMIT = 256;

    /**
     * Returns <code>true</code> if the {@link HttpServletRequest} accepts Gzip
     * encoding. This is done by checking that the accept-encoding header
     * specifies gzip as a supported encoding.
     *    
     * @return <code>true</code> if the {@link HttpServletRequest} accepts Gzip
     *         encoding
     */
    static boolean acceptsGzipEncoding() {
        String acceptEncoding = ContextAccess.getHttpRequest().getHeader(ACCEPT_ENCODING);
        if (null == acceptEncoding) {
            return false;
        }

        return (acceptEncoding.indexOf(CONTENT_ENCODING_GZIP) != -1);
    }

    /**
     * Returns <code>true</code> if the response content's estimated UTF-8 byte
     * length exceeds 256 bytes.
     * 
     * @param content the contents of the response
     * @return <code>true</code> if the response content's estimated UTF-8 byte
     *         length exceeds 256 bytes
     */
    static boolean exceedsUncompressedContentLengthLimit(String content) {
        return (content.length() * 2) > UNCOMPRESSED_BYTE_SIZE_LIMIT;
    }

    /**
     * Returns true if the {@link java.lang.reflect.Method Method} definition on
     * the service is specified to throw the exception contained in the
     * InvocationTargetException or false otherwise. NOTE we do not check that the
     * type is serializable here. We assume that it must be otherwise the
     * application would never have been allowed to run.
     * 
     * @param serviceIntfMethod the method from the RPC request
     * @param cause the exception that the method threw
     * @return true if the exception's type is in the method's signature
     */
    static boolean isExpectedException(Method serviceIntfMethod,
            Throwable cause) {
        assert (serviceIntfMethod != null);
        assert (cause != null);

        Class<?>[] exceptionsThrown = serviceIntfMethod.getExceptionTypes();
        if (exceptionsThrown.length <= 0) {
            // The method is not specified to throw any exceptions
            //
            return false;
        }

        Class<? extends Throwable> causeType = cause.getClass();

        for (Class<?> exceptionThrown : exceptionsThrown) {
            assert (exceptionThrown != null);

            if (exceptionThrown.isAssignableFrom(causeType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the content of an {@link HttpServletRequest} by decoding it using
     * <code>expectedCharSet</code>, or <code>UTF-8</code> if
     * <code>expectedCharSet</code> is <code>null</null>.
     *    
     * @param expectedContentType the expected content (i.e. 'type/subtype' only)
     *          in the Content-Type request header, or <code>null</code> if no
     *          validation is to be performed, and you are willing to allow for
     *          some types of cross type security attacks
     * @param expectedCharSet the expected request charset, or <code>null</code>
     *          if no charset validation is to be performed and <code>UTF-8</code>
     *          should be assumed
     * @return the content of an {@link HttpServletRequest} by decoding it using
     *         <code>expectedCharSet</code>, or <code>UTF-8</code> if
     *         <code>expectedCharSet</code> is <code>null</code>
     * @throws IOException if the request's input stream cannot be accessed, read
     *         from or closed
     * @throws RPCException if the request's content type does not
     *         equal the supplied <code>expectedContentType</code> or
     *         <code>expectedCharSet</code>
     */
    static String readContent(String expectedContentType, String expectedCharSet)
            throws IOException, RPCException {
        if (expectedContentType != null) {
            checkContentTypeIgnoreCase(expectedContentType);
        }
        if (expectedCharSet != null) {
            checkCharacterEncodingIgnoreCase(expectedCharSet);
        }

        /*
         * Need to support 'Transfer-Encoding: chunked', so do not rely on
         * presence of a 'Content-Length' request header.
         */
        InputStream in = ContextAccess.getHttpRequest().getInputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            while (true) {
                int byteCount = in.read(buffer);
                if (byteCount == -1) {
                    break;
                }
                out.write(buffer, 0, byteCount);
            }
            String contentCharSet = expectedCharSet != null
                    ? expectedCharSet : CHARSET_UTF8;
            return out.toString(contentCharSet);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Returns the content of an {@link HttpServletRequest}, after verifying a
     * <code>gwt/x-gwt-rpc; charset=utf-8</code> content type.
     *
     * @return the content of an {@link HttpServletRequest} by decoding it using
     *         <code>UTF-8</code>
     * @throws IOException if the request's input stream cannot be accessed, read
     *         from or closed
     * @throws RPCException if the request's content type is not
     *         <code>gwt/x-gwt-rpc; charset=utf-8</code>, ignoring case
     */
    static String readContentAsGwtRpc()
            throws IOException, RPCException {
        return readContent(GWT_RPC_CONTENT_TYPE, CHARSET_UTF8);
    }

    /**
     * Sets the correct header to indicate that a response is gzipped.
     */
    static void setGzipEncodingHeader() {
        ContextAccess.getHttpResponse().setHeader(CONTENT_ENCODING, CONTENT_ENCODING_GZIP);
    }

    /**
     * Returns <code>true</code> if the request accepts gzip encoding and the the
     * response content's estimated UTF-8 byte length exceeds 256 bytes.
     *
     * @param responseContent a string that will be
     * @return <code>true</code> if the request accepts gzip encoding and the the
     *         response content's estimated UTF-8 byte length exceeds 256 bytes
     */
    static boolean shouldGzipResponseContent(String responseContent) {
        return acceptsGzipEncoding()
                && exceedsUncompressedContentLengthLimit(responseContent);
    }

    /**
     * Write the response content into the {@link HttpServletResponse}. If
     * <code>gzipResponse</code> is <code>true</code>, the response content will
     * be gzipped prior to being written into the response.
     * 
     * @param responseContent a string containing the response content
     * @param gzipResponse if <code>true</code> the response content will be gzip
     *          encoded before being written into the response
     * @throws IOException if reading, writing, or closing the response's output
     *           stream fails
     */
    static void writeResponse(String responseContent, boolean gzipResponse)
            throws IOException {

        byte[] responseBytes = responseContent.getBytes(CHARSET_UTF8);
        if (gzipResponse) {
            // Compress the reply and adjust headers.
            //
            ByteArrayOutputStream output = null;
            GZIPOutputStream gzipOutputStream = null;
            Throwable caught = null;
            try {
                output = new ByteArrayOutputStream(responseBytes.length);
                gzipOutputStream = new GZIPOutputStream(output);
                gzipOutputStream.write(responseBytes);
                gzipOutputStream.finish();
                gzipOutputStream.flush();
                setGzipEncodingHeader();
                responseBytes = output.toByteArray();
            } catch (IOException e) {
                caught = e;
            } finally {
                if (null != gzipOutputStream) {
                    gzipOutputStream.close();
                }
                if (null != output) {
                    output.close();
                }
            }

            if (caught != null) {                
                LOGGER.info("Unable to compress response", caught);
                ContextAccess.setControllerResponse(new InternalErrorResponse());
                return;
            }
        }

        // Send the reply.
        //
        ContextAccess.setControllerResponse(new GWTByteArrayResponse(responseBytes));
    }

    /**
     * Called when the servlet itself has a problem, rather than the invoked
     * third-party method. It writes a simple 500 message back to the client.
     * 
     * @param failure
     */
    static void writeResponseForUnexpectedFailure(Throwable failure) {
        DirectResponse directResponse = new DirectResponse(GENERIC_FAILURE_MSG);
        directResponse.setContentType("text/plain");
        directResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ContextAccess.setControllerResponse(directResponse);
    }

    /**
     * Performs validation of the character encoding, ignoring case.
     *
     * @param expectedCharSet the expected charset of the request
     * @throws RPCException if requests encoding is not <code>null</code> and
     *         does not equal, ignoring case, <code>expectedCharSet</code>
     */
    static void checkCharacterEncodingIgnoreCase(String expectedCharSet)
            throws RPCException {
        boolean encodingOkay = false;
        String characterEncoding = ContextAccess.getHttpRequest().getCharacterEncoding();
        if (characterEncoding != null) {
            /*
             * TODO: It would seem that we should be able to use equalsIgnoreCase here
             * instead of indexOf. Need to be sure that servlet engines return a
             * properly parsed character encoding string if we decide to make this
             * change.
             */
            if (characterEncoding.toLowerCase().indexOf(expectedCharSet.toLowerCase())
                    != -1) {
                encodingOkay = true;
            }
        }

        if (!encodingOkay) {
            throw new RPCException("Character Encoding is '"
                    + (characterEncoding == null ? "(null)" : characterEncoding)
                    + "'.  Expected '" + expectedCharSet + "'");
        }
    }

    /**
     * Performs Content-Type validation of the incoming request, ignoring case
     * and any <code>charset</code> parameter.
     *
     * @param expectedContentType the expected Content-Type for the incoming
     *        request
     * @throws RPCException if the request's content type is not
     *         <code>null</code> and does not, ignoring case, equal
     *         <code>expectedContentType</code>,
     */
    static void checkContentTypeIgnoreCase(String expectedContentType)
            throws RPCException {
        String contentType = ContextAccess.getHttpRequest().getContentType();
        boolean contentTypeIsOkay = false;

        if (contentType != null) {
            contentType = contentType.toLowerCase();
            /*
             * NOTE:We use startsWith because some servlet engines, i.e. Tomcat, do
             * not remove the charset component but others do.
             */
            if (contentType.startsWith(expectedContentType.toLowerCase())) {
                contentTypeIsOkay = true;
            }
        }

        if (!contentTypeIsOkay) {
            throw new RPCException("Content-Type was '"
                    + (contentType == null ? "(null)" : contentType) + "'. Expected '"
                    + expectedContentType + "'.");
        }
    }
}
