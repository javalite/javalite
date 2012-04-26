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
 * Class to represent the generated Java bean Class. Class name is same as the
 * table name in singular form. e.g For employees table , Employee.java is
 * generated
 * 
 * @author Kalyan Mulampaka
 * 
 */
public class Clazz
{

	final static Logger logger = LoggerFactory.getLogger(Clazz.class);
	private String rootFolderPath;
	private String packageName;
	private String name;
	private String extendsClassName;
	private String interfaceName;
	private List<Method> methods;

	public Clazz ()
	{

	}

	public String getRootFolderPath ()
	{
		return rootFolderPath;
	}

	public void setRootFolderPath (String rootFolderPath)
	{
		this.rootFolderPath = rootFolderPath;
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
		return WordUtils.capitalize(CodeGenUtil.normalize(name));
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

	public String getExtendsClassName ()
	{
		return extendsClassName;
	}

	public void setExtendsClassName (String extendsClassName)
	{
		this.extendsClassName = extendsClassName;
	}

	public String getInterfaceName ()
	{
		return interfaceName;
	}

	public void setInterfaceName (String interfaceName)
	{
		this.interfaceName = interfaceName;
	}

	public StringBuffer print ()
	{
		StringBuffer strBuf = new StringBuffer("");
		strBuf.append("package " + packageName + ";\n\n");
		strBuf.append("import " + extendsClassName + ";\n\n");

		strBuf.append("public class " + WordUtils.capitalize(CodeGenUtil.normalize(name)));
		strBuf.append(" extends " + extendsClassName);
		if (StringUtils.isNotBlank(interfaceName))
		{
			strBuf.append(" implements " + interfaceName);
		}
		strBuf.append("\n{\n\n");
		// no args constructor
		strBuf.append("\tpublic " + WordUtils.capitalize(CodeGenUtil.normalize(name)) + " ()\n\t{\n");

		strBuf.append("\t}\n\n");

		for (Method method : methods)
		{
			String methodName = WordUtils.capitalize(CodeGenUtil.normalize(method.getName()));
			String paramName = CodeGenUtil.normalize(method.getParameter().getName());
			String paramType = method.getParameter().getType().getName();
			String columnName = method.getParameter().getName();
			// setter
			if (method.isGenerateSetter())
			{
				strBuf.append(CodeGenUtil.generateComment());
				strBuf.append("\tpublic void set" + methodName + " (");
				strBuf.append(paramType + " " + paramName);
				strBuf.append(")\n");

				// implementation
				strBuf.append("\t{\n");
				strBuf.append("\t\tset(\"" + columnName + "\", " + paramName + ");\n");
				strBuf.append("\t}\n\n");
			}

			// getter
			if (method.isGenerateGetter())
			{
				strBuf.append(CodeGenUtil.generateComment());
				strBuf.append("\tpublic " + paramType + " get" + methodName + " ()\n");
				strBuf.append("\t{\n");
				strBuf.append("\t\treturn (" + paramType + ") get(\"" + columnName + "\");\n");
				strBuf.append("\t}\n\n");
			}
		}
		strBuf.append("}");
		logger.debug("Printing Class file content:\n" + strBuf.toString());
		return strBuf;
	}


	public void createFile () throws Exception
	{
		String path = "";
		if (StringUtils.isNotBlank(this.packageName))
		{
			path = StringUtils.replace(this.packageName, ".", "/") + "/";
		}
		if (StringUtils.isNotBlank(this.rootFolderPath))
		{
			path = this.rootFolderPath + "/" + path;
		}
		String fileName = path + WordUtils.capitalize(CodeGenUtil.normalize(this.name)) + ".java";
		File file = new File(fileName);
		FileWriter writer = new FileWriter(file);
		writer.write(this.print().toString());
		writer.close();
		logger.info("Class File created:" + fileName);
	}

}
