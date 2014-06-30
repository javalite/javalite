package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;


public class Migration implements Comparable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DEFAULT_DELIMITER_KEYWORD = "DELIMITER";

    private File migrationFile;
    private String version;

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

    private String getCharset(File file) {
        String charset = "GBK"; // 默认编码
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1]
                    == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1]
                    == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    //单独出现BF以下的，也算是GBK
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)// 双字节 (0xC0 - 0xDF)
                            // (0x80 -
                            // 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                        // 也有可能出错，但是几率较小
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
                System.out.println(loc + " " + Integer.toHexString(read));
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    public void migrate() throws Exception {

        String encoding = getCharset(migrationFile);
        StringBuffer command = null;
        try {
            String delimiter = DEFAULT_DELIMITER;
            LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(new FileInputStream(migrationFile), encoding));
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }

                line = line.trim(); // Strip extra whitespace too?

                if (line.length() < 1) {
                    // Do nothing, it's an empty line.
                } else if (commentLine(line)) {
                    logger.debug(line);
                } else {
                    if (startsWithIgnoreCase(line, DEFAULT_DELIMITER_KEYWORD)) {
                        delimiter = line.substring(10).trim();
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
                            Base.exec(command.toString().trim());
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
                Base.exec(command.toString().trim());
            }

        } catch (Exception e) {
            logger.error("Error executing: " + command, e);
            throw e;
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

    public int compareTo(Object o) {
        Migration other = (Migration) o;
        return this.getVersion().compareTo(other.getVersion());
    }
}
