/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;


public class RowProcessor {
    private ResultSet rs;
    private Statement s;

    protected RowProcessor(ResultSet rs, Statement s){
        this.rs = rs;
        this.s = s;
    }

    public void with(RowListener listener){

        try{
            processRS(listener);
        }catch(Exception e){throw new DBException(e);}
        finally{try{rs.close();}catch(Exception e){/*ignore*/}  try{s.close();}catch(Exception e){/*ignore*/}}

    }

    protected void processRS(RowListener listener) throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        String labels[] = new String[metaData.getColumnCount()];
        for (int i = 1; i <= labels.length; i++) {
            labels[i - 1] = metaData.getColumnLabel(i);
        }

        while (rs.next()) {
            HashMap<String, Object> row = new HashMap<String, Object>();
            for (String label : labels) {                
                row.put(label.toLowerCase(), rs.getObject(label));
            }
            if(!listener.next(row)) break;
        }
        rs.close();
        s.close();
    }
}
