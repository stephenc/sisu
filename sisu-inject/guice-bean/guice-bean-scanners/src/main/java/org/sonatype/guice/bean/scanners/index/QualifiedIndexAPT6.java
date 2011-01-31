/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.scanners.index;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Qualifier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SuppressWarnings( "restriction" )
public final class QualifiedIndexAPT6
    extends AbstractQualifiedIndex
    implements Processor
{
    private ProcessingEnvironment environment;

    public void init( final ProcessingEnvironment _environment )
    {
        environment = _environment;
    }

    public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment round )
    {
        for ( final TypeElement anno : annotations )
        {
            if ( null != anno.getAnnotation( Qualifier.class ) )
            {
                for ( final Element elem : round.getElementsAnnotatedWith( anno ) )
                {
                    if ( elem.getKind().isClass() )
                    {
                        updateIndex( anno.getQualifiedName(), ( (TypeElement) elem ).getQualifiedName() );
                    }
                }
            }
        }

        if ( round.processingOver() )
        {
            saveIndex();
        }

        return false;
    }

    public Iterable<? extends Completion> getCompletions( final Element element, final AnnotationMirror annotation,
                                                          final ExecutableElement member, final String userText )
    {
        return Collections.emptyList();
    }

    public Set<String> getSupportedAnnotationTypes()
    {
        return Collections.singleton( "*" );
    }

    public Set<String> getSupportedOptions()
    {
        return Collections.emptySet();
    }

    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.RELEASE_5;
    }

    @Override
    protected void info( final String msg )
    {
        environment.getMessager().printMessage( Diagnostic.Kind.NOTE, msg );
    }

    @Override
    protected void warn( final String msg )
    {
        environment.getMessager().printMessage( Diagnostic.Kind.WARNING, msg );
    }

    @Override
    protected Writer getWriter( final String path )
        throws IOException
    {
        return environment.getFiler().createResource( StandardLocation.CLASS_OUTPUT, "", path ).openWriter();
    }
}
