package org.javalite.sass.maven;

/**
 * @author Igor Polevoy on 5/22/15.
 */
public class SassConfig {
    private String sassMain, targetDirectory, targetFileName, sassArguments;

    public String getSassMain() {
        return sassMain;
    }

    public void setSassMain(String sassMain) {
        this.sassMain = sassMain;
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

    public String getSassArguments() {
        return sassArguments;
    }

    public void setSassArguments(String lesscArguments) {
        this.sassArguments = lesscArguments;
    }
}
