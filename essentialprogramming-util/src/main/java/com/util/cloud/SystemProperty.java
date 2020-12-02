package com.util.cloud;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Lapusan
 */
public class SystemProperty {

    private String key;
    private final DeploymentStrategy deploymentStrategy;

    private final Map<DeploymentStrategy, Strategy> strategiesMap = new HashMap<>();

    public SystemProperty(String key, DeploymentStrategy strategy) {
        init();
        this.key = key;
        this.deploymentStrategy = strategy;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(T defaultValue) {
        T value = (T) strategiesMap.get(deploymentStrategy).getValue();
        return value != null ? value : defaultValue;
    }

    private void init() {
        strategiesMap.put(DeploymentStrategy.CLOUD, () -> System.getenv(key));
        strategiesMap.put(DeploymentStrategy.STANDALONE, () -> System.getProperty(key));
    }

    interface Strategy {
        Object getValue();
    }
}
