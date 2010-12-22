package activejdbc.reveng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activejdbc.Base;
import activejdbc.ColumnMetadata;
import activejdbc.LogFilter;
import activejdbc.Registry;
import activejdbc.annotations.Table;

public class ModelGenerator {
	
    private final static Logger logger = LoggerFactory.getLogger("ActiveJDBC Registry");
    private static final String DEFAULT_DB_NAME = "default";
	private String dbName = DEFAULT_DB_NAME;
	private String relativeSrcPath = "\\src\\main\\java\\";

	private static final Map<String, String> TYPE_MAP = 
	    Collections.unmodifiableMap(new HashMap<String, String>() {{ 
	        put("int unsigned", "Integer");
	        put("varchar", "String");
	        put("char", "String");
	        put("datetime", "Date");
	        put("int", "Integer");
	        put("float", "Float");
	        put("tinyint", "Integer");
	        put("smallint", "Integer");
	        put("tinyint unsigned", "Integer");
	        put("smallint unsigned", "Integer");
	        //put("", "");
	    }});
	
	public void reveng(String packageName, String tableName) {
		try {
			Map<String, ColumnMetadata> metaParams = Registry.instance()
					.fetchMetaParams(tableName, getDbName());

			String folder = new File(".").getCanonicalPath()
					+ relativeSrcPath + packageName.replace(".", "\\");
			
			String tableNameProper = tableName.substring(0,1).toUpperCase() + tableName.substring(1);
			String filename = folder + "\\" + tableNameProper + ".java";
			boolean success = (new File(folder)).mkdirs();
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("package " + packageName + ";\n");
			out.write("import activejdbc.Model;\n");
			out.write("import java.sql.Date;\n");
			out.write("import activejdbc.annotations.Table;\n\n");

			out.write("@Table(\""+tableName+"\")\n");
			out.write("public class " + tableNameProper + " extends Model {\n");

			for (ColumnMetadata column : metaParams.values()) {
				//System.out.println(column);
				
	            LogFilter.log(logger, "Column "+column);
				
				// out.write(column.toString());
				String fieldNameProper = column.getColumnName().substring(0, 1)
						.toUpperCase()
						+ column.getColumnName().substring(1);
				
				out.write("public void set" + fieldNameProper + "("+TYPE_MAP.get(column.getTypeName().toLowerCase())+" "
						+ column.getColumnName() + ") { this.set(\""
						+ column.getColumnName() + "\", "
						+ column.getColumnName() + "); }\n");
				out.write("public "+ TYPE_MAP.get(column.getTypeName().toLowerCase()) +" get" + fieldNameProper
						+ "() { return ("+ TYPE_MAP.get(column.getTypeName().toLowerCase()) +") this.get(\""
						+ column.getColumnName() + "\"); }\n");
				
/**				
				if (column.getTypeName().equalsIgnoreCase("varchar")
						|| column.getTypeName().equalsIgnoreCase("char")) {
					out.write("public void set" + fieldNameProper + "(String "
							+ column.getColumnName() + ") { this.set(\""
							+ column.getColumnName() + "\", "
							+ column.getColumnName() + "); }\n");
					out.write("public String get" + fieldNameProper
							+ "() { return (String) this.get(\""
							+ column.getColumnName() + "\"); }\n");
				}
				else if (column.getTypeName().equalsIgnoreCase("int")) {
					out.write("public void set" + fieldNameProper + "(Integer "
							+ column.getColumnName() + ") { this.set(\""
							+ column.getColumnName() + "\", "
							+ column.getColumnName() + "); }\n");
					out.write("public Integer get" + fieldNameProper
							+ "() { return (Integer) this.get(\""
							+ column.getColumnName() + "\"); }\n");
				}
	
**/
				
			}
			out.write("}\n");

			// Close the output stream
			out.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbName() {
		return dbName;
	}

	public String getRelativeSrcPath() {
		return relativeSrcPath;
	}

	public void setRelativeSrcPath(String relativeSrcPath) {
		this.relativeSrcPath = relativeSrcPath;
	}

	/**
	 * Example on how to auto generate model 
	 * 
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {

		Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://mysqltest/test", "", "");
		ModelGenerator generator = new ModelGenerator();
		generator.reveng("model","subscriber");
		generator.reveng("model","acc");
		generator.reveng("model","location");
		generator.reveng("model","lcr");
		Base.close();
		
	}
	
}
