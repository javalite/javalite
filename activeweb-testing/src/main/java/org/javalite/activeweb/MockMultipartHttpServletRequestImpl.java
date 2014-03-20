/*
Copyright 2009-2010 Igor Polevoy 

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

import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Igor Polevoy
 */
public class MockMultipartHttpServletRequestImpl extends MockHttpServletRequest implements AWMockMultipartHttpServletRequest{

    private List<FormItem> formItems = new ArrayList<FormItem>();

    public void addFormItem(FormItem item) {
        formItems.add(item);
    }

    public Iterator<FormItem> getFormItemIterator() {
        return formItems.iterator();
    }

    @Override
    public List<FormItem> getFormItems() {
        return formItems;
    }
}
