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

import java.io.ByteArrayInputStream;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Max Artyukhov
 */
public class GWTByteArrayResponse extends StreamResponse {
    
    private static final String CONTENT_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String ATTACHMENT = "attachment";
    
    private byte[] responseBytes;
    
    public GWTByteArrayResponse(byte[] responseBytes) {
        super(new ByteArrayInputStream(responseBytes));
        this.responseBytes = responseBytes;
    }

    @Override
    void doProcess() {
        ContextAccess.getHttpResponse().setContentLength(responseBytes.length);
        setContentType(CONTENT_TYPE_APPLICATION_JSON_UTF8);
        setStatus(HttpServletResponse.SC_OK);
        ContextAccess.getHttpResponse().setHeader(CONTENT_DISPOSITION, ATTACHMENT);
        super.doProcess();
    }
    
}
