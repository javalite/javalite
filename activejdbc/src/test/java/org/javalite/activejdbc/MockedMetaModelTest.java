package org.javalite.activejdbc;

import org.javalite.activejdbc.test_models.Library;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.javalite.test.jspec.JSpec.the;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MockedMetaModelTest {

    @Mock
    private MetaModel metaModelMock;
    @Mock
    private ModelRegistry registryMock;

    private Library library;

    @Before
    public void setUp()
    {
        library = new Library(metaModelMock, registryMock);
    }

    @Test
    public void shouldSetAndReadModelPropertiesWithoutMetaModel()
    {
        library.setString("address", "5th Avenue");
        library.setString("city", "New York City");
        library.setString("state", "New York");

        the(library.getString("address")).shouldBeEqual("5th Avenue");
    }

    @Test(expected = IllegalStateException.class)
    public void mockShouldThrowIllegalStateExceptionSavingModel()
    {
        when(metaModelMock.getIdName()).thenThrow(new IllegalStateException("Meta model not available"));

        library.setString("address", "5th Avenue");

        library.saveIt();
    }

    @Test
    public void toStringShouldWork()
    {
        library.setString("address", "5th Avenue");

        the(library.toString()).shouldNotBeNull();
    }

    @Test
    public void mockTableName()
    {
        when(metaModelMock.getTableName()).thenReturn("<no meta data>");

        library.setString("address", "5th Avenue");

        the(library.toString().contains("<no meta data>")).shouldBeTrue();
    }

}
