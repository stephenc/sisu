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
package org.sonatype.guice.bean.reflect;

import java.util.logging.Level;

/**
 * Utility methods for dealing with internal debug and warning messages.
 */
public final class Logs
{
    // ----------------------------------------------------------------------
    // Static initialization
    // ----------------------------------------------------------------------

    static
    {
        String newLine;
        boolean debug;
        try
        {
            newLine = System.getProperty( "line.separator" );
            debug = "true".equalsIgnoreCase( System.getProperty( "org.sonatype.inject.debug" ) );
        }
        catch ( final Throwable e )
        {
            newLine = "\n";
            debug = false;
        }
        NEW_LINE = newLine;
        Sink sink;
        try
        {
            sink = debug ? new ConsoleSink() : new SLF4JSink();
        }
        catch ( final Throwable e )
        {
            sink = new JULSink();
        }
        SINK = sink;
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    public static final String NEW_LINE;

    private static final String SISU = "Sisu";

    private static final Sink SINK;

    private static final boolean DEBUG_ENABLED = SINK.hasDebug();

    private static final String ANCHOR = "{}";

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Logs()
    {
        // static utility class, not allowed to create instances
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Logs a debug message; uses "{}" format anchors. Pass {@link Throwable}s in last parameter for special handling.
     * 
     * @param format The debug message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void debug( final String format, final Object arg1, final Object arg2 )
    {
        if ( DEBUG_ENABLED )
        {
            SINK.debug( format( format( format, arg1 ), arg2 ), arg2 instanceof Throwable ? (Throwable) arg2 : null );
        }
    }

    /**
     * Logs a warning message; uses "{}" format anchors. Pass {@link Throwable}s in last parameter for special handling.
     * 
     * @param format The warning message format
     * @param arg1 First object to format
     * @param arg2 Second object to format
     */
    public static void warn( final String format, final Object arg1, final Object arg2 )
    {
        SINK.warn( format( format( format, arg1 ), arg2 ), arg2 instanceof Throwable ? (Throwable) arg2 : null );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Replaces the first available formatting anchor with the given object.
     * 
     * @param format The format string
     * @param arg The object to format
     */
    private static String format( final String format, final Object arg )
    {
        int cursor = format.indexOf( ANCHOR );
        if ( cursor < 0 )
        {
            return format;
        }
        final StringBuilder buf = new StringBuilder();
        if ( cursor > 0 )
        {
            buf.append( format.substring( 0, cursor ) );
        }
        try
        {
            buf.append( arg );
        }
        catch ( final Throwable e )
        {
            buf.append( arg.getClass() );
        }
        cursor += ANCHOR.length();
        if ( cursor < format.length() )
        {
            buf.append( format.substring( cursor, format.length() ) );
        }
        return buf.toString();
    }

    private interface Sink
    {
        boolean hasDebug();

        void debug( String message, Throwable cause );

        void warn( String message, Throwable cause );
    }

    static final class ConsoleSink
        implements Sink
    {
        private static final String DEBUG = "DEBUG: " + SISU + " - ";

        private static final String WARN = "WARN: " + SISU + " - ";

        public boolean hasDebug()
        {
            return true;
        }

        public void debug( final String message, final Throwable cause )
        {
            System.out.println( DEBUG + message );
            if ( null != cause )
            {
                cause.printStackTrace( System.out );
            }
        }

        public void warn( final String message, final Throwable cause )
        {
            System.err.println( WARN + message );
            if ( null != cause )
            {
                cause.printStackTrace( System.err );
            }
        }
    }

    static final class JULSink
        implements Sink
    {
        private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger( SISU );

        public boolean hasDebug()
        {
            return logger.isLoggable( Level.FINE );
        }

        public void debug( final String message, final Throwable cause )
        {
            logger.log( Level.FINE, message, cause );
        }

        public void warn( final String message, final Throwable cause )
        {
            logger.log( Level.WARNING, message, cause );
        }
    }

    static final class SLF4JSink
        implements Sink
    {
        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger( SISU );

        public boolean hasDebug()
        {
            return logger.isDebugEnabled();
        }

        public void debug( final String message, final Throwable cause )
        {
            logger.debug( message, cause );
        }

        public void warn( final String message, final Throwable cause )
        {
            logger.warn( message, cause );
        }
    }
}
