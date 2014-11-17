package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Image;
import org.javalite.common.Util;
import org.javalite.test.jspec.TestException;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;


/**
 * @author Igor Polevoy: 7/11/12 11:31 AM
 */
public class BlobTest extends ActiveJDBCTest {

    @Test
    public void shouldWriteAndReadBytesToDb() throws SQLException, IOException {
        Image image = new Image();
        byte[] igorBytes = Util.readResourceBytes("/igor.jpg");
        image.set("name", "igor's head");
        image.set("content", igorBytes);
        image.saveIt();

        Image igor1 = (Image) Image.findAll().get(0);
        byte[] igor1Bytes = igor1.getBytes("content");
        assertEqual(igorBytes, igor1Bytes);
    }

    private void assertEqual(byte[] igor, byte[] igor1) {
        if(igor.length != igor1.length) throw new TestException("arrays not equal");
        for(int i = 0 ; i < igor.length ; i++){
            if(igor[i] != igor1[i])
                throw new TestException("arrays not equal");
        }
    }
}
