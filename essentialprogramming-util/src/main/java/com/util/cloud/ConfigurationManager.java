package com.util.cloud;

import com.util.io.FileInputResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ovidiu Lapusan
 */
public class ConfigurationManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final String CONFIG_FILE_PATH = "CONFIG_FILE";
    private static final String DEFAULT_CONFIG_FILE = "classpath:application.properties";
    private static final String DEPLOYMENT_STRATEGY = "DEPLOYMENT_STRATEGY";

    private ConfigurationManager() {
        throw new IllegalAccessError("Instantiation prohibited");
    }

    private static class ConfigurationHolder {
        private static final Configuration CONFIGURATION = loadConfiguration(initDeploymentStrategy());
    }

    /**
     * Initialize and load configuration from property files.
     */
    private static DeploymentStrategy initDeploymentStrategy() {
        return DeploymentStrategy
                .valueOf(DeploymentConfiguration.getProperty(DEPLOYMENT_STRATEGY, "STANDALONE"));

    }

    public static Configuration getConfiguration() {
        return ConfigurationHolder.CONFIGURATION;
    }

    private static Configuration loadConfiguration(DeploymentStrategy deploymentStrategy) {
        final SystemProperty configFile = new SystemProperty(CONFIG_FILE_PATH, deploymentStrategy);
        final String systemConfigFilePath = configFile.getValue(DEFAULT_CONFIG_FILE);

        WebConfiguration configuration = new WebConfiguration();
        try {
            FileInputResource fileInputResource = new FileInputResource(systemConfigFilePath);
            InputStream stream = fileInputResource.getInputStream();

            Properties properties = getPropertiesFromInputStream(stream);
            configuration.setProperties(properties);


        } catch (Exception e) {
            LOG.error("Configuration could not be loaded.");
            //throw e;
        }
        return configuration;
    }

    private static Properties getPropertiesFromInputStream(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        return properties;
    }
}
