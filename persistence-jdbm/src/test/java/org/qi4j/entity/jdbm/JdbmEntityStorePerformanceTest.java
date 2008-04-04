package org.qi4j.entity.jdbm;

import java.io.File;
import org.junit.After;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.structure.Visibility;
import org.qi4j.test.entity.AbstractEntityStorePerformanceTest;

/**
 * Performance test for JdbmEntityStoreComposite
 */
public class JdbmEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( JdbmEntityStoreService.class );

        ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addComposites( JdbmConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreComposite.class );
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();

        boolean deleted = new File( "qi4j.data.db" ).delete();
        deleted = deleted | new File( "qi4j.data.lg" ).delete();
        if( !deleted )
        {
            throw new Exception( "Could not delete test data" );
        }
    }


    @Override @Test
    public void whenNewEntitiesThenPerformanceIsOk() throws Exception
    {
        super.whenNewEntitiesThenPerformanceIsOk();
    }

    @Override @Test
    public void whenBulkNewEntitiesThenPerformanceIsOk() throws Exception
    {
        super.whenBulkNewEntitiesThenPerformanceIsOk();
    }

    @Override @Test
    public void whenFindEntityThenPerformanceIsOk() throws Exception
    {
        super.whenFindEntityThenPerformanceIsOk();
    }
}