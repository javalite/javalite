package org.javalite.activejdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author igor on 4/7/18.
 */
public class ScopeBuilder<T extends Model> {

    private Class<T> modelClass;
    private List<String> scopes;

    @SuppressWarnings("WeakerAccess") //has to be public!
    public ScopeBuilder(Class<T> modelClass, String[] scopes) {
        this.modelClass = modelClass;
        this.scopes = new ArrayList<>(Arrays.asList(scopes));
    }

    /**
     * Applies additional criteria to scopes defined in the model.
     *
     * @param subquery additional criteria.
     * @param params dynamic parameters for the subquery. Similar to {@link Model#where(String, Object...)}.
     * @return a list of records filtered by all supplied scopes as well as additional criteria.
     */
    public <T extends Model> LazyList<T> where(String subquery, Object... params) {
        StringBuilder query;

        if(subquery.equals("*")){
            query = new StringBuilder();

        }else {
            query = new StringBuilder(subquery);
            query.append(" AND ");
        }

        for (int i = 0; i < scopes.size(); i++) {
            String scope = scopes.get(i);
            if(!ModelDelegate.getScopes(modelClass.getName()).containsKey(scope)){
                throw new DBException(String.format("Scope '%s' is not defined in model '%s'.", scope, modelClass.getName()));
            }
            String scopeQuery = ModelDelegate.getScopes(modelClass.getName()).get(scope);
            query.append(scopeQuery);
            if (i < (scopes.size() - 1)) {
                query.append(" AND ");
            }
        }
        return ModelDelegate.where((Class<T>) modelClass, query.toString(), params);
    }

    /**
     * Use in case the scopes define all criteria you need.
     *
     * @return all instances of models according to defined scope filters.
     */
    public <T extends Model> LazyList<T> all() {
        return where("*");
    }

    public  ScopeBuilder<T> scope(String  scope) {
        scopes.add(scope);
        return this;
    }
}
