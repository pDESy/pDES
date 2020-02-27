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
package org.pdes.rcp.controller.command;

import org.eclipse.gef.commands.Command;
import org.pdes.rcp.model.AllocationLink;
import org.pdes.rcp.model.ComponentHierarchyLink;
import org.pdes.rcp.model.ComponentNode;
import org.pdes.rcp.model.FacilityNode;
import org.pdes.rcp.model.SubWorkflowNode;
import org.pdes.rcp.model.TargetComponentLink;
import org.pdes.rcp.model.TaskDependencyLink;
import org.pdes.rcp.model.TaskNode;
import org.pdes.rcp.model.TeamLink;
import org.pdes.rcp.model.TeamNode;
import org.pdes.rcp.model.base.Link;
import org.pdes.rcp.model.base.NodeElement;

/**
 * This class is the command class for creating line to connection.</br>
 * @author Taiga Mitsuyuki <mitsuyuki@k.u-tokyo.ac.jp>
 *
 */
public class CreateLinkCommand extends Command {

	protected NodeElement source;
	protected NodeElement target;
	protected Link link;
	
	/**
	 * This is the constructor.<br>
	 * @param link
	 */
	public CreateLinkCommand(Link link){
		super();
		this.link = link;
	}

	/**
	 * Get the source of NodeElement in this link.<br>
	 * @return the source
	 */
	public NodeElement getSource() {
		return source;
	}

	/**
	 * Set the source of NodeElement in this link.<br>
	 * @param source the source to set
	 */
	public void setSource(NodeElement source) {
		this.source = source;
	}

	/**
	 * Get the target of NodeElement in this link.<br>
	 * @return the target
	 */
	public NodeElement getTarget() {
		return target;
	}

	/**
	 * Set the target of NodeElement in this link.<br>
	 * @param target the target to set
	 */
	public void setTarget(NodeElement target) {
		this.target = target;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute(){
		
		if(source.equals(target)) return false;//IF source==target THEN wrong.
		
		if(link instanceof TaskDependencyLink){
			if(this.hasSomeLinkBetweenNodeElement()) return false;//IF a link has existed in the same place THEN wrong.
			if(!(source instanceof TaskNode || source instanceof SubWorkflowNode)) return false; // source or target have to be Task or SubWorkflow
			if(!(target instanceof TaskNode || target instanceof SubWorkflowNode)) return false;
			return true;
		}else if(link instanceof TeamLink){
			if(this.hasSomeLinkBetweenNodeElement()) return false;//IF a link has existed in the same place THEN wrong.
			if(!(source instanceof TeamNode)) return false;
			if(!(target instanceof TeamNode)) return false;
			return true;
		}else if(link instanceof ComponentHierarchyLink){
			if(this.hasSomeLinkBetweenNodeElement()) return false;//IF a link has existed in the same place THEN wrong.
			if(!(source instanceof ComponentNode)) return false; // source have to be Component
			if(!(target instanceof ComponentNode || target instanceof SubWorkflowNode)) return false; // target have to be Component or SubWorkflow
			if(target.getIncomingLinkList().stream().filter(link -> link instanceof ComponentHierarchyLink).count()>0) return false;//Child component can have only one parent component.
			return true;
		}else if(link instanceof AllocationLink){
			if(this.hasSomeLinkBetweenNodeElement()) return false;//IF a link has existed in the same place THEN wrong.
			if(!(source instanceof TeamNode) && !(source instanceof FacilityNode)) return false;
			if(!(target instanceof TaskNode)) return false;
			//if(target.getIncomingLinkList().stream().filter(link -> link instanceof AllocationLink).count()>0) return false;//Only one team can be assigned to one Task.
			return true;
		}else if(link instanceof TargetComponentLink){
			if(this.hasSomeLinkBetweenNodeElement()) return false;//IF a link has existed in the same place THEN wrong.
			if(!(source instanceof ComponentNode)) return false;
			if(!(target instanceof TaskNode)) return false;
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		source.addOutgoingLink(link);
		target.addIncomingLink(link);
		link.setOriginNode(source);
		link.setDestinationNode(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		source.removeOutgoingLink(link);
		target.removeIncomingLink(link);
		link.setOriginNode(null);
		link.setDestinationNode(null);
	}
	
	/**
	 * Check whether a link has existed in the same place.<br>
	 * @return
	 */
	private boolean hasSomeLinkBetweenNodeElement(){
		if(source.getOutgoingLinkList().stream().anyMatch(link -> link.getDestinationNode().equals(target))) return true;
		if(source.getIncomingLinkList().stream().anyMatch(link -> link.getOriginNode().equals(target))) return true;
		return false;
	}
	
	/**
	 * Check whether a same type of link has existed in the same place.<br>
	 * @param linktype
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean hasSomeLinkBetweenNodeElement(String linktype){
		if(source.getOutgoingLinkList().stream().filter(link -> link.getDestinationNode().equals(target)).anyMatch(link -> link.getLinkTypeName().equals(linktype))) return true;
		if(source.getIncomingLinkList().stream().filter(link -> link.getOriginNode().equals(target)).anyMatch(link -> link.getLinkTypeName().equals(linktype))) return true;
		return false;
	}
	
}
