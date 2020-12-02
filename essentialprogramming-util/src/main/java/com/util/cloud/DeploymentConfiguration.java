package com.util.cloud;

import java.util.Optional;
import java.util.function.Function;

/**
 * Fetches a property from the Environment, System Properties, then uses a
 * fallback. Useful in Cloud Native (Docker / Kubernetes) deployments.
 *
 */
public interface DeploymentConfiguration {

	Function<String, String> ENV = (key) -> System.getenv().getOrDefault(key, System.getProperty(key));

	
	@SuppressWarnings("unchecked")
	static<T> T getProperty(String key, T fallback) {
		String type =  fallback != null ? fallback.getClass().getSimpleName().toUpperCase() : "STRING";
		T value = (T) PropertyTypes.valueOf(type).getValue(key, ENV);
		
		return Optional.ofNullable( value ).orElse( fallback );
	}
}
