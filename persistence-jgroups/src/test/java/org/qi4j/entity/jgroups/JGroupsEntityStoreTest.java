/*  Copyright 2008 Rickard �berg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.qi4j.entity.jgroups;

import org.junit.After;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test of JGroups EntityStore backend.
 */
public class JGroupsEntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( JGroupsEntityStoreComposite.class, UuidIdentityGeneratorComposite.class );
        module.addComposites( TestComposite.class );
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void whenNewEntityThenFindEntity()
        throws Exception
    {
        String id = createEntity( null );
        UnitOfWork unitOfWork;
        TestComposite instance;

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        instance = unitOfWork.find( id, TestComposite.class );
        org.junit.Assert.assertThat( "property has correct value", instance.name().get(), org.hamcrest.CoreMatchers.equalTo( "Rickard" ) );
        unitOfWork.discard();
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        String id = createEntity( null );

        // Remove entity
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestComposite instance = unitOfWork.find( id, TestComposite.class );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            instance = unitOfWork.find( id, TestComposite.class );
            org.junit.Assert.fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        unitOfWork.discard();
    }

    @Test
    public void whenNewEntityThenFindInReplica()
        throws Exception
    {
        // Create first app
        SingletonAssembler app1 = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( JGroupsEntityStoreComposite.class, UuidIdentityGeneratorComposite.class ).activateOnStartup();
                module.addComposites( TestComposite.class );
            }
        };

        // Create second app
        SingletonAssembler app2 = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( JGroupsEntityStoreComposite.class, UuidIdentityGeneratorComposite.class ).activateOnStartup();
                module.addComposites( TestComposite.class );
            }
        };

        // Create entity in app 1
        System.out.println( "Create entity" );
        UnitOfWork app1Unit = app1.getUnitOfWorkFactory().newUnitOfWork();
        TestComposite instance = app1Unit.newEntityBuilder( TestComposite.class ).newInstance();
        instance.name().set( "Foo" );
        app1Unit.complete();

//        Thread.sleep( 5000 );

        // Find entity in app 2
        System.out.println( "Find entity" );
        UnitOfWork app2Unit = app2.getUnitOfWorkFactory().newUnitOfWork();
        instance = app2Unit.getReference( instance );

        System.out.println( instance.name() );

    }

    @Test
    public void whenNewEntitiesThenPerformanceIsOk()
        throws Exception
    {
        long start = System.currentTimeMillis();

        int nrOfEntities = 10000;
        for( int i = 0; i < nrOfEntities; i++ )
        {
            createEntity( null );
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( end - start );
        System.out.println( nrOfEntities / ( time / 1000.0D ) );
    }

    @Test
    public void whenBulkNewEntitiesThenPerformanceIsOk()
        throws Exception
    {
        int nrOfEntities = 10000;
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        long start = System.currentTimeMillis();

        for( int i = 0; i < nrOfEntities; i++ )
        {
            // Create entity
            CompositeBuilder<TestComposite> builder = unitOfWork.newEntityBuilder( TestComposite.class );
            builder.propertiesOfComposite().name().set( "Rickard" );
            TestComposite instance = builder.newInstance();
        }

        unitOfWork.complete();
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( end - start );
        System.out.println( nrOfEntities / ( time / 1000.0D ) );
    }

    @Test
    public void whenFindEntityThenPerformanceIsOk()
        throws Exception
    {
        long start = System.currentTimeMillis();

        String id = createEntity( null );

        int nrOfLookups = 10000;
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        for( int i = 0; i < nrOfLookups; i++ )
        {
            TestComposite instance = unitOfWork.find( id, TestComposite.class );
            unitOfWork.clear();
        }
        unitOfWork.discard();

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( time );
        System.out.println( nrOfLookups / ( time / 1000.0D ) );
    }

    private String createEntity( String id )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        CompositeBuilder<TestComposite> builder = unitOfWork.newEntityBuilder( id, TestComposite.class );
        builder.propertiesOfComposite().name().set( "Rickard" );
        TestComposite instance = builder.newInstance();
        id = instance.identity().get();
        unitOfWork.complete();
        return id;
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface TestComposite
        extends EntityComposite
    {
        Property<String> name();
    }
}