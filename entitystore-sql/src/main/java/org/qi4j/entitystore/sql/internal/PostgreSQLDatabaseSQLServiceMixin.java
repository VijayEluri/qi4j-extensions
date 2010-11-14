/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
package org.qi4j.entitystore.sql.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.qi4j.api.injection.scope.This;
import org.qi4j.library.sql.common.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.vendor.PostgreSQLVendor;

@SuppressWarnings("ProtectedField")
public abstract class PostgreSQLDatabaseSQLServiceMixin
    implements DatabaseSQLServiceSpi, DatabaseSQLStringsBuilder, DatabaseSQLService
{

    private static final Logger LOGGER = LoggerFactory.getLogger( PostgreSQLDatabaseSQLServiceMixin.class );

    @This
    protected DatabaseSQLServiceSpi spi;

    public boolean tableExists( Connection connection )
        throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = connection.getMetaData().getTables( null, this.spi.getCurrentSchemaName(), SQLs.TABLE_NAME,
                new String[]
                {
                    "TABLE"
                } );
            boolean tableExists = rs.next();
            LOGGER.trace( "Found table {}? {}", SQLs.TABLE_NAME, tableExists );
            return tableExists;

        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }
    }

    public EntityValueResult getEntityValue( ResultSet rs )
        throws SQLException
    {
        return new EntityValueResult( rs.getLong( SQLs.ENTITY_PK_COLUMN_NAME ),
            rs.getLong( SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME ),
            rs.getCharacterStream( SQLs.ENTITY_STATE_COLUMN_NAME ) );
    }

}