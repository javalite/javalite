/**
 *
Copyright 2012 Kalyan Mulampaka

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
package activejdbc.codegen;

import java.io.File;
import java.io.FileWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used in code generation
 * @author Kalyan Mulampaka
 *
 */
public class CodeGenUtil
{

	final static Logger logger = LoggerFactory.getLogger(CodeGenUtil.class);

	public CodeGenUtil ()
	{

	}

	/**
	 * Returns the java bean style name for the input database column name.
	 * assumes database column name words are separated by underscores ('_'). If there are no underscores the input string is returned as is.
	 * e.g created_at will return createdAt
	 * @param name
	 * @return
	 */
	public static String normalize (String name)
	{
		StringBuilder strBuf = new StringBuilder("");
		// e,g created_at
		// should become createdAt

		char[] delimiter =
		{ '_' };
		if (name.indexOf("_") != -1)
		{
			name = WordUtils.capitalize(name, delimiter); // CreatedAt
			name = StringUtils.uncapitalize(name); // createdAt
			name = StringUtils.replace(name, "_", "");
		}
		strBuf.append(name);
		return strBuf.toString();
	}

	/**
	 * Creates the package folder structure if already not present
	 * @param packageName String
	 * @throws Exception
	 */
	public static void createPackage (String packageName) throws Exception
	{
		String path = "";
		if (StringUtils.isNotBlank(packageName))
		{
			path = StringUtils.replace(packageName, ".", "/");
			File file = new File(path);
			if (!file.exists())
			{
				file.mkdirs();
				logger.info("Package structure created:" + path);
			}
		}
	}

	/**
	 * This method creates the Column.java file which is used in the interfaces.
	 * @param packageName String
	 * @throws Exception
	 */
	public static void createColumnFile (String packageName) throws Exception
	{
		StringBuffer strBuf = new StringBuffer("");
		strBuf.append("public class Column<T>\n");
		strBuf.append("{\n");
		strBuf.append("	private Class<T> columnType;\n");
		strBuf.append("	private String columnName;\n");
		strBuf.append("	public Column (Class<T> type, String columnName)\n");
		strBuf.append("	{\n");
		strBuf.append("		super();\n");
		strBuf.append("		this.columnType = type;\n");
		strBuf.append("		this.columnName = columnName;\n");
		strBuf.append("	}\n");
		strBuf.append("	public Class<T> getColumnType ()\n");
		strBuf.append("	{\n");
		strBuf.append("		return columnType;\n");
		strBuf.append("	}\n");
		strBuf.append("	public void setColumnType (Class<T> columnType)\n");
		strBuf.append("	{\n");
		strBuf.append("		this.columnType = columnType;\n");
		strBuf.append("	}\n");
		strBuf.append("	public String getColumnName ()\n");
		strBuf.append("	{\n");
		strBuf.append("		return columnName;\n");
		strBuf.append("	}\n");
		strBuf.append("	public void setColumnName (String columnName)\n");
		strBuf.append("	{\n");
		strBuf.append("		this.columnName = columnName;\n");
		strBuf.append("	}\n");
		strBuf.append("}");

		String path = "";
		if (StringUtils.isNotBlank(packageName))
		{
			path = StringUtils.replace(packageName, ".", "/") + "/";
		}
		String fileName = path + "Column.java";
		File file = new File(fileName);
		FileWriter writer = new FileWriter(file);
		String header = "package " + packageName + ";\n\n";
		writer.write(header);
		writer.write(strBuf.toString());
		writer.close();
		logger.info("Column File created:" + fileName);

	}

}
