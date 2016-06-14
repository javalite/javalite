/*
Copyright 2009-2016 Igor Polevoy

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

/**
 * @author Igor Polevoy: 12/29/13 1:14 AM
 */

package org.javalite.db_migrator;

import org.apache.maven.plugin.logging.Log;

public class MockLog implements Log {

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void debug(CharSequence charSequence) {

        }

        @Override
        public void debug(CharSequence charSequence, Throwable throwable) {

        }

        @Override
        public void debug(Throwable throwable) {

        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void info(CharSequence charSequence) {

        }

        @Override
        public void info(CharSequence charSequence, Throwable throwable) {

        }

        @Override
        public void info(Throwable throwable) {

        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void warn(CharSequence charSequence) {

        }

        @Override
        public void warn(CharSequence charSequence, Throwable throwable) {

        }

        @Override
        public void warn(Throwable throwable) {

        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void error(CharSequence charSequence) {

        }

        @Override
        public void error(CharSequence charSequence, Throwable throwable) {

        }

        @Override
        public void error(Throwable throwable) {

        }
    }
