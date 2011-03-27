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
package org.sonatype.guice.bean.scanners;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;
import org.sonatype.guice.bean.scanners.asm.Opcodes;

/**
 * {@link ClassSpaceVisitor} that reports types annotated with {@link Qualifier} annotations.
 */
public final class QualifiedTypeVisitor
    extends EmptyClassVisitor
    implements ClassSpaceVisitor
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifierCache qualifierCache = new QualifierCache();

    private final List<Class<Annotation>> qualifiers = new ArrayList<Class<Annotation>>();

    private final QualifiedTypeListener listener;

    private ClassSpace space;

    private URL location;

    private String source;

    private String clazzName;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedTypeVisitor( final QualifiedTypeListener listener )
    {
        this.listener = listener;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final ClassSpace _space )
    {
        space = _space;
        source = null;
    }

    public ClassVisitor visitClass( final URL url )
    {
        qualifiers.clear();

        location = url;
        clazzName = null;

        return this;
    }

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        if ( ( access & ( Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC ) ) == 0 )
        {
            clazzName = name; // concrete type
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( null != clazzName )
        {
            final Class<Annotation> qualifier = qualifierCache.qualify( space, desc );
            if ( null != qualifier )
            {
                qualifiers.add( qualifier );
            }
        }
        return null;
    }

    @Override
    public void visitEnd()
    {
        final int numQualifiers = qualifiers.size();
        if ( numQualifiers > 0 )
        {
            // compressed record of class location
            final String path = location.getPath();
            if ( null == source || !path.startsWith( source ) )
            {
                final int i = path.indexOf( clazzName );
                source = i <= 0 ? path : path.substring( 0, i );
            }

            final Class<?> clazz = space.loadClass( clazzName.replace( '/', '.' ) );
            for ( int i = 0; i < numQualifiers; i++ )
            {
                listener.hear( clazz.getAnnotation( qualifiers.get( i ) ), clazz, source );
            }
        }
    }
}