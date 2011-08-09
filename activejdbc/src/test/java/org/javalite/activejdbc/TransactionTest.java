/**
 * Developer: Kadvin Date: 11-8-9 上午1:55
 */
package org.javalite.activejdbc;

import org.javalite.activejdbc.test.DefaultDBReset;
import org.javalite.activejdbc.test.OracleDBReset;
import org.javalite.activejdbc.test_models.Account;
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.User;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;

import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * Test the AJ Model can maintain the connection inside
 *
 * Developer need establish the connection at the very first stage,
 * and need not care about the connection open/close and
 * transaction begin/commit/rollback issues
 *
 * Then the AJ can be more friendly for developer
 *
 * Actually, almost all the underling API was migrated to transactional support
 * except some Base.xxx, the transaction API is tested by all previous test cases also.
 */
public class TransactionTest extends JSpecSupport {

    static boolean schemaGenerated = false;

    @BeforeClass
    public static void setup() throws Exception {
        Base.establish(driver(), url(), user(), password());

        if(!schemaGenerated){
            try {
                MetaModel.acquire("default", false);
                generateSchema();
            } finally {
                MetaModel.release("default");
            }
            schemaGenerated = true;
            System.out.println("DB: " + db());
        }
    }

    @Test public void getMetaModelShouldWorks(){
        //any model related API can works now
        a(Account.getMetaModel().getTableName()).shouldBeEqual("accounts");
    }

    @Test public void countShouldWorks(){
        //any model related API can works now
        a(Account.count() == 0).shouldBeTrue();
    }

    @Test public void countByQueryShouldWorks(){
        //any model related API can works now
        a(Account.count("total > 10") == 0).shouldBeTrue();
    }

    @Test public void saveShouldWorks(){
        a(account().save()).shouldBeTrue();
    }

    @Test public void saveItShouldWorks(){
        a(account().saveIt()).shouldBeTrue();
    }

    @Test public void deleteShouldWorks(){
        Model account = createAccount();
        a(account.delete()).shouldBeTrue();
    }

    @Test public void deleteCascadeShouldWorks(){
        Model account = createAccount();
        account.deleteCascade();// without any exception
    }

    @Test public void existsShouldWorks(){
        Account.exists(10); //without any exception
    }

    @Test public void deleteAllShouldWorks(){
        Account.deleteAll(); //without any exception
    }

    @Test public void deleteByQueryShouldWorks(){
        Account.delete("total > 10"); //without any exception
    }

    @Test public void updateShouldWorks(){
        Account.update("amount = ?", "total = ?", 5, 99); //without any exception
    }

    @Test public void updateAllShouldWorks(){
        Account.updateAll("amount = ?", 99); //without any exception
    }

    @Test(expected = OrphanRecordException.class)
    public void parentShouldWorks(){
        Model address = Address.create("city", "Shanghai", "state", "Shanghai", "zip", "200123", "user_id", null);
        address.save();
        address.parent(User.class);
    }

    @Test public void setParentShouldWorks(){
        Model address = Address.create("city", "Shanghai", "state", "Shanghai", "zip", "200123", "user_id", 123);
        address.save();

        User parent = new User();
        parent.setId(122);
        //I'm worry about this method implementations will query db, so add this case
        address.setParent(parent);
    }

    @Test public void refreshShouldWorks(){
        Model account = createAccount();
        account.refresh();
    }

    @Test public void lazyListShouldWorks(){
        LazyList<Model> accounts = Account.findAll();
        accounts.size();//does not throw any exception
    }

    private Model createAccount() {
        Model account = account();
        a(account.saveIt()).shouldBeTrue();
        return account;
    }

    private Model account() {
        return Account.create("account", "kadvin", "description", "demo value", "amount", 100, "total", 99);
    }

    protected static void generateSchema() throws SQLException, ClassNotFoundException {
        if (db().equals("mysql")) {
            DefaultDBReset.resetSchema(getStatements(";", "mysql_schema.sql"));
        }else if (db().equals("postgresql")) {
            DefaultDBReset.resetSchema(getStatements(";", "postgres_schema.sql"));
        } else if (db().equals("h2")) {
            DefaultDBReset.resetSchema(getStatements(";", "h2_schema.sql"));
        } else if (db().equals("oracle")) {
            OracleDBReset.resetOracle(getStatements("-- BREAK", "oracle_schema.sql"));
        }
    }

    public static String[] getStatements(String delimiter, String file) {
        try {

            System.out.println("Getting statements from file: " + file);
            InputStreamReader isr = new InputStreamReader(TransactionTest.class.getClassLoader().getResourceAsStream(file));
            BufferedReader reader = new BufferedReader(isr);
            StringBuffer text = new StringBuffer();
            String t;
            while ((t = reader.readLine()) != null) {
                text.append(t).append('\n');
            }
            return text.toString().split(delimiter);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
