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

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link BeanLocator} that locates qualified beans across a dynamic group of {@link Injector}s.
 */
@Singleton
public final class GuiceBeanLocator
    implements BeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Injector> injectors = new LinkedHashSet<Injector>();

    private final List<Reference<GuiceBeans<?, ?>>> exposedBeans = new ArrayList<Reference<GuiceBeans<?, ?>>>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public GuiceBeanLocator()
    {
        // no injectors
    }

    @Inject
    public GuiceBeanLocator( final Injector injector )
    {
        add( injector );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized <Q extends Annotation, T> Iterable<Entry<Q, T>> locate( final Key<T> key )
    {
        final GuiceBeans<Q, T> beans = new GuiceBeans<Q, T>( key );
        for ( final Injector injector : injectors )
        {
            beans.add( injector );
        }

        // check for any unused (GC'd) bean sequences
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            if ( null == exposedBeans.get( i ).get() )
            {
                exposedBeans.remove( i-- ); // sequence GC'd, so no need to track anymore
            }
        }

        // remember exposed sequences so we can update them in the future
        exposedBeans.add( new WeakReference<GuiceBeans<?, ?>>( beans ) );

        return beans;
    }

    public <Q extends Annotation, T, W> void watch( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        // TODO: support proper tracking
        for ( final Injector injector : injectors )
        {
            final InjectorBeans<Q, T> beans = new InjectorBeans<Q, T>( injector, key );
            for ( int i = 0, size = beans.size(); i < size; i++ )
            {
                mediator.add( beans.get( i ), watcher );
            }
        }
    }

    /**
     * Adds qualified beans from the given injector to any exported qualified bean sequences.
     * 
     * @param injector The new injector
     */
    public synchronized void add( final Injector injector )
    {
        if ( null == injector || !injectors.add( injector ) )
        {
            return; // injector already tracked
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final GuiceBeans<?, ?> beans = exposedBeans.get( i ).get();
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

    /**
     * Removes qualified beans from the given injector from any exported qualified bean sequences.
     * 
     * @param injector The old injector
     */
    public synchronized void remove( final Injector injector )
    {
        if ( null == injector || !injectors.remove( injector ) )
        {
            return; // injector wasn't tracked
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final GuiceBeans<?, ?> beans = exposedBeans.get( i ).get();
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
}
