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
package org.sonatype.guice.plexus.locators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public final class ClassRealmUtils
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        boolean getImportRealmsSupported = true;
        try
        {
            // support both old and new forms of Plexus class realms
            ClassRealm.class.getDeclaredMethod( "getImportRealms" );
        }
        catch ( final Throwable e )
        {
            getImportRealmsSupported = false;
        }
        GET_IMPORT_REALMS_SUPPORTED = getImportRealmsSupported;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean GET_IMPORT_REALMS_SUPPORTED;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private ClassRealmUtils()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static Map<ClassRealm, RealmInfo> realmCache = new WeakHashMap<ClassRealm, RealmInfo>();

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static ClassRealm contextRealm()
    {
        for ( ClassLoader tccl = Thread.currentThread().getContextClassLoader(); tccl != null; tccl = tccl.getParent() )
        {
            if ( tccl instanceof ClassRealm )
            {
                return (ClassRealm) tccl;
            }
        }
        return null;
    }

    public static Set<String> visibleRealmNames( final ClassRealm contextRealm )
    {
        if ( false == GET_IMPORT_REALMS_SUPPORTED || null == contextRealm )
        {
            return Collections.emptySet();
        }

        synchronized ( realmCache )
        {
            RealmInfo realmInfo = realmCache.get( contextRealm );
            if ( null == realmInfo )
            {
                realmInfo = new RealmInfo( contextRealm );
                realmCache.put( contextRealm, realmInfo );
            }
            return realmInfo.getVisibleRealmNames();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class RealmInfo
    {
        private final Set<String> visibleRealmNames = new HashSet<String>();

        @SuppressWarnings( "unchecked" )
        RealmInfo( final ClassRealm forRealm )
        {
            final List<ClassRealm> searchRealms = new ArrayList<ClassRealm>();

            searchRealms.add( forRealm );
            for ( int i = 0; i < searchRealms.size(); i++ )
            {
                final ClassRealm realm = searchRealms.get( i );
                if ( visibleRealmNames.add( realm.toString() ) )
                {
                    searchRealms.addAll( realm.getImportRealms() );
                    final ClassRealm parent = realm.getParentRealm();
                    if ( null != parent )
                    {
                        searchRealms.add( parent );
                    }
                }
            }
        }

        public Set<String> getVisibleRealmNames()
        {
            return visibleRealmNames;
        }
    }
}
