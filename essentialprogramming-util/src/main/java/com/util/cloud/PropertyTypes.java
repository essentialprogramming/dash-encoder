package com.util.cloud;

import java.util.function.Function;

public enum PropertyTypes {

    STRING {
        public String getValue(String key, Function<String, String> FUNC) {
            return FUNC.apply(key);
        }
    },
    INTEGER {
        public Integer getValue(String key, Function<String, String> FUNC) {
    		String result = FUNC.apply(key);
            return result != null ? Integer.valueOf(result) : null;
        }
    };
    
    public abstract Object getValue(String key, Function<String, String> FUNC);
    };
