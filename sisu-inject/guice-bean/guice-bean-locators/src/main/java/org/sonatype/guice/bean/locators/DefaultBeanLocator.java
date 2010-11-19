/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.bean.locators;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Default {@link BeanLocator} that locates qualified beans across a dynamic group of {@link Injector}s.
 */
@Singleton
public final class DefaultBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Provider<? extends QualifiedBeans<?, ?>>> exposedBeans =
        new ArrayList<Provider<? extends QualifiedBeans<?, ?>>>();

    private final Set<Injector> injectors = new LinkedHashSet<Injector>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized Iterable<QualifiedBean> locate( final Key key, final Runnable listener )
    {
        final QualifiedBeans beans = null == listener ? new QualifiedBeans( key ) : new NotifyingBeans( key, listener );
        exposedBeans.add( new WeakBeanReference( initialize( beans ) ) );
        return beans;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public synchronized void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        exposedBeans.add( initialize( new WatchedBeans( key, mediator, watcher ) ) );
    }

    @Inject
    public synchronized void add( final Injector injector )
    {
        if ( null == injector || !injectors.add( injector ) )
        {
            return; // injector already tracked
        }

        Logs.debug( getClass(), "Adding Injector@{}: {}", Integer.toHexString( injector.hashCode() ), injector );

        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.add( injector ); // update exposed sequence to include new injector
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
    }

    public synchronized void remove( final Injector injector )
    {
        if ( null == injector || !injectors.remove( injector ) )
        {
            return; // injector wasn't tracked
        }

        Logs.debug( getClass(), "Removing Injector@{}:", Integer.toHexString( injector.hashCode() ), null );

        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.remove( injector ); // update exposed sequence to ignore old injector
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
    }

    public synchronized void clear()
    {
        Logs.debug( getClass(), "Clearing all Injectors", null, null );

        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final QualifiedBeans<?, ?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.clear();
            }
            else
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
        injectors.clear();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Initializes a sequence of qualified beans based on the current list of {@link Injector}s.
     * 
     * @param beans The beans to initialize
     * @return Passed-in beans; now initialized
     */
    private <B extends QualifiedBeans<?, ?>> B initialize( final B beans )
    {
        for ( final Injector injector : injectors )
        {
            beans.add( injector );
        }
        // take a moment to clear stale bean sequences
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            if ( null == exposedBeans.get( i ).get() )
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }
        return beans;
    }
}
