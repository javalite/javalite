package org.javalite.db_migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.javalite.db_migrator.DbUtils.exec;


public class Migration implements Comparable<Migration> {
    private static final Logger logger = LoggerFactory.getLogger(Migration.class);
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DEFAULT_DELIMITER_KEYWORD = "DELIMITER";

    private final File migrationFile;
    private final String version;

    public Migration(String version, File migrationFile) {
        this.migrationFile = migrationFile;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return migrationFile.getName();
    }

    public static String read(InputStream in, String charset) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("input stream cannot be null");
        }
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in, charset);
            char[] buffer = new char[1024];
            StringBuilder sb = new StringBuilder();
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            return sb.toString();
        } finally {
            assert reader != null;
            reader.close();
        }
    }


    public void migrate(String encoding) throws Exception {

        //TODO: I must say this needs to be refactored.
        try {
            String migration = read(new FileInputStream(migrationFile), encoding == null ? "UTF-8" : encoding);
            if (migration.contains("BLOCK")) {
                processAsBlocks(migration);
            } else {
                processLineByLine(encoding);
            }
        } catch (Exception e) {
            throw new MigrationException(e);
        }
    }

    private void processLineByLine(String encoding) throws IOException {
        StringBuilder command = null;

        String delimiter = DEFAULT_DELIMITER;
        LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(new FileInputStream(migrationFile), encoding == null ? "UTF-8" : encoding));
        String line;
        while ((line = lineReader.readLine()) != null) {
            if (command == null) {
                command = new StringBuilder();
            }

            line = line.trim(); // Strip extra whitespace too?

            if (line.length() < 1) {
                // Do nothing, it's an empty line.
            } else if (commentLine(line)) {
                logger.debug(line);
            } else {
                if (startsWithIgnoreCase(line, DEFAULT_DELIMITER_KEYWORD)) {
                    delimiter = line.substring(9).trim();
                } else if ((command.length() == 0) && startsWithIgnoreCase(line, "create ") && containsIgnoreCase(line, " as ")) {
                    delimiter = line.substring(line.toLowerCase().lastIndexOf(" as ") + 4);
                    command.append(line);
                    command.append(" ");
                } else if (line.contains(delimiter)) {
                    if (line.startsWith(delimiter)) {
                        delimiter = DEFAULT_DELIMITER;
                    }

                    if (line.endsWith(delimiter)) {
                        command.append(line.substring(0, line.lastIndexOf(delimiter)));
                        exec(command.toString().trim());
                        command = null;
                    }
                } else {
                    command.append(line);
                    command.append(" ");
                }
            }
        }

        // Check to see if we have an unexecuted statement in command.
        if (command != null && command.length() > 0) {
            //Last statement in script is missing a terminating delimiter, executing anyway.
            exec(command.toString().trim());
        }
    }

    private void processAsBlocks(String migration) {
        String[] statements = migration.split("BLOCK");
        for (String statement : statements) {
            exec(statement);
        }
    }

    private boolean containsIgnoreCase(String line, String sub) {
        return line.toLowerCase().contains(sub.toLowerCase());
    }

    private boolean startsWithIgnoreCase(String line, String sub) {
        return line.toLowerCase().startsWith(sub.toLowerCase());
    }

    private boolean commentLine(String line) {
        return line.startsWith("--") || line.startsWith("#") || line.startsWith("//");
    }

    public int compareTo(Migration other) {
        return this.getVersion().compareTo(other.getVersion());
    }
}
