package br.com.anteros.persistence.osql.attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

@SuppressWarnings("serial")
public class AttributeInits implements Serializable {

    public static final AttributeInits DEFAULT = new AttributeInits();

    public static final AttributeInits DIRECT  = new AttributeInits("*");
    
    public static final AttributeInits DIRECT2  = new AttributeInits("*.*");
    
    private final boolean initAllProps;
    
    private final AttributeInits defaultValue;
    
    private final Map<String,AttributeInits> propertyToInits = new HashMap<String,AttributeInits>();

    public AttributeInits(String... initStrs) {
        boolean _initAllProps = false;
        AttributeInits _defaultValue = DEFAULT;
        
        Map<String, Collection<String>> properties = Maps.newHashMap();
        for (String initStr : initStrs) {
            if (initStr.equals("*")) {
                _initAllProps = true;
            } else if (initStr.startsWith("*.")) {
                _initAllProps = true;
                _defaultValue = new AttributeInits(initStr.substring(2));
            } else {
                String key = initStr;
                List<String> inits = Collections.emptyList();
                if (initStr.contains(".")) {
                    key = initStr.substring(0, initStr.indexOf('.'));
                    inits = ImmutableList.of(initStr.substring(key.length()+1));
                }
                Collection<String> values = properties.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    properties.put(key, values);
                }
                values.addAll(inits);
            }
        }
        
        for (Map.Entry<String, Collection<String>> entry : properties.entrySet()) {
            AttributeInits inits = new AttributeInits(Iterables.toArray(entry.getValue(), String.class));
            propertyToInits.put(entry.getKey(), inits);
        }
        
        initAllProps = _initAllProps;
        defaultValue = _defaultValue;
    }
    
    public AttributeInits get(String property) {
        if (propertyToInits.containsKey(property)) {
            return propertyToInits.get(property);
        } else if (initAllProps) {
            return defaultValue;
        } else {
            throw new IllegalArgumentException(property + " não inicializada");
        }
    }

    public boolean isInitialized(String property) {
        return initAllProps || propertyToInits.containsKey(property);
    }
    
}
