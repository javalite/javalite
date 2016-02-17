package org.javalite.lessc.maven;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Igor Polevoy on 5/22/15.
 */
public class LessConfig {
    private String lesscMain, targetDirectory, targetFileName, lesscArguments;

    public String getLesscMain() {
        return lesscMain;
    }

    public void setLesscMain(String lesscMain) {
        this.lesscMain = lesscMain;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public String getLesscArguments() {
        return lesscArguments;
    }

    public void setLesscArguments(String lesscArguments) {
        this.lesscArguments = lesscArguments;
    }
}
