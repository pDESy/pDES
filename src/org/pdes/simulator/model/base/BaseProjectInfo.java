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
package org.pdes.simulator.model.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.simulator.model.Component;
import org.pdes.simulator.model.Factory;
import org.pdes.simulator.model.Organization;
import org.pdes.simulator.model.Product;
import org.pdes.simulator.model.Task;
import org.pdes.simulator.model.Team;
import org.pdes.simulator.model.Workflow;

/**
 * This is the class for collecting Project information.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class BaseProjectInfo {
	
	private ProjectDiagram diagram;
	
	public BaseOrganization organization;
	public BaseFactory factory;
	public List<BaseWorkflow> workflowList;
	public List<BaseProduct> productList;
	public int concurrencyWorkflowLimit;
	
	/**
	 * This is the constructor.
	 * If you put the diagram information, you can create the model for simulation automatically.
	 * @param diagram
	 */
	public BaseProjectInfo(ProjectDiagram diagram, int workflowCount){
		this.diagram = diagram;
		this.organization = this.getOrganizationFromProjectDiagram();
		this.factory = this.getFactoryFromProjectDiagram();
		this.workflowList = new ArrayList<BaseWorkflow>();
		this.productList = new ArrayList<BaseProduct>();
		IntStream.range(0,workflowCount).forEach(i ->{
			List<BaseTask> taskList = this.getTaskListConsideringOnlyTaskDependency();
			List<BaseComponent> componentList = this.getComponentListConsideringOnlyComponentDependency();
			this.addTargetComponentLinkInformation(componentList, taskList);
			this.addAllocationLinkInformation(organization, factory, taskList);
			BaseWorkflow workflow = new Workflow(i,taskList);
			BaseProduct product = new Product(i,componentList);
			this.workflowList.add(workflow);
			this.productList.add(product);
		});
		this.concurrencyWorkflowLimit = this.diagram.getConcurrencyLimitOfWorkflow();
	}
	
	/**
	 * Get Factory from ProjectDiagram.
	 * @return
	 */
	private BaseFactory getFactoryFromProjectDiagram(){
		List<BaseFacilityGroup> facilityGroupList = diagram.getFacilityNodeList().stream()
				.map(node -> new BaseFacilityGroup(node))
				.collect(Collectors.toList());
		return new Factory(facilityGroupList);
	}
	/**
	 * Get Organization from ProjectDiagram.
	 * @return
	 */
	private BaseOrganization getOrganizationFromProjectDiagram(){
		List<BaseTeam> teamList = diagram.getTeamNodeList().stream()
				.map(node -> new Team(node))
				.collect(Collectors.toList());
		return new Organization(teamList);
	}
	
	
	/**
	 * Get the list of Task considering only task dependency.
	 * @return
	 */
	private List<BaseTask> getTaskListConsideringOnlyTaskDependency(){
		List<BaseTask> taskList = this.diagram.getTaskNodeList().stream()
				.map(node -> new Task(node))
				.collect(Collectors.toList());
		this.diagram.getTaskLinkList().forEach(link -> {
			BaseTask destinationTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			BaseTask originTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationTask.addInputTask(originTask);
			originTask.addOutputTask(destinationTask);
		});
		return taskList;
	}
	
	/**
	 * Get the list of Component considering only component dependency.
	 * @return
	 */
	private List<BaseComponent> getComponentListConsideringOnlyComponentDependency(){
		List<BaseComponent> componentList = this.diagram.getComponentNodeList().stream()
				.map(node -> new Component(node))
				.collect(Collectors.toList());
		this.diagram.getComponentLinkList().forEach(link -> {
			BaseComponent destinationComponent = componentList.stream()
					.filter(component -> component.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			BaseComponent originComponent = componentList.stream()
					.filter(component -> component.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationComponent.addDependedComponent(originComponent);
			originComponent.addDependingComponent(destinationComponent);
		});
		return componentList;
	}
	
	/**
	 * Add TargetComponentLink information to Component and Task.
	 * @param taskList
	 * @param componentList
	 */
	private void addTargetComponentLinkInformation(List<BaseComponent> componentList, List<BaseTask> taskList){
		this.diagram.getTargetComponentLinkList().forEach(link -> {
			BaseTask destinationTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			BaseComponent originComponent = componentList.stream()
					.filter(component -> component.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationTask.addTargetComponent(originComponent);
			originComponent.addTargetedTask(destinationTask);
		});
	}
	
	/**
	 * Add AllocationLink information to Team and Task.
	 * @param organization
	 * @param taskList
	 */
	private void addAllocationLinkInformation(BaseOrganization organization, BaseFactory factory, List<BaseTask> taskList){
		this.diagram.getAllocationLinkList().forEach(link -> {
			BaseTask destinationTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			Optional<BaseTeam> originTeam = organization.getTeamList().stream()
					.filter(team -> team.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst();
			originTeam.ifPresent(team -> {
				destinationTask.addAllocatedTeam(team);
			});
			Optional<BaseFacilityGroup> originFacilityGroup = factory.getFacilityGroupList().stream()
					.filter(group -> group.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst();
			originFacilityGroup.ifPresent(group -> {
				destinationTask.addAllocatedFacilityGroup(group);
			});
		});
	}
	/**
	 * Get the Organization.
	 * @return the organization
	 */
	public BaseOrganization getOrganization() {
		return organization;
	}

	/**
	 * Get the list of Workflow.
	 * @return the workflowList
	 */
	public List<BaseWorkflow> getWorkflowList() {
		return workflowList;
	}

	/**
	 * Get the list of Product
	 * @return the productList
	 */
	public List<BaseProduct> getProductList() {
		return productList;
	}

	/**
	 * Get the limit of concurrency workflow.
	 * @return the concurrencyWorkflowLimit
	 */
	public int getConcurrencyWorkflowLimit() {
		return concurrencyWorkflowLimit;
	}
	
	/**
	 * Get the total cost of this Project after simulation.
	 * @return
	 */
	public double getTotalCost(){
		return organization.getTotalCost();
	}
	
	/**
	 * Get the total actual work amount of this project after simulation.
	 * @return
	 */
	public double getTotalActualWorkAmount(){
		return workflowList.stream()
				.mapToDouble(w -> w.getTotalActualWorkAmount())
				.sum();
	}
	
	/**
	 * Get the duration considering all workflows.
	 * @return
	 */
	public int getDuration(){
		return workflowList.stream()
				.mapToInt(w -> w.getDuration())
				.max()
				.orElse(0);
	}
}
