/*
Copyright 2009-2018 Igor Polevoy

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

package org.javalite.activejdbc;

import org.javalite.instrumentation.DBParameters;
import org.javalite.instrumentation.InstrumentationException;
import org.javalite.instrumentation.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Andrey Yanchevsky
 */
public class StaticMetadataGenerator {

    private List<DBParameters> dbParameters;

    public StaticMetadataGenerator() {

    }

    public void setDBParameters(List<DBParameters> dbParameters) {
        this.dbParameters = dbParameters;
    }

    public void generate(String outputDirectory) {

        Path metadataPath = Paths.get(outputDirectory, "activejdbc_metadata.json");

        Logger.info("Static metadata: " + metadataPath.toString());

        RegistryProxy registry = new RegistryProxy();

        DBProxy dbProxy = null;

        try {

            Files.deleteIfExists(metadataPath);

            if (getClass().getResource("/database.properties") != null) {
                dbProxy = new DBProxy().open();
                registry.init(dbProxy.name());
                dbProxy.close();
            } else {
                Logger.info("File database.properties not found");
            }

            if (dbParameters != null) {
                for(DBParameters parameters : dbParameters) {
                    dbProxy = new DBProxy(parameters.getName())
                            .open(
                                    parameters.getDriver(),
                                    parameters.getUrl().toString(),
                                    parameters.getUsername(),
                                    parameters.getPassword()
                            );
                    registry.init(parameters.getName());
                    dbProxy.close();
                }
            }

            Files.write(metadataPath, registry.toJSON().getBytes());

        } catch(Throwable e) {
            throw new InstrumentationException(e);
        } finally {
            if (dbProxy != null) {
                dbProxy.close();
            }
        }

    }

}
