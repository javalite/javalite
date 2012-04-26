package activejdbc.codegen.test;

import java.io.FileInputStream;
import java.util.Properties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import activejdbc.codegen.CodeGenerator;

public class CodeGeneratorTest
{

	final static Logger logger = LoggerFactory.getLogger(CodeGeneratorTest.class);

	public CodeGeneratorTest ()
	{

	}

	public Properties loadProperties () throws Exception
	{
		String propertiesFile = "src/test/resources/generator-test.properties";
		logger.info("Loading properties from file:" + propertiesFile);
		FileInputStream propFile = new FileInputStream(propertiesFile);
		Properties props = new Properties();
		props.load(propFile);
		logger.debug("Loaded properties:" + props);

		return props;
	}

	@Test
	public void generate () throws Exception
	{
		CodeGenerator generator = new CodeGenerator();
		generator.setProperties(this.loadProperties());
		generator.generate();
	}


}
