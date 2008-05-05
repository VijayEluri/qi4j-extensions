/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.entity.ibatis.internal.association;

import java.lang.reflect.Type;
import static org.qi4j.composite.NullArgumentException.*;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.spi.entity.association.AssociationBinding;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.entity.association.AssociationResolution;

/**
 * {@code IBatisAbstractAssociationInstance} provides implementation of {@code AbstractAssociation}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public abstract class IBatisAbstractAssociationInstance implements AbstractAssociation
{
    protected final AssociationBinding associationBinding;

    /**
     * Construct an instance of {@code IBatisAbstractAssociationInstance}.
     *
     * @param aBinding The binding. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@aBinding} argument is {@code null}.
     * @since 0.1.0
     */
    IBatisAbstractAssociationInstance( AssociationBinding aBinding )
        throws IllegalArgumentException
    {
        validateNotNull( "aBinding", aBinding );
        associationBinding = aBinding;
    }

    /**
     * Returns the association info. Must not return {@code null}.
     *
     * @param infoType The info type.
     * @return Returns the association info.
     * @since 0.1.0
     */
    public final <T> T metaInfo( Class<T> infoType )
    {
        return associationBinding.metaInfo( infoType );
    }

    /**
     * Returns the association name. Must not return {@code null}.
     *
     * @return The association name.
     * @since 0.1.0
     */
    public final String name()
    {
        AssociationResolution associationResolution = associationBinding.getAssociationResolution();
        AssociationModel model = associationResolution.getAssociationModel();
        return associationBinding.name();
    }

    /**
     * Returns the association qualified name. Must not return {@code null}.
     *
     * @return The association qualified name.
     * @since 0.1.0
     */
    public final String qualifiedName()
    {
        AssociationResolution associationResolution = associationBinding.getAssociationResolution();
        AssociationModel model = associationResolution.getAssociationModel();
        return model.getQualifiedName();
    }

    public Type type()
    {
        return null;
    }

    /**
     * Complete the persistence for this assocation instance.
     *
     * @since 0.1.0
     */
    public abstract void complete();
}