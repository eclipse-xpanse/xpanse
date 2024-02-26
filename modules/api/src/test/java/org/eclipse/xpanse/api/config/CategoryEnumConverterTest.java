package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryEnumConverterTest {

    private CategoryEnumConverter converterTest;

    @BeforeEach
    void setUp() {
        converterTest = new CategoryEnumConverter();
    }

    @Test
    void testConvert() {
        Assertions.assertEquals(Category.AI, converterTest.convert("ai"));
        Assertions.assertEquals(Category.COMPUTE, converterTest.convert("compute"));
        Assertions.assertEquals(Category.CONTAINER, converterTest.convert("container"));
        Assertions.assertEquals(Category.STORAGE, converterTest.convert("storage"));
        Assertions.assertEquals(Category.NETWORK, converterTest.convert("network"));
        Assertions.assertEquals(Category.DATABASE, converterTest.convert("database"));
        Assertions.assertEquals(Category.MEDIA_SERVICE, converterTest.convert("mediaService"));
        Assertions.assertEquals(Category.SECURITY, converterTest.convert("security"));
        Assertions.assertEquals(Category.MIDDLEWARE, converterTest.convert("middleware"));
        Assertions.assertEquals(Category.OTHERS, converterTest.convert("others"));
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert(" "));
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert("error_value"));

    }
}
