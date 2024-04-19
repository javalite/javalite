package org.javalite.db_migrator;

import org.javalite.common.Templator;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.javalite.common.Util.blank;
import static org.javalite.db_migrator.DbUtils.exec;
import static org.javalite.db_migrator.MigrationManager.MIGRATION_LOGGER;


public class SQLMigration extends Migration {

    private static final String DEFAULT_DELIMITER = ";";
    private static final String DELIMITER_KEYWORD = "DELIMITER";
    private static final String[] COMMENT_CHARS = new String[]{"--", "#", "//"};

    public SQLMigration(String version, String fileName, String migrationContent, Properties mergeProperties) {
        super(version, fileName, migrationContent, mergeProperties);
    }


    void migrate() {
        try {

            MIGRATION_LOGGER.info("Reading file {}", getFileName());

            String[] lines = Util.split(getMigrationContent(), System.getProperty("line.separator"));
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
                exec(mergeProperties == null ? statement : Templator.mergeFromTemplate(statement, mergeProperties, false));
            }
        } catch (Exception e) {
            MIGRATION_LOGGER.error("Error executing migration file: {}", getFileName(), e);
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
