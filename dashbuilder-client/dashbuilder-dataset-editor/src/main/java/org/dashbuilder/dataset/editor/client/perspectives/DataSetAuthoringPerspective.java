/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataset.editor.client.perspectives;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.MultiTabWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.SimpleWorkbenchPanelPresenter;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

import javax.enterprise.context.ApplicationScoped;

/**
 * <p>The authoring perspective for the management of data sets using the UI.</p>
 * <p>Provides:</p>
 * <ul>
 *     <li>The Data Set Explorer widget on the left area.</li>     
 *     <li>The Data Set Editor widget on the center area.</li> 
 * </ul>
 *
 * @since 0.3.0 
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "DataSetAuthoringPerspective")
public class DataSetAuthoringPerspective {

    @Perspective
    public PerspectiveDefinition buildPerspective() {

        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl(MultiTabWorkbenchPanelPresenter.class.getName());
        perspective.setName("Data Set Authoring"); // TODO: I18n

        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "DataSetEditor" ) ) );
        final PanelDefinition west = new PanelDefinitionImpl( SimpleWorkbenchPanelPresenter.class.getName() );
        west.setWidth( 300 );
        west.setMinWidth( 200 );
        west.addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "DataSetExplorer" ) ) );
        perspective.getRoot().insertChild( CompassPosition.WEST, west );
        return perspective;
    }
}