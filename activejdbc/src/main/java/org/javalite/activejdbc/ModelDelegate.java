package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.QueryCache;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javalite.activejdbc.associations.BelongsToAssociation;
import org.javalite.activejdbc.associations.Many2ManyAssociation;
import org.javalite.activejdbc.conversion.BlankToNullConverter;
import org.javalite.activejdbc.conversion.Converter;
import org.javalite.activejdbc.conversion.ZeroToNullConverter;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.validation.DateConverter;
import org.javalite.activejdbc.validation.EmailValidator;
import org.javalite.activejdbc.validation.NumericValidationBuilder;
import org.javalite.activejdbc.validation.RangeValidator;
import org.javalite.activejdbc.validation.RegexpValidator;
import org.javalite.activejdbc.validation.TimestampConverter;
import org.javalite.activejdbc.validation.ValidationBuilder;
import org.javalite.activejdbc.validation.Validator;
import org.javalite.common.Convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.javalite.common.Util.*;

/**
 * This class exists to offload some logic from {@link Model}  class.
 *
 * @author Igor Polevoy: 4/25/12 2:45 AM
 * @author Eric Nielsen
 */
public final class ModelDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDelegate.class);

    private ModelDelegate() {
        // not instantiable
    }

    public static List<Association> associations(Class<? extends Model> clazz) {
        return metaModelOf(clazz).getAssociations();
    }

    /**
     * @deprecated use {@link #attributeNames(Class)} instead
     */
    @Deprecated
    public static List<String> attributes(Class<? extends Model> clazz){
        return Arrays.asList(lowerCased(attributeNames(clazz)));
    }

    public static Set<String> attributeNames(Class<? extends Model> clazz) {
        return metaModelOf(clazz).getAttributeNames();
    }

    public static boolean belongsTo(Class<? extends Model> clazz, Class<? extends Model> targetClass) {
        MetaModel metaModel = metaModelOf(clazz);
        return (null != metaModel.getAssociationForTarget(targetClass, BelongsToAssociation.class) ||
                null != metaModel.getAssociationForTarget(targetClass, Many2ManyAssociation.class));
    }

    public static void blankToNull(Class<? extends Model> clazz, String... attributeNames) {
        modelRegistryOf(clazz).convertWith(BlankToNullConverter.instance(), attributeNames);
    }

    public static void callbackWith(Class<? extends Model> clazz, CallbackListener... listeners) {
         modelRegistryOf(clazz).callbackWith(listeners);
    }

    /**
     * @deprecated use {@link #dateFormat(Class, String, String...) instead
     */
    @Deprecated
    public static ValidationBuilder convertDate(Class<? extends Model> clazz, String attributeName, String format) {
        return modelRegistryOf(clazz).validateWith(new DateConverter(attributeName, format));
    }
    /**
     * @deprecated use {@link #timestampFormat(Class, String, String...) instead
     */
    @Deprecated
    public static ValidationBuilder convertTimestamp(Class<? extends Model> clazz, String attributeName, String format) {
        return modelRegistryOf(clazz).validateWith(new TimestampConverter(attributeName, format));
    }

    /**
     * @deprecated use {@link #convertWith(Class, org.javalite.activejdbc.conversion.Converter, String...)} instead
     */
    @Deprecated
    protected static ValidationBuilder convertWith(Class<? extends Model> clazz, org.javalite.activejdbc.validation.Converter converter) {
        return validateWith(clazz, converter);
    }

    public static void convertWith(Class<? extends Model> clazz, Converter converter, String... attributeNames) {
        modelRegistryOf(clazz).convertWith(converter, attributeNames);
    }

    public static Long count(Class<? extends Model> clazz) {
        MetaModel metaModel = metaModelOf(clazz);
        String sql = metaModel.getDialect().selectCount(metaModel.getTableName());
        Long result;
        if (metaModel.cached()) {
            result = (Long) QueryCache.instance().getItem(metaModel.getTableName(), sql, null);
            if (result == null) {
                result = Convert.toLong(new DB(metaModel.getDbName()).firstCell(sql));
                QueryCache.instance().addItem(metaModel.getTableName(), sql, null, result);
            }else {
                LogFilter.logQuery(LOGGER, sql, new Object[]{}, System.currentTimeMillis(), true);
            }
        } else {
            result = Convert.toLong(new DB(metaModel.getDbName()).firstCell(sql));
        }
        return result;
    }

    public static Long count(Class<? extends Model> clazz, String query, Object... params) {
        MetaModel metaModel = metaModelOf(clazz);
        String sql = metaModel.getDialect().selectCount(metaModel.getTableName(), query);
        Long result;
        if (metaModel.cached()) {
            result = (Long) QueryCache.instance().getItem(metaModel.getTableName(), sql, params);
            if (result == null) {
                result = Convert.toLong(new DB(metaModel.getDbName()).firstCell(sql, params));
                QueryCache.instance().addItem(metaModel.getTableName(), sql, params, result);
            }
        } else {
            result = Convert.toLong(new DB(metaModel.getDbName()).firstCell(sql, params));
        }
        return result;
    }

    public static <T extends Model> T create(Class<T> clazz, Object... namesAndValues) {
        try {
            return clazz.newInstance().set(namesAndValues);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("All even arguments must be strings");
        } catch (DBException e) {
            throw e;
        } catch (Exception e) {
            throw new InitException("Model '" + clazz.getName() + "' must provide a default constructor.", e);
        }
    }

    public static <T extends Model> T createIt(Class<T> clazz, Object... namesAndValues) {
        T model = create(clazz, namesAndValues);
        model.saveIt();
        return model;
    }

    public static void dateFormat(Class<? extends Model> clazz, DateFormat format, String... attributeNames) {
        modelRegistryOf(clazz).dateFormat(format, attributeNames);
    }

    public static void dateFormat(Class<? extends Model> clazz, String pattern, String... attributeNames) {
        modelRegistryOf(clazz).dateFormat(pattern, attributeNames);
    }

    public static int delete(Class<? extends Model> clazz, String query, Object... params) {
        MetaModel metaModel = metaModelOf(clazz);
        //TODO: refactor this:
        int count = (params == null || params.length == 0)
            ? new DB(metaModel.getDbName()).exec("DELETE FROM " + metaModel.getTableName() + " WHERE " + query)
            : new DB(metaModel.getDbName()).exec("DELETE FROM " + metaModel.getTableName() + " WHERE " + query, params);
        if (metaModel.cached()) {
            Registry.cacheManager().purgeTableCache(metaModel);
        }
        purgeEdges(metaModel);
        return count;
    }

    public static int deleteAll(Class<? extends Model> clazz) {
        MetaModel metaModel = metaModelOf(clazz);
        int count = new DB(metaModel.getDbName()).exec("DELETE FROM " + metaModel.getTableName());
        if (metaModel.cached()) {
            Registry.cacheManager().purgeTableCache(metaModel);
        }
        purgeEdges(metaModel);
        return count;
    }

    public static boolean exists(Class<? extends Model> clazz, Object id) {
        MetaModel metaModel = metaModelOf(clazz);
        return null != new DB(metaModel.getDbName()).firstCell(metaModel.getDialect().selectExists(metaModel), id);
    }

    public static <T extends Model> LazyList<T> findAll(Class<T> clazz) {
        return new LazyList(null, metaModelOf(clazz));
    }

    public static <T extends Model> T findById(Class<T> clazz, Object id) {
        if (id == null) { return null; }
        MetaModel metaModel = metaModelOf(clazz);
        LazyList<T> list = new LazyList<T>(metaModel.getIdName() + " = ?", metaModel, id).limit(1);
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T extends Model> T findByCompositeKeys(Class<T> clazz, Object...values) {
        if (values == null || values.length == 0) { return null; }
        MetaModel metaModel = metaModelOf(clazz);
        String[] compositeKeys = metaModel.getCompositeKeys();
        if (compositeKeys == null || compositeKeys.length != values.length){
        	return null;
        }
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < compositeKeys.length; i++) {
			sb.append(i == 0 ? "" : " AND ").append(compositeKeys[i])
					.append(" = ?");
		}
        LazyList<T> list = new LazyList<T>(sb.toString(), metaModel, values).limit(1);
        return list.isEmpty() ? null : list.get(0);
    }
    
    public static <T extends Model> LazyList<T> findBySql(Class<T> clazz, String fullQuery, Object... params) {
        return new LazyList<>(false, metaModelOf(clazz), fullQuery, params);
    }

    public static <T extends Model> T findFirst(Class<T> clazz, String subQuery, Object... params) {
        LazyList<T> list = new LazyList<T>(subQuery, metaModelOf(clazz), params).limit(1);
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T extends Model, M extends T> void findWith(final Class<M> clazz, final ModelListener<T> listener,
            String query, Object... params) {
        long start = System.currentTimeMillis();
        final MetaModel metaModel = metaModelOf(clazz);
        String sql = metaModel.getDialect().selectStar(metaModel.getTableName(), query);
        new DB(metaModel.getDbName()).find(sql, params).with(new RowListenerAdapter() {
            @Override
            public void onNext(Map<String, Object> row) {
                listener.onModel(instance(row, metaModel, clazz));
            }
        });
        LogFilter.logQuery(LOGGER, sql, null, start);
    }

    static <T extends Model> T instance(Map<String, Object> map, MetaModel metaModel) {
        return (T) instance(map, metaModel, metaModel.getModelClass());
    }

    static <T extends Model> T instance(Map<String, Object> map, MetaModel metaModel, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            instance.hydrate(map, true);
            return instance;
        } catch(InstantiationException e) {
            throw new InitException("Failed to create a new instance of: " + metaModel.getModelClass() + ", are you sure this class has a default constructor?");
        } catch(DBException e) {
            throw e;
        } catch(InitException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static String[] lowerCased(Collection<String> collection) {
        String[] array = new String[collection.size()];
        int i = 0;
        for (String elem : collection) {
            array[i++] = elem.toLowerCase();
        }
        return array;
    }

    /**
     * Returns {@link MetaModel} associated with table name.
     *
     * @param tableName name of table.
     * @return {@link MetaModel} associated with table name
     */
    public static MetaModel metaModelFor(String tableName) {
        return Registry.instance().getMetaModel(tableName);
    }

    /**
     * Returns {@link MetaModel} associated with model class.
     *
     * @param clazz model class.
     * @return {@link MetaModel} associated with table name
     */
    public static MetaModel metaModelOf(Class<? extends Model> clazz) {
        return Registry.instance().getMetaModel(clazz);
    }

    private static ModelRegistry modelRegistryOf(Class<? extends Model> clazz) {
        return Registry.instance().modelRegistryOf(clazz);
    }

    public static void purgeCache(Class<? extends Model> clazz) {
        MetaModel metaModel = metaModelOf(clazz);
        if (metaModel.cached()) {
            Registry.cacheManager().purgeTableCache(metaModel);
        }
    }

    static void purgeEdges(MetaModel metaModel) {
        //this is to eliminate side effects of cache on associations.
        //TODO: Need to write tests for cases;
        // 1. One to many relationship. Parent and child are cached.
        //      When a new child inserted, the parent.getAll(Child.class) should see that
        // 2. Many to many. When a new join inserted, updated or deleted, the one.getAll(Other.class) should see the difference.

        //Purge associated targets

        List<Association> associations = metaModel.getAssociations();
        for(Association association: associations){
            Registry.cacheManager().purgeTableCache(metaModelOf(association.getTargetClass()));
        }

        //Purge edges in case this model represents a join
        List<String> edges = Registry.instance().getEdges(metaModel.getTableName());
        for(String edge: edges){
            Registry.cacheManager().purgeTableCache(edge);
        }
    }

    public static void removeValidator(Class<? extends Model> clazz, Validator validator) {
        modelRegistryOf(clazz).removeValidator(validator);
    }

    public static String tableNameOf(Class<? extends Model> clazz) {
        return Registry.instance().getTableName(clazz);
    }

    public static void timestampFormat(Class<? extends Model> clazz, String pattern, String... attributeNames) {
        modelRegistryOf(clazz).timestampFormat(pattern, attributeNames);
    }

    public static void timestampFormat(Class<? extends Model> clazz, DateFormat format, String... attributeNames) {
        modelRegistryOf(clazz).timestampFormat(format, attributeNames);
    }

    public static int update(Class<? extends Model> clazz, String updates, String conditions, Object... params) {
        return update(metaModelOf(clazz), updates, conditions, params);
    }

    private static int update(MetaModel metaModel, String updates, String conditions, Object... params) {
        StringBuilder sql = new StringBuilder().append("UPDATE ").append(metaModel.getTableName()).append(" SET ");
        Object[] allParams;
        if (metaModel.hasAttribute("updated_at")) {
            sql.append("updated_at = ?, ");
            allParams = new Object[params.length + 1];
            allParams[0] = new Timestamp(System.currentTimeMillis());
            System.arraycopy(params, 0, allParams, 1, params.length);
        } else {
            allParams = params;
        }
        sql.append(updates);
        if (!blank(conditions)) {
            sql.append(" WHERE ").append(conditions);
        }
        int count = new DB(metaModel.getDbName()).exec(sql.toString(), allParams);
        if (metaModel.cached()) {
            Registry.cacheManager().purgeTableCache(metaModel);
        }
        return count;
    }

    public static int updateAll(Class<? extends Model> clazz, String updates, Object... params) {
        return update(clazz, updates, null, params);
    }

    public static ValidationBuilder validateEmailOf(Class<? extends Model> clazz, String attributeName) {
        return modelRegistryOf(clazz).validateWith(new EmailValidator(attributeName));
    }

    public static NumericValidationBuilder validateNumericalityOf(Class<? extends Model> clazz, String... attributeNames) {
        return modelRegistryOf(clazz).validateNumericalityOf(attributeNames);
    }

    public static List<Validator> validatorsOf(Class<? extends Model> clazz) {
        return modelRegistryOf(clazz).validators();
    }

    public static ValidationBuilder validatePresenceOf(Class<? extends Model> clazz, String... attributeNames) {
        return modelRegistryOf(clazz).validatePresenceOf(attributeNames);
    }

    public static ValidationBuilder validateRange(Class<? extends Model> clazz, String attributeName, Number min, Number max) {
        return modelRegistryOf(clazz).validateWith(new RangeValidator(attributeName, min, max));
    }

    public static ValidationBuilder validateRegexpOf(Class<? extends Model> clazz, String attributeName, String pattern) {
        return modelRegistryOf(clazz).validateWith(new RegexpValidator(attributeName, pattern));
    }

    public static ValidationBuilder validateWith(Class<? extends Model> clazz, Validator validator) {
        return modelRegistryOf(clazz).validateWith(validator);
    }

    public static <T extends Model> LazyList<T> where(Class<T> clazz, String subquery, Object... params) {
        if (subquery.trim().equals("*")) {
            if (empty(params)) {
                return findAll(clazz);
            } else {
                throw new IllegalArgumentException(
                        "cannot provide parameters with query: '*', use findAll() method instead");
            }
        }
        return new LazyList<>(subquery, metaModelOf(clazz), params);
    }

    public static void zeroToNull(Class<? extends Model> clazz, String... attributeNames) {
        modelRegistryOf(clazz).convertWith(ZeroToNullConverter.instance(), attributeNames);
    }



    public static <T extends Model> T findOrCreateIt(Class<T> clazz, Object... namesAndValues) {
        if (namesAndValues.length == 0 || namesAndValues.length % 2 != 0){
            throw new IllegalArgumentException("number of arguments must be even");
        }
        //Generates subQuery from namesAndValues
        StringBuilder subQuery = new StringBuilder();
        //Parameters for subQuery
        Object[] params = new Object[namesAndValues.length / 2];
        int x = 0;
        for (int i = 0; i < namesAndValues.length; i++){
            if (i % 2 == 0){
                subQuery.append((subQuery.length() > 0) ? " and " + namesAndValues[i] + " = ?" : namesAndValues[i] + " = ?");
            } else {
                params[x++] = namesAndValues[i];
            }
        }
        if(count(clazz, subQuery.toString(), params) > 0){
            return findFirst(clazz, subQuery.toString(),params);
        } else{
            return createIt(clazz, namesAndValues);
        }
    }
}
