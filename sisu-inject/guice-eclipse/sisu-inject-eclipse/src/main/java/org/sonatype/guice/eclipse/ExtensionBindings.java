package org.sonatype.guice.eclipse;

import java.security.ProviderException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.locators.spi.BindingExporter;
import org.sonatype.guice.bean.locators.spi.BindingImporter;
import org.sonatype.inject.EagerSingleton;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderInstanceBinding;

@Named
@EagerSingleton
final class ExtensionBindings
    implements BindingExporter
{
    private final IExtensionRegistry registry;

    @Inject
    ExtensionBindings( final MutableBeanLocator locator )
    {
        registry = RegistryFactory.getRegistry();
        locator.publish( this, -1 );
    }

    public <T> void publish( final TypeLiteral<T> type, final BindingImporter importer )
    {
        final Class<?> clazz = type.getRawType();
        final String pointId = clazz.getPackage().getName(); // FIXME
        for ( final IConfigurationElement config : registry.getConfigurationElementsFor( pointId ) )
        {
            try
            {
                final String name = config.getAttribute( "class" );
                if ( null != name )
                {
                    if ( type.getRawType().isAssignableFrom( loadExtensionClass( config, name ) ) )
                    {
                        importer.publish( new ExtensionBinding<T>( type, config ), 0 );
                    }
                }
            }
            catch ( final Throwable e )
            {
                // ignore
            }
        }
    }

    public <T> void remove( final TypeLiteral<T> type, final BindingImporter importer )
    {
        // TODO Auto-generated method stub
    }

    private static final class ExtensionBinding<T>
        implements ProviderInstanceBinding<T>
    {
        private final TypeLiteral<T> type;

        final IConfigurationElement config;

        ExtensionBinding( final TypeLiteral<T> type, final IConfigurationElement config )
        {
            this.type = type;
            this.config = config;
        }

        public Key<T> getKey()
        {
            return Key.get( type );
        }

        @SuppressWarnings( "unchecked" )
        public Provider<T> getProvider()
        {
            return new Provider<T>()
            {
                public T get()
                {
                    try
                    {
                        return (T) config.createExecutableExtension( "class" );
                    }
                    catch ( final CoreException e )
                    {
                        throw new ProviderException( e );
                    }
                }
            };
        }

        public Object getSource()
        {
            return config;
        }

        public Set<Dependency<?>> getDependencies()
        {
            return Collections.emptySet();
        }

        public Set<InjectionPoint> getInjectionPoints()
        {
            return Collections.emptySet();
        }

        public Provider<T> getProviderInstance()
        {
            return getProvider();
        }

        public void applyTo( Binder binder )
        {
            binder.bind( getKey() ).toProvider( getProvider() );
        }

        public <S> S acceptVisitor( ElementVisitor<S> visitor )
        {
            return visitor.visit( this );
        }

        public <V> V acceptScopingVisitor( BindingScopingVisitor<V> visitor )
        {
            return visitor.visitNoScoping();
        }

        public <V> V acceptTargetVisitor( BindingTargetVisitor<? super T, V> visitor )
        {
            return visitor.visit( this );
        }
    }

    static Class<?> loadExtensionClass( final IConfigurationElement config, final String clazzName )
        throws ClassNotFoundException
    {
        final Bundle bundle = ContributorFactoryOSGi.resolve( config.getContributor() );

        String value = clazzName;
        int n = value.indexOf( ':' );

        return bundle.loadClass( n < 0 ? value : value.substring( 0, n ) );
    }
}
