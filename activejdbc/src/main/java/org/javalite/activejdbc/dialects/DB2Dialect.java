package org.javalite.activejdbc.dialects;

import org.javalite.activejdbc.MetaModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author William Janssen, Arjo Poldervaart
 */

public class DB2Dialect extends DefaultDialect {
	private final List<String> EMPTY_LIST = new ArrayList<String>();
	
	@Override
	public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
		// if table name is null, sub query contains a full SQL statement
		// we will need a more complex query if offset != -1
		// example:
		// SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY item_number) as ROWNUMBER, items.* from items WHERE  item_description like ?) as TEMP WHERE TEMP.ROWNUMBER  BETWEEN 271 AND 280
		// select * from (select row_number() over(order by item_number) as rownumber, ORIG.* from (select * from items where item_description like '%2%') as ORIG) as TEMP WHERE TEMP.ROWNUMBER  BETWEEN 271 AND 280
		boolean needOffset = offset != -1;
		boolean needLimit = limit != -1;
		StringBuilder fullQuery = new StringBuilder(subQuery == null? 0: subQuery.length());
		
		if (needOffset) {
			// first create inner query without order by
			StringBuilder innerQuery = new StringBuilder(subQuery == null? 0: subQuery.length());
			appendSelect(innerQuery, tableName, columns, null, subQuery, EMPTY_LIST);
			
			fullQuery.append("SELECT * FROM (SELECT ROW_NUMBER() OVER(");
			appendOrderBy(fullQuery, orderBys);
			fullQuery.append(") AS ROWNUMBER, ORIG.* from (");
			fullQuery.append(innerQuery.toString());
			fullQuery.append(") as ORIG) as TEMP WHERE TEMP.ROWNUMBER ");
			if (needLimit) {
				fullQuery.append(" BETWEEN ");
				fullQuery.append(offset+1);
				fullQuery.append(" AND ");
				fullQuery.append(offset+limit);
			} else {
				fullQuery.append(" > ");
				fullQuery.append(offset);
			}
			
		} else {
			appendSelect(fullQuery, tableName, columns, null, subQuery, orderBys);
			if (needLimit) {
				fullQuery.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
			}
		}
		
		return fullQuery.toString();
	}
	
    @Override
    protected void appendEmptyRow(MetaModel metaModel, StringBuilder query) {
        query.append('(').append(metaModel.getIdName()).append(") VALUES (DEFAULT)");
    }
}
