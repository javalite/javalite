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
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent the Java bean generated Interface.
 * Interfaces generated start with "I" + Table name e.g Employees table will generate IEmployee.java
 * @author Kalyan Mulampaka
 *
 */
public class Interfaze
{

	final static Logger logger = LoggerFactory.getLogger(Interfaze.class);

	private String packageName;
	private String name;
	private List<Method> methods;

	public Interfaze ()
	{

	}

	public List<Method> getMethods ()
	{
		return methods;
	}

	public void setMethods (List<Method> methods)
	{
		this.methods = methods;
	}

	public String getName ()
	{
		return "I" + WordUtils.capitalize(CodeGenUtil.normalize(name));
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public String getPackageName ()
	{
		return packageName;
	}

	public void setPackageName (String packageName)
	{
		this.packageName = packageName;
	}

	public StringBuffer print ()
	{
		StringBuffer strBuf = new StringBuffer("");
		strBuf.append("package " + packageName + ";\n\n");
		strBuf.append("public interface I" + WordUtils.capitalize(CodeGenUtil.normalize(name)) + "\n{\n");
		for (Method method : methods)
		{
			String methodName = WordUtils.capitalize(CodeGenUtil.normalize(method.getName()));
			String paramType = method.getParameter().getType().getName();
			String paramName = CodeGenUtil.normalize(method.getParameter().getName());

			strBuf.append("\tpublic static Column<" + paramType + "> " + StringUtils.upperCase(method.getName()) + " = ");
			strBuf.append("new Column<" + paramType + "> (" + paramType +".class, ");
			strBuf.append("\"" + method.getName() + "\");\n");

			// setter
			strBuf.append("\tvoid set" + methodName + " (");
			strBuf.append(paramType + " " + paramName);
			strBuf.append(");\n");

			// getter
			strBuf.append("\t" + paramType + " get" + methodName + " ();\n\n");
		}
		strBuf.append("}");
		logger.debug("Printing Interface file content:\n" + strBuf.toString());
		return strBuf;
	}

	public void createFile () throws Exception
	{
		String path = "";
		if (StringUtils.isNotBlank(this.packageName))
		{
			path = StringUtils.replace(this.packageName, ".", "/") + "/";
		}
		String fileName = path + "I" + WordUtils.capitalize(CodeGenUtil.normalize(this.name)) + ".java";
		File file = new File(fileName);
		FileWriter writer = new FileWriter(file);
		writer.write(this.print().toString());
		writer.close();
		logger.info("Interface File created:" + fileName);
	}

}
