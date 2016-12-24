/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.common;

import com.datumbox.framework.common.concurrency.ConcurrencyConfiguration;
import com.datumbox.framework.common.interfaces.Configurable;
import com.datumbox.framework.common.persistentstorage.interfaces.StorageConfiguration;

import java.util.Properties;

/**
 * The main Configuration object of the framework which information about the storage, the concurrency etc.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Configuration implements Configurable {
    
    private StorageConfiguration storageConfiguration;
    private ConcurrencyConfiguration concurrencyConfiguration;
    
    /**
     * Protected constructor. Use the static getConfiguration method instead.
     */
    protected Configuration() {
        
    }
    
    /**
     * Getter for the Storage Configuration object.
     * 
     * @return 
     */
    public StorageConfiguration getStorageConfiguration() {
        return storageConfiguration;
    }
    
    /**
     * Setter for the Storage Configuration object.
     * 
     * @param storageConfiguration
     */
    public void setStorageConfiguration(StorageConfiguration storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
    }
    
    /**
     * Getter for the Concurrency Configuration object.
     * 
     * @return 
     */
    public ConcurrencyConfiguration getConcurrencyConfuration() {
        return concurrencyConfiguration;
    }
    
    /**
     * Setter for the Concurrency Configuration object.
     * 
     * @param concurrencyConfiguration
     */
    public void setConcurrencyConfiguration(ConcurrencyConfiguration concurrencyConfiguration) {
        this.concurrencyConfiguration = concurrencyConfiguration;
    }
    
    /** {@inheritDoc} */
    @Override
    public void load(Properties properties) {
        String storageConfClassName = properties.getProperty("storageConfiguration.className");
        try {
            storageConfiguration = ConfigurableFactory.getConfiguration((Class<StorageConfiguration>) Class.forName(storageConfClassName));
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        concurrencyConfiguration = ConfigurableFactory.getConfiguration(ConcurrencyConfiguration.class);
    }
    
    /**
     * Creates a new configuration object based on the property file.
     * 
     * @return 
     */
    public static Configuration getConfiguration() {
        return ConfigurableFactory.getConfiguration(Configuration.class);
    }
    
}
