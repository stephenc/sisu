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

import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;

/**
 * Represents a {@link BeanProperty} bound to an optional {@link Provider}.
 */
final class OptionalPropertyBinding<T>
    implements PropertyBinding
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanProperty<T> property;

    private final Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    OptionalPropertyBinding( final BeanProperty<T> property, final Provider<T> provider )
    {
        this.property = property;
        this.provider = provider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> void injectProperty( final B bean )
    {
        try
        {
            property.set( bean, provider.get() );
        }
        catch ( final Throwable e ) // NOPMD
        {
            // binding is optional, ignore failures
        }
    }
}
