package com.util.cloud;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Ovidiu Lapusan
 */
class WebConfigurationTest {

    private static final String STRING_PROPERTY_KEY = "string.property.key";
    private static final String STRING_PROPERTY_VALUE = "stringValue";
    private static final String NEW_PROPERTY_KEY = "test.key";
    private static final String NEW_PROPERTY_VALUE = "testValue";
    private static final String CLASSPATH = "classpath";

    private Configuration webConfiguration;

    @BeforeEach
    void setUp() {
        System.setProperty("DEPLOYMENT_STRATEGY", DeploymentStrategy.STANDALONE.toString());
        System.setProperty("CONFIG_FILE", CLASSPATH + ":" + "application.properties");

        webConfiguration = ConfigurationManager.getConfiguration();
    }

    @Test
    void properties_should_not_be_empty() {
        Assertions.assertFalse(webConfiguration.isEmpty());
    }

    @Test
    void configuration_should_contain_property_key() {
        Assertions.assertTrue(webConfiguration.containsKey(STRING_PROPERTY_KEY));
        Assertions.assertFalse(webConfiguration.containsKey("unexisting.key"));
    }

    @Test
    void configuration_should_get_property_value() {
        Assertions.assertNotNull(webConfiguration.getProperty(STRING_PROPERTY_KEY));
    }

    @Test
    void configuration_should_add_a_property_with_custom_value() {
        Assertions.assertNull(webConfiguration.getProperty("test.property"));
        webConfiguration.addProperty("test.property", "test.value");
        Assertions.assertNotNull(webConfiguration.getProperty("test.property"));
        Assertions.assertEquals(webConfiguration.getProperty("test.property"), "test.value");
    }

    @Test
    void configuration_should_add_a_property_with_null_value() {
        Assertions.assertNull(webConfiguration.getProperty("key1"));
        webConfiguration.addProperty("key1", null);
        Assertions.assertNull(webConfiguration.getProperty("key1"));
    }

    @Test
    void configuration_should_get_a_string_property() {
        String value = webConfiguration.getPropertyAsString(STRING_PROPERTY_KEY);

        Assertions.assertEquals(STRING_PROPERTY_VALUE, value);
    }

    @Test
    void configuration_should_get_a_string_property_with_null_value() {
        webConfiguration.addProperty("property.with.null.value", null);
        Assertions.assertNull(webConfiguration.getPropertyAsString("property.with.null.value"));
    }

    @Test
    void configuration_should_get_an_integer_property() {
        Object value = webConfiguration.getPropertyAsInteger("integer.property.key");

        Assertions.assertNotNull(value);
        Assertions.assertEquals(8, value);
    }

    @Test
    void configuration_should_get_an_integer_property_with_null_value() {
        Assertions.assertNull(webConfiguration.getPropertyAsInteger("key2"));
    }

    @Test
    void configuration_should_throw_exception_for_invalid_integer_value() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> webConfiguration.getPropertyAsInteger(STRING_PROPERTY_KEY));

        Assertions.assertTrue(exception.getMessage().contains("Format for property with key '" + STRING_PROPERTY_KEY + "' and value '"
                + STRING_PROPERTY_VALUE + "' can not be converted to Integer."));
    }
}
