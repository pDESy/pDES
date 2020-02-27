/*
 * Copyright (c) 2016, Design Engineering Laboratory, The University of Tokyo.
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package org.pdes.rcp.controller.editpart;

import org.eclipse.gef.EditPart;
import org.pdes.rcp.model.AllocationLink;
import org.pdes.rcp.model.ComponentHierarchyLink;
import org.pdes.rcp.model.ComponentNode;
import org.pdes.rcp.model.FacilityNode;
import org.pdes.rcp.model.TargetComponentLink;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.rcp.model.SubWorkflowNode;
import org.pdes.rcp.model.TaskDependencyLink;
import org.pdes.rcp.model.TaskNode;
import org.pdes.rcp.model.TeamNode;
import org.pdes.rcp.controller.editpart.base.DiagramEditPartFactory;

/**
 * This class is the edit part factory class for creating EditPart corresponding to created model in ProjectEditor.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class ProjectEditorEditPartFactory extends DiagramEditPartFactory {

	/* (non-Javadoc)
	 * @see org.pdes.rcp.controller.editpart.base.DiagramEditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart editPart = null;
		if(model instanceof ProjectDiagram){
			editPart = new ProjectDiagramEditPart();
		}else if(model instanceof TeamNode){
			editPart = new TeamNodeEditPart();
		}else if(model instanceof FacilityNode) {
			editPart = new FacilityNodeEditPart();
		}else if(model instanceof TaskNode){
			editPart = new TaskNodeEditPart();
		}else if(model instanceof ComponentNode){
			editPart = new ComponentNodeEditPart();
		}else if(model instanceof SubWorkflowNode){
			editPart = new SubWorkflowNodeEditPart();
		}else if(model instanceof TaskDependencyLink){
			editPart = new TaskLinkEditPart();
		}else if(model instanceof AllocationLink){
			editPart = new AllocationLinkEditPart();
		}else if(model instanceof ComponentHierarchyLink){
			editPart = new ComponentLinkEditPart();
		}else if(model instanceof TargetComponentLink){
			editPart = new TargetComponentLinkEditPart();
		}
		editPart.setModel(model);
		return editPart;
	}

}
