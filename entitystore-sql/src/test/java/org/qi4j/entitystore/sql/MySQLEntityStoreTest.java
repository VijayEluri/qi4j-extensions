/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql;

import org.junit.Ignore;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.assembly.MySQLEntityStoreAssembler;
import org.qi4j.entitystore.sql.internal.SQLs;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.test.entity.AbstractEntityStoreTest;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
@Ignore
// DO NOT WORK AS MYSQL DON'T SUPPORT SCHEMAS ...
public class MySQLEntityStoreTest extends AbstractEntityStoreTest
{

    @Override
    @SuppressWarnings("unchecked")
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        new MySQLEntityStoreAssembler().assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        config.services( MemoryEntityStoreService.class );
        config.entities( SQLConfiguration.class ).visibleIn( Visibility.layer );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        UnitOfWork uow = this.module.newUnitOfWork();
        try
        {
            SQLConfiguration config = uow.get( SQLConfiguration.class,
                MySQLEntityStoreAssembler.DATASOURCE_SERVICE_NAME );
            Connection connection = SQLUtil.getConnection( module );
            String schemaName = config.schemaName().get();
            if( schemaName == null )
            {
                schemaName = SQLs.DEFAULT_SCHEMA_NAME;
            }

            Statement stmt = null;
            try
            {
                stmt = connection.createStatement();
                stmt.execute( String.format( "DELETE FROM %s." + SQLs.TABLE_NAME, schemaName ) );
                connection.commit();
            }
            finally
            {
                SQLUtil.closeQuietly( stmt );
            }

        }
        finally
        {
            uow.discard();
            super.tearDown();
        }
    }

}
