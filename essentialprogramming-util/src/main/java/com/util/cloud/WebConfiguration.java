package com.util.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Ovidiu Lapusan
 */
public class WebConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(WebConfiguration.class);

    private Properties properties;

    @Override
    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return this.properties.containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    public void addProperty(String key, String value) {
        if (key != null && value != null) {
            this.properties.put(key, value);
        }
    }

    @Override
    public String getPropertyAsString(String key) {
        return this.properties.getProperty(key);
    }

    @Override
    public Integer getPropertyAsInteger(String key) {
        Integer property = null;
        String value = this.properties.getProperty(key);

        if (value != null) {
            try {
                property = Integer.parseInt(this.properties.getProperty(key));
            } catch (NumberFormatException e) {
                LOG.error(getExceptionMessage(key, value, "Integer"));
                throw new IllegalArgumentException(getExceptionMessage(key, value, "Integer"), e);
            }
        }

        return property;
    }

    @Override
    public void setProperties(Properties prop) {
        this.properties = prop;
    }

    @Override
    public void cleanProperties() {
        this.properties.clear();
    }

    private String getExceptionMessage(String key,
                                       String value,
                                       String type) {
        return "Format for property with key '" + key + "' and value '" + value + "' can not be converted to " + type
                + ".";
    }
}
