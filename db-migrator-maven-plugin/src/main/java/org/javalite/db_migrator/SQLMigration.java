package org.javalite.db_migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.common.Util.blank;
import static org.javalite.db_migrator.DbUtils.exec;


public class SQLMigration extends Migration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLMigration.class);
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DELIMITER_KEYWORD = "DELIMITER";
    private static final String[] COMMENT_CHARS = new String[]{"--", "#", "//"};

    public SQLMigration(String version, File migrationFile) {

        super(version, migrationFile);
    }


    void migrate(String encoding) {
        try {

            String path = getMigrationFile().getCanonicalPath();
            Charset charset = encoding != null ? Charset.forName(encoding) : Charset.defaultCharset();

            LOGGER.info("Reading file {} using charset: {} ", path, charset);

            List<String> lines = Files.readAllLines(Paths.get(path), charset);
            String delimiter = DEFAULT_DELIMITER;
            List<String> statements = new ArrayList<>();

            StringBuilder currentStatement = new StringBuilder();
            for (String line : lines) {
                line = line.trim();
                if (!commentLine(line) && !blank(line)) {
                    if (line.startsWith(DELIMITER_KEYWORD)) {
                        delimiter = line.substring(10).trim();
                    } else if (line.endsWith(delimiter)) {
                        currentStatement.append(line.substring(0, line.length() - delimiter.length()));
                        if (!blank(currentStatement.toString())) {
                            statements.add(currentStatement.toString());
                        }
                        currentStatement = new StringBuilder();
                    } else {
                        currentStatement.append(line).append(System.getProperty("line.separator"));
                    }
                }
            }

            if (!blank(currentStatement.toString())) {
                statements.add(currentStatement.toString());
            }

            for (String statement : statements) {
                exec(statement);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing migration file: {}", getMigrationFile().toString(), e);
            throw new MigrationException(e);
        }
    }

    private boolean commentLine(String line) {
        for (String cc : COMMENT_CHARS) {
            if (line.trim().startsWith(cc)) {
                return true;
            }
        }
        return false;
    }

}
