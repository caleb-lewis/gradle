/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.plugins.Convention;
import org.gradle.api.tasks.ConventionValue;
import org.gradle.util.ReflectionUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

/**
 * @author Hans Dockter
 */
public class ConventionAwareHelper implements ConventionMapping {
    private Convention convention;

    private IConventionAware source;

    private Map<String, ConventionValue> conventionMapping = new HashMap<String, ConventionValue>();
    private Map<String, Object> conventionMappingCache = new HashMap<String, Object>();

    public ConventionAwareHelper(IConventionAware source, Convention convention) {
        this.source = source;
        this.convention = convention;
    }

    public ConventionMapping map(String propertyName, ConventionValue value) {
        map(Collections.singletonMap(propertyName, value));
        return this;
    }

    public ConventionMapping map(Map<String, ConventionValue> mapping) {
        Iterator keySetIterator = mapping.keySet().iterator();
        while (keySetIterator.hasNext()) {
            String propertyName = (String) keySetIterator.next();
            if (!ReflectionUtil.hasProperty(source, propertyName)) {
                throw new InvalidUserDataException("You can't map a property that does not exist: propertyName= " + propertyName);
            }
        }
        this.conventionMapping.putAll(mapping);
        return this;
    }

    public Object getConventionValue(String propertyName) {
        Object value = ReflectionUtil.getProperty(source, propertyName);
        return getConventionValue(value, propertyName);
    }

    public <T> T getConventionValue(T internalValue, String propertyName) {
        Object returnValue = internalValue;
        if (internalValue == null && conventionMapping.keySet().contains(propertyName)) {
            if (!conventionMappingCache.keySet().contains(propertyName)) {
                Object conventionValue = conventionMapping.get(propertyName).getValue(convention, source);
                conventionMappingCache.put(propertyName, conventionValue);
            }
            returnValue = conventionMappingCache.get(propertyName);
        }
        return (T) returnValue;
    }

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
    }

    public IConventionAware getSource() {
        return source;
    }

    public void setSource(IConventionAware source) {
        this.source = source;
    }

    public Map getConventionMapping() {
        return conventionMapping;
    }

    public void setConventionMapping(Map conventionMapping) {
        this.conventionMapping = conventionMapping;
    }

    public Map getConventionMappingCache() {
        return conventionMappingCache;
    }

    public void setConventionMappingCache(Map conventionMappingCache) {
        this.conventionMappingCache = conventionMappingCache;
    }
}
