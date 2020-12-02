package com.util.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Ovidiu Lapusan
 */
class ConfigurationManagerTest {

    private static final String CLASSPATH = "classpath";

    @BeforeEach
    void setUp() {
        System.setProperty("DEPLOYMENT_STRATEGY", DeploymentStrategy.STANDALONE.toString());
        System.setProperty("CONFIG_FILE", CLASSPATH + ":" + "application.properties");
    }

    @Test
    void configuration_should_load_file_from_classpath() {
        ConfigurationManager.getConfiguration();
    }
}
