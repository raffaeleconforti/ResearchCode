package com.raffaeleconforti.wrappers.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adriano on 6/7/2017.
 */
public class MiningSettings {
    private Map<String, Object> params;

    public MiningSettings() { params = new HashMap<>(); }

    public void setParam(String param, Object value) { params.put(param, value); }
    public Object getParam(String param) { return params.get(param); }
    public boolean containsParam(String param) { return params.containsKey(param); }
}
