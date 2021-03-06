/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.plexus.binders;

import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for properties with @{@link Configuration} metadata.
 */
final class PlexusConfigurations
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Provider<PlexusBeanConverter> converterProvider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusConfigurations( final TypeEncounter<?> encounter )
    {
        converterProvider = encounter.getProvider( PlexusBeanConverter.class );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides values that match the given property configuration.
     * 
     * @param configuration The Plexus configuration
     * @param property The bean property
     * @return Provider that provides configured values for the given property
     */
    public <T> Provider<T> lookup( final Configuration configuration, final BeanProperty<T> property )
    {
        return new ConfigurationProvider<T>( converterProvider, property.getType(), configuration.value() );
    }

    // ----------------------------------------------------------------------
    // Implementation classes
    // ----------------------------------------------------------------------

    /**
     * {@link Provider} of Plexus configurations.
     */
    private static final class ConfigurationProvider<T>
        implements Provider<T>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Provider<PlexusBeanConverter> converterProvider;

        private final TypeLiteral<T> type;

        private final String value;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ConfigurationProvider( final Provider<PlexusBeanConverter> converterProvider, final TypeLiteral<T> type,
                               final String value )
        {
            this.converterProvider = converterProvider;

            this.type = type;
            this.value = value;
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public T get()
        {
            return converterProvider.get().convert( type, value );
        }
    }
}
