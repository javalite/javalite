package org.javalite.activejdbc.dialects;

import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.ColumnMetadata;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.MetaModel;
import org.javalite.common.Util;

public class MSSQLDialect extends DefaultDialect {

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {
        boolean needLimit = limit != -1;
        boolean needOffset = offset != -1;
        
        if(needOffset && (orderBys == null || orderBys.isEmpty())) {
        	throw new DBException("MSSQL offset queries require an order by column.");
        }

        limit = (offset == -1 ? limit : offset + limit);
        offset += 1; //T-SQL offset starts with 1, not like MySQL with 0;

        String fullQuery = createBaseQuery(tableName, subQuery, needOffset);

        fullQuery = addOderBys(orderBys, needOffset, fullQuery);

        fullQuery = addOffsetAndLimit(orderBys, limit, offset, needLimit, needOffset, fullQuery);

        return fullQuery;
    }
    
    /**
     * TDS converts a number of important data types to String. This isn't what we want, nor helpful. Here, we change them back.
     */
    @Override
    public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
        Map<String, ColumnMetadata> types = mm.getColumnMetadata();
    	if(value != null && value instanceof java.lang.String && types.get(attributeName.toLowerCase()).getTypeName().equalsIgnoreCase("date") && mm.getDialect() instanceof MSSQLDialect) {
    		return java.sql.Date.valueOf((String)value);
    	} else if(value != null && value instanceof java.lang.String && types.get(attributeName.toLowerCase()).getTypeName().equalsIgnoreCase("datetime2") && mm.getDialect() instanceof MSSQLDialect) {
    		return java.sql.Timestamp.valueOf((String)value);
    	} else {
    		return value;
    	}
    }

    private String createBaseQuery(String tableName, String subQuery,
			boolean needOffset) {
		String fullQuery;
		if (tableName == null) {//table is in the sub-query already
            fullQuery = subQuery;
        } else {
            fullQuery = needOffset ? " * FROM " + tableName + " " : "SELECT {LIMIT} * FROM " + tableName;
            fullQuery = addSubQuery(subQuery, fullQuery);
        }
		return fullQuery;
	}

	private String addOderBys(List<String> orderBys, boolean needOffset,
			String fullQuery) {
		if(orderBys.size() != 0 && !needOffset){
            fullQuery += " ORDER BY " + Util.join(orderBys, ", ");
        }
		return fullQuery;
	}

	private String addOffsetAndLimit(List<String> orderBys, long limit,
			long offset, boolean needLimit, boolean needOffset, String fullQuery) {
		String limitString = "";
		String fullQueryWithoutSelect = fullQuery.replaceFirst("^\\s*[Ss][Ee][Ll][Ee][Cc][Tt]", "");
        if(needLimit && needOffset){
            fullQuery = "SELECT sq.* FROM ( SELECT ROW_NUMBER() OVER (ORDER BY " + Util.join(orderBys, ", ") + ") AS rownumber, " + fullQueryWithoutSelect + " ) AS sq WHERE rownumber BETWEEN " + offset + " AND " + limit + " ";
        }
        else if(needLimit && !needOffset){
            limitString = " TOP " + limit + " ";
        }else if(needOffset){
            fullQuery = "SELECT sq.* FROM ( SELECT ROW_NUMBER() OVER (ORDER BY " + Util.join(orderBys, ", ") + ") AS rownumber, " + fullQueryWithoutSelect + " ) AS sq WHERE rownumber >= " + offset + " ";
        }
        fullQuery = fullQuery.replace("{LIMIT}", limitString);
		return fullQuery;
	}

	private String addSubQuery(String subQuery, String fullQuery) {
		if (!Util.blank(subQuery)) {
		    String where = " WHERE ";
		    //this is only to support findFirst("order by..."), might need to revisit later

		    if (!groupByPattern.matcher(subQuery.toLowerCase().trim()).find() &&
		            !orderByPattern.matcher(subQuery.toLowerCase().trim()).find()) {
		        fullQuery += where;
		    }
		    fullQuery += subQuery;
		}
		return fullQuery;
	}

}
