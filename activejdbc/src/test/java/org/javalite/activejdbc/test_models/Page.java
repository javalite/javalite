package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.conversion.ConverterAdapter;
import org.javalite.common.Convert;

/**
 * @author Igor Polevoy
 */
public class Page extends Model {
    static {
        validateNumericalityOf("word_count").greaterThan(10).onlyInteger().message("'word_count' must be an integer greater than 10");
        convertWith(new StringToIntegerConverter(), "word_count");
    }
}

class StringToIntegerConverter extends ConverterAdapter<Object, Integer> {

    @Override
    protected Class<Object> sourceClass() {
        return Object.class;
    }

    @Override
    public boolean canConvert(Class<Object> aSourceClass, Class<Integer> aDestinationClass) {
        return true;
    }

    @Override
    protected Class<Integer> destinationClass() {
        return Integer.class;
    }

    @Override
    protected Integer doConvert(Object source){

        if(source == null){
            return null;
        }

        if("zero".equals(source)){
            return 0;
        }else{
            return Convert.toInteger(source);
        }
    }
}
