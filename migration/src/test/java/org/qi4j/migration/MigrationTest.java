/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.migration;

import static org.junit.Assert.*;
import org.junit.Test;
import org.hamcrest.CoreMatchers;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.entitystore.memory.TestData;
import org.qi4j.migration.assembly.MigrationRules;
import java.io.IOException;

/**
 * JAVADOC
 */
public class MigrationTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.addEntities( TestEntity1_0.class,
                            TestEntity1_1.class,
                            TestEntity2_0.class);

        MigrationRules migration = new MigrationRules();
        migration.fromVersion("1.0").

            toVersion("1.1").
                renameEntity(TestEntity1_0.class.getName(), TestEntity1_1.class.getName()).
                forEntities(TestEntity1_1.class.getName()).
                    renameProperty( "foo", "newFoo").

            toVersion( "2.0" ).
                renameEntity(TestEntity1_1.class.getName(), TestEntity2_0.class.getName()).
                forEntities( TestEntity2_0.class.getName() ).
                    addProperty("bar", "Some value").
                    removeProperty( "newFoo", "Some value" );

        module.addServices( MigrationService.class ).setMetaInfo( migration );
    }

    @Test
    public void testMigration() throws UnitOfWorkCompletionException, IOException
    {
        // Set up version 1
        String id;
        String data_v1;
        {
            SingletonAssembler v1 = new SingletonAssembler()
                    {
                        public void assemble( ModuleAssembly module ) throws AssemblyException
                        {
                            MigrationTest.this.assemble( module );
                            module.layerAssembly().applicationAssembly().setVersion( "1.0" );
                        }
                    };

            UnitOfWork uow = v1.unitOfWorkFactory().newUnitOfWork();
            TestEntity1_0 entity = uow.newEntity( TestEntity1_0.class );
            entity.foo().set( "Some value" );
            id = entity.identity().get();
            uow.complete();

            TestData testData = (TestData) v1.module().serviceFinder().findService( TestData.class ).get();
            data_v1 = testData.exportData();
        }

        // Set up version 1.1
        String data_v1_1;
        {
            SingletonAssembler v1_1 = new SingletonAssembler()
                    {
                        public void assemble( ModuleAssembly module ) throws AssemblyException
                        {
                            MigrationTest.this.assemble( module );
                            module.layerAssembly().applicationAssembly().setVersion( "1.1" );
                        }
                    };

            TestData testData = (TestData) v1_1.serviceFinder().findService( TestData.class ).get();
            testData.importData( data_v1 );

            UnitOfWork uow = v1_1.unitOfWorkFactory().newUnitOfWork();
            TestEntity1_1 entity = uow.get( TestEntity1_1.class, id );
            assertThat( "Property has been renamed", entity.newFoo().get(), CoreMatchers.equalTo("Some value" ));
            uow.complete();

            data_v1_1 = testData.exportData();
        }

        // Set up version 2.0
        {
            SingletonAssembler v2_0 = new SingletonAssembler()
                    {
                        public void assemble( ModuleAssembly module ) throws AssemblyException
                        {
                            MigrationTest.this.assemble( module );
                            module.layerAssembly().applicationAssembly().setVersion( "2.0" );
                        }
                    };

            TestData testData = (TestData) v2_0.serviceFinder().findService( TestData.class ).get();
            testData.importData( data_v1_1 );

            {
                UnitOfWork uow = v2_0.unitOfWorkFactory().newUnitOfWork();
                TestEntity2_0 entity = uow.get( TestEntity2_0.class, id );
                assertThat( "Property has been created", entity.bar().get(), CoreMatchers.equalTo("Some value" ));
                uow.complete();
            }

            // Test migration from 1.0 -> 2.0
            {
                testData.importData( data_v1 );
                UnitOfWork uow = v2_0.unitOfWorkFactory().newUnitOfWork();
                TestEntity2_0 entity = uow.get( TestEntity2_0.class, id );
                assertThat( "Property has been created", entity.bar().get(), CoreMatchers.equalTo("Some value" ));
                uow.complete();
            }
        }

    }
}