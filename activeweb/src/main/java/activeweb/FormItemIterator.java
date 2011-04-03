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

package activeweb;

import org.apache.commons.fileupload.FileItemIterator;

import java.util.Iterator;

/**
 * @author Igor Polevoy
 */
class FormItemIterator implements Iterator<FormItem> {
    private FileItemIterator it;

    public FormItemIterator(FileItemIterator it) {
        this.it = it;
    }

    public boolean hasNext() {
        try {
            return it.hasNext();
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public FormItem next() {
        try {
            return new FormItem(it.next());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}