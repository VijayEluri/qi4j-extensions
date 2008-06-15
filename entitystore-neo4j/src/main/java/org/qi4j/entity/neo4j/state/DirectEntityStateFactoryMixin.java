/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entity.neo4j.state;

import java.util.Iterator;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.qi4j.entity.neo4j.NeoCoreService;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.injection.scope.Service;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectEntityStateFactoryMixin implements NodeEntityStateFactory
{
    private @Service NeoCoreService neo;

    public CommittableEntityState createEntityState( NeoIdentityIndex idIndex, LoadedDescriptor descriptor, QualifiedIdentity identity, EntityStatus status )
    {
        Node node = idIndex.getOrCreateNode( identity.identity() );
        return createEntityState( idIndex, node, descriptor, identity, status );
    }

    public CommittableEntityState createEntityState( NeoIdentityIndex idIndex, Node node, LoadedDescriptor descriptor, QualifiedIdentity identity, EntityStatus status )
    {
        return new DirectEntityState( neo, idIndex, node, identity, status, descriptor );
    }

    public CommittableEntityState loadEntityStateFromNode( NeoIdentityIndex idIndex, Node node )
    {
        QualifiedIdentity identity = DirectEntityState.getIdentityFromNode( node );
        LoadedDescriptor descriptor = LoadedDescriptor.loadDescriptor( idIndex.getTypeNode( identity.type() ) );
        return createEntityState( idIndex, node, descriptor, identity, EntityStatus.LOADED );
    }

    public StateCommitter prepareCommit( NeoIdentityIndex idIndex, Iterable<CommittableEntityState> updated, Iterable<QualifiedIdentity> removed )
    {
        return new StateCommitter()
        {
            public void commit()
            {
            }

            public void cancel()
            {
            }
        };
    }

    public Iterator<CommittableEntityState> iterator( final NeoIdentityIndex idIndex )
    {
        final Iterator<Node> nodes = null; // TODO: get all nodes from Neo
        return new Iterator<CommittableEntityState>()
        {
            Node next = null;
            CommittableEntityState previous = null;

            public boolean hasNext()
            {
                if( next != null )
                {
                    return true;
                }
                while( nodes.hasNext() )
                {
                    Node node = nodes.next();
                    if( node.hasRelationship( DirectEntityState.PROXY_FOR, Direction.OUTGOING ) )
                    {
                        next = node;
                        return true;
                    }
                }
                return false;
            }

            public CommittableEntityState next()
            {
                if( hasNext() )
                {
                    previous = loadEntityStateFromNode( idIndex, next );
                }
                return previous;
            }

            public void remove()
            {
                if( previous != null )
                {
                    previous.remove();
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
        };
    }
}