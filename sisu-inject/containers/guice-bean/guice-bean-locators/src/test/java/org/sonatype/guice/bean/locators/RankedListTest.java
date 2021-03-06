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
package org.sonatype.guice.bean.locators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

public class RankedListTest
    extends TestCase
{
    static final AtomicBoolean active = new AtomicBoolean( true );

    static final List<Throwable> errors = Collections.synchronizedList( new ArrayList<Throwable>() );

    static final Random random = new Random( System.currentTimeMillis() );

    static final int CONCURRENCY = 3;

    static final RankedList<Integer> rankedList = new RankedList<Integer>();

    public void testOrdering()
    {
        final RankedList<String> list = new RankedList<String>();

        list.insert( "A1", Integer.MAX_VALUE );
        list.insert( "F", Integer.MIN_VALUE + 1 );
        list.insert( "E", -1 );
        list.insert( "G1", Integer.MIN_VALUE );
        list.insert( "D", 0 );
        list.insert( "B", Integer.MAX_VALUE - 1 );
        list.insert( "C", 1 );
        list.insert( "G2", Integer.MIN_VALUE );
        list.insert( "D", 0 );
        list.insert( "A2", Integer.MAX_VALUE );

        assertEquals( 10, list.size() );

        Iterator<String> itr = list.iterator();

        assertTrue( itr.hasNext() );
        assertTrue( itr.hasNext() );

        assertTrue( itr.hasNext() );
        assertEquals( "A1", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "A2", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "B", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "C", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "D", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "D", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "E", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "F", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "G1", itr.next() );
        assertTrue( itr.hasNext() );
        assertEquals( "G2", itr.next() );
        assertFalse( itr.hasNext() );

        assertFalse( itr.hasNext() );
        assertFalse( itr.hasNext() );

        itr = list.iterator();

        assertEquals( "A1", itr.next() );
        assertEquals( "A2", itr.next() );
        assertEquals( "B", itr.next() );
        assertEquals( "C", itr.next() );
        assertEquals( "D", itr.next() );
        assertEquals( "D", itr.next() );
        assertEquals( "E", itr.next() );
        assertEquals( "F", itr.next() );
        assertEquals( "G1", itr.next() );
        assertEquals( "G2", itr.next() );

        itr = list.iterator();

        list.remove( 4 );
        list.insert( "D-", 0 );
        list.remove( 4 );
        assertEquals( "A1", itr.next() );
        assertEquals( "A2", itr.next() );
        assertTrue( itr.hasNext() );
        list.remove( 2 );
        list.insert( "512", 512 );
        assertEquals( "B", itr.next() );
        assertTrue( itr.hasNext() );
        list.insert( "1024", 1024 );
        assertEquals( "512", itr.next() );
        assertEquals( "C", itr.next() );
        assertEquals( "D-", itr.next() );
        assertEquals( "E", itr.next() );
        assertEquals( "F", itr.next() );
        assertEquals( "G1", itr.next() );
        assertEquals( "G2", itr.next() );
    }

    public void testRankQuery()
    {
        final RankedList<String> list = new RankedList<String>();

        assertEquals( 0, list.size() );

        list.insert( "G", Integer.MIN_VALUE );
        list.insert( "D", 0 );
        list.insert( "B", Integer.MAX_VALUE - 1 );
        list.insert( "C", 1 );
        list.insert( "F", Integer.MIN_VALUE + 1 );
        list.insert( "E", -1 );
        list.insert( "A", Integer.MAX_VALUE );

        assertEquals( 7, list.size() );

        assertEquals( Integer.MAX_VALUE, list.getRank( 0 ) );
        assertEquals( Integer.MIN_VALUE, list.getRank( 6 ) );

        assertFalse( list.isEmpty() );
        list.clear();
        assertTrue( list.isEmpty() );

        assertEquals( 0, list.size() );

        list.insert( "G", Integer.MIN_VALUE );
        list.insert( "D", 0 );
        list.insert( "B", Integer.MAX_VALUE - 1 );
        list.insert( "C", 1 );
        list.insert( "F", Integer.MIN_VALUE + 1 );
        list.insert( "E", -1 );
        list.insert( "A", Integer.MAX_VALUE );

        assertEquals( 7, list.size() );
        assertEquals( Integer.MAX_VALUE, list.getRank( 0 ) );
        assertEquals( Integer.MIN_VALUE, list.getRank( 6 ) );
        list.remove( 0 );

        assertEquals( 6, list.size() );
        assertEquals( Integer.MAX_VALUE - 1, list.getRank( 0 ) );
        assertEquals( Integer.MIN_VALUE, list.getRank( 5 ) );
        list.remove( 5 );

        assertEquals( 5, list.size() );
        assertEquals( Integer.MAX_VALUE - 1, list.getRank( 0 ) );
        assertEquals( Integer.MIN_VALUE + 1, list.getRank( 4 ) );
        list.remove( 0 );

        assertEquals( 4, list.size() );
        assertEquals( 1, list.getRank( 0 ) );
        assertEquals( Integer.MIN_VALUE + 1, list.getRank( 3 ) );
        list.remove( 3 );

        assertEquals( 3, list.size() );
        assertEquals( 1, list.getRank( 0 ) );
        assertEquals( -1, list.getRank( 2 ) );
        list.remove( 0 );

        assertEquals( 2, list.size() );
        assertEquals( 0, list.getRank( 0 ) );
        assertEquals( -1, list.getRank( 1 ) );
        list.remove( 1 );

        assertEquals( 1, list.size() );
        assertEquals( 0, list.getRank( 0 ) );
        list.remove( 0 );

        assertEquals( 0, list.size() );
    }

    public void testIndexing()
    {
        final RankedList<String> list = new RankedList<String>();

        final String markerString = new String( "A" );

        list.insert( new String( "A" ), 1 );
        list.insert( new String( "A" ), 3 );
        list.insert( markerString, 2 );

        assertEquals( 0, list.indexOf( new String( "A" ) ) );
        assertEquals( 0, list.indexOf( markerString ) );

        assertEquals( -1, list.indexOfThis( new String( "A" ) ) );
        assertEquals( 1, list.indexOfThis( markerString ) );
    }

    public void testEmptyList()
    {
        final Iterator<Object> itr = new RankedList<Object>().iterator();

        assertFalse( itr.hasNext() );

        try
        {
            itr.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
            // expected
        }

        try
        {
            itr.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }
    }

    public void testBounds()
    {
        final RankedList<Object> list = new RankedList<Object>();

        list.insert( "Test", 42 );

        try
        {
            list.remove( -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        try
        {
            list.get( -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        try
        {
            list.getRank( -1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        assertEquals( "Test", list.get( 0 ) );
        assertEquals( 42, list.getRank( 0 ) );

        try
        {
            list.remove( 1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        try
        {
            list.get( 1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        try
        {
            list.getRank( 1 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        assertEquals( "Test", list.remove( 0 ) );

        try
        {
            list.remove( 0 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        try
        {
            list.get( 0 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }

        try
        {
            list.getRank( 0 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            // expected
        }
    }

    public void testCloneable()
    {
        final RankedList<String> list = new RankedList<String>();

        list.insert( "X", -1 );
        list.insert( "Y", 0 );
        list.insert( "Z", 1 );

        final RankedList<String> clone = list.clone();

        clone.insert( clone.remove( 0 ), -1 );
        clone.insert( clone.remove( 1 ), 1 );

        assertEquals( "Z", list.remove( 0 ) );
        assertEquals( "Y", list.remove( 0 ) );
        assertEquals( "X", list.remove( 0 ) );
        assertTrue( list.isEmpty() );

        assertEquals( "X", clone.remove( 0 ) );
        assertEquals( "Y", clone.remove( 0 ) );
        assertEquals( "Z", clone.remove( 0 ) );
        assertTrue( clone.isEmpty() );
    }

    @SuppressWarnings( "deprecation" )
    public void testUnsupportedMethods()
    {
        final RankedList<Object> list = new RankedList<Object>();

        try
        {
            list.add( 0, null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        try
        {
            list.addAll( 0, null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        try
        {
            list.set( 0, null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        try
        {
            list.lastIndexOf( null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        try
        {
            list.listIterator();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        try
        {
            list.listIterator( 0 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }

        try
        {
            list.subList( 0, 0 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
            // expected
        }
    }

    public void testConcurrentIteration()
    {
        final Thread[] threads = new Thread[3 * CONCURRENCY];

        int i = 0;
        while ( i < threads.length )
        {
            threads[i++] = new Thread( new Push() );
            threads[i++] = new Thread( new Pull() );
            threads[i++] = new Thread( new Read() );
        }

        for ( final Thread t : threads )
        {
            t.start();
        }

        try
        {
            Thread.sleep( 10 * 1000 );
        }
        catch ( final InterruptedException e )
        {
            // ignore
        }

        active.set( false );

        for ( final Thread t : threads )
        {
            try
            {
                t.join();
            }
            catch ( final InterruptedException e )
            {
                // ignore
            }
        }

        for ( final Throwable e : errors )
        {
            e.printStackTrace();
        }
        if ( errors.size() > 0 )
        {
            fail( "Unexpected errors!" );
        }
    }

    static class Push
        implements Runnable
    {
        public void run()
        {
            while ( active.get() )
            {
                Thread.yield();
                final int rank = random.nextInt();
                synchronized ( rankedList )
                {
                    rankedList.insert( Integer.valueOf( rank ), rank );
                }
                Thread.yield();
            }
        }
    }

    static class Pull
        implements Runnable
    {
        public void run()
        {
            try
            {
                while ( active.get() || !rankedList.isEmpty() )
                {
                    Thread.yield();
                    synchronized ( rankedList )
                    {
                        if ( !rankedList.isEmpty() )
                        {
                            rankedList.remove( random.nextInt( rankedList.size() ) );
                        }
                    }
                    Thread.yield();
                }
            }
            catch ( final Throwable e )
            {
                errors.add( e );
            }
        }
    }

    static class Read
        implements Runnable
    {
        public void run()
        {
            try
            {
                while ( active.get() )
                {
                    Thread.yield();
                    int lastRank = Integer.MAX_VALUE;
                    final Iterator<Integer> itr = rankedList.iterator();
                    while ( itr.hasNext() )
                    {
                        Thread.yield();
                        final int rank = itr.next().intValue();
                        Thread.yield();
                        assertTrue( "Rank should descend during iteration", lastRank >= rank );
                        lastRank = rank;
                    }
                    Thread.yield();
                }
            }
            catch ( final Throwable e )
            {
                errors.add( e );
            }
        }
    }
}
