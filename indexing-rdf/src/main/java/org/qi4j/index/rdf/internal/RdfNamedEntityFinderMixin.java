/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf.internal;

import java.util.Map;
import org.openrdf.query.QueryLanguage;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.index.rdf.RdfFactory;
import org.qi4j.index.rdf.callback.CollectingQualifiedIdentityResultCallback;
import org.qi4j.index.rdf.callback.SingleQualifiedIdentityResultCallback;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;


public class RdfNamedEntityFinderMixin
    implements NamedEntityFinder
{
    @Service private RdfFactory factory;
    @This private TupleQueryExecutor tupleExecutor;

    private NamedQueries queriesAvailable;

    public RdfNamedEntityFinderMixin( @This Composite me )
    {
        queriesAvailable = me.metaInfo( NamedQueries.class );
    }

    public Iterable<EntityReference> findEntities( String name,
                                                   String resultType,
                                                   Map<String, Object> variables,
                                                   OrderBy[] orderBySegments,
                                                   Integer firstResult,
                                                   Integer maxResults )
        throws EntityFinderException
    {
        NamedQueryDescriptor decl = queriesAvailable.getQuery( name );
        QueryLanguage queryLanguage = QueryLanguage.valueOf( decl.language() );
        String query = decl.compose( variables, orderBySegments, firstResult, maxResults );
        CollectingQualifiedIdentityResultCallback callback = new CollectingQualifiedIdentityResultCallback();
        tupleExecutor.performTupleQuery( queryLanguage, query, callback );
        return callback.getEntities();
    }

    public EntityReference findEntity( String name, String resultType, Map<String, Object> variables )
        throws EntityFinderException
    {
        NamedQueryDescriptor decl = queriesAvailable.getQuery( name );
        QueryLanguage queryLanguage = QueryLanguage.valueOf( decl.language() );
        String query = decl.compose( variables, null, null, 1 );
        SingleQualifiedIdentityResultCallback callback = new SingleQualifiedIdentityResultCallback();
        tupleExecutor.performTupleQuery( queryLanguage, query, callback );
        return callback.getQualifiedIdentity();
    }

    public long countEntities( String name, String resultType, Map<String, Object> variables )
        throws EntityFinderException
    {
        NamedQueryDescriptor decl = queriesAvailable.getQuery( name );
        QueryLanguage queryLanguage = QueryLanguage.valueOf( decl.language() );
        return tupleExecutor.performTupleQuery( queryLanguage, decl.compose( null, null, null, null ), null );
    }

    public String showQuery( String queryName )
    {
        NamedQueryDescriptor decl = queriesAvailable.getQuery( queryName );
        return decl.compose( null, null, null, null );
    }
}