package org.javalite.activeweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Igor Polevoy on 12/30/15.
 */
class FileResponse extends ControllerResponse {
    Logger logger = LoggerFactory.getLogger(FileResponse.class);

    private File file;
    private boolean delete = false;

    FileResponse(File file) {
        this.file = file;
    }

    /**
     * @param delete true to delete file after sending to client
     */
    FileResponse(File file, boolean delete) {
        this(file);
        this.delete = delete;
    }

    @Override
    void doProcess() {
        try {
            stream(new FileInputStream(file), Context.getHttpResponse().getOutputStream());
            if (delete) {
                if (!file.delete()) {
                    logger.warn("failed to delete file: " + file + " after processing");
                }
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

}
