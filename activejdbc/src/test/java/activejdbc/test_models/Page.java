package activejdbc.test_models;

import activejdbc.Model;

/**
 * @author Igor Polevoy
 */
public class Page extends Model {
    static{
           validateNumericalityOf("word_count").greaterThan(10).onlyInteger().message("'word_count' must be a number greater than 10");
    }
}
