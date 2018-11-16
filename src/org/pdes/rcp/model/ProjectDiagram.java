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
package org.pdes.rcp.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.draw2d.geometry.Point;
import org.pdes.rcp.model.base.Diagram;
import org.pdes.rcp.model.base.Link;
import org.pdes.rcp.model.base.NodeElement;
import org.pdes.rcp.model.base.ResourceElement;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the Project Diagram class.
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class ProjectDiagram extends Diagram {

	private static final long serialVersionUID = 5579324532129810803L;
	
	private int concurrencyLimitOfWorkflow; //Concurrency Limit of workflow in this diagram.
	
	/**
	 * This is the constructor.
	 */
	public ProjectDiagram() {
		super();
		this.concurrencyLimitOfWorkflow = 1;
	}

	/**
	 * Save the Project file in the "filePath".
	 * @param filePath
	 * @return
	 */
	public boolean saveProjectFile(String filePath) {
		try{
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
			pw.println("<?xml version=\"1.0\"?>");
			pw.println("<!--Project Diagram-->");
			pw.println("<ProjectDiagram>");
			
			// Concurrency Limit of Workflow
			pw.println("	<ConcurrencyLimit>" + this.concurrencyLimitOfWorkflow + "</ConcurrencyLimit>");
			
			List<Link> linkList = new ArrayList<Link>();
			
			//1. NodeElement
			pw.println("	<NodeElementList>");
			for(NodeElement node : this.getNodeElementList()){
				linkList.addAll(node.getIncomingLinkList());//register all Link for processing after NodeElement.
				
				if(node instanceof TeamNode){
					TeamNode team = (TeamNode)node;
					pw.println("		<TeamNode id=\""+team.getId()+"\" Left=\""+((NodeElement) node).getX()+"\" Top=\""+((NodeElement) node).getY()+"\" Width=\""+((NodeElement) node).getWidth()+"\" Height=\""+((NodeElement) node).getHeight()+"\">");
					pw.println("			<Name>"+this.xmlEscape(team.getName())+"</Name>");
					
					//Worker
					List<WorkerElement> workerList = team.getWorkerList();
					if(workerList.size()==0) pw.println("			<WorkerList/>");
					else{
						pw.println("			<WorkerList>");
						for(WorkerElement worker:workerList){
							pw.println("				<Worker>");
							pw.println("					<Name>"+this.xmlEscape(worker.getName())+"</Name>");
							pw.println("					<Cost>"+worker.getCost()+"</Cost>");
							Map<String,Double> workAmountSkillMap = worker.getWorkAmountSkillMap();
							for(Iterator<Entry<String, Double>> it = workAmountSkillMap.entrySet().iterator();it.hasNext();){
								Entry<String, Double> entry = (Map.Entry<String, Double>)it.next();
								pw.println("					<WorkAmountSkill name=\""+this.xmlEscape(entry.getKey())+"\" value=\""+entry.getValue()+"\"/>");
							}
							Map<String,Double> qualitySkillMap = worker.getQualitySkillMap();
							for(Iterator<Entry<String, Double>> it = qualitySkillMap.entrySet().iterator();it.hasNext();){
								Entry<String, Double> entry = (Map.Entry<String, Double>)it.next();
								pw.println("					<QualitySkill name=\""+this.xmlEscape(entry.getKey())+"\" value=\""+entry.getValue()+"\"/>");
							}
							pw.println("				</Worker>");
						}
						pw.println("			</WorkerList>");
					}
					
					//Facility
					List<FacilityElement> facilityList = team.getFacilityList();
					if(facilityList.size()==0) pw.println("			<FacilityList/>");
					else{
						pw.println("			<FacilityList>");
						for(FacilityElement facility:facilityList){
							pw.println("				<Facility>");
							pw.println("					<Name>"+this.xmlEscape(facility.getName())+"</Name>");
							pw.println("					<Cost>"+facility.getCost()+"</Cost>");
							Map<String,Double> workAmountSkillMap = facility.getWorkAmountSkillMap();
							for(Iterator<Entry<String, Double>> it = workAmountSkillMap.entrySet().iterator();it.hasNext();){
								Entry<String, Double> entry = (Map.Entry<String, Double>)it.next();
								pw.println("					<WorkAmountSkill name=\""+this.xmlEscape(entry.getKey())+"\" value=\""+entry.getValue()+"\"/>");
							}
							Map<String,Double> qualitySkillMap = facility.getQualitySkillMap();
							for(Iterator<Entry<String, Double>> it = qualitySkillMap.entrySet().iterator();it.hasNext();){
								Entry<String, Double> entry = (Map.Entry<String, Double>)it.next();
								pw.println("					<QualitySkill name=\""+this.xmlEscape(entry.getKey())+"\" value=\""+entry.getValue()+"\"/>");
							}
							pw.println("				</Facility>");
						}
						pw.println("			</FacilityList>");
					}
					pw.println("		</TeamNode>");
					
				}else if(node instanceof TaskNode){
					TaskNode task = (TaskNode) node;
					pw.println("		<TaskNode id=\""+task.getId()+"\" Left=\""+((NodeElement) node).getX()+"\" Top=\""+((NodeElement) node).getY()+"\" Width=\""+((NodeElement) node).getWidth()+"\" Height=\""+((NodeElement) node).getHeight()+"\">");
					pw.println("			<Name>"+this.xmlEscape((task.getName()).toString())+"</Name>");
					pw.println("			<WorkAmount>"+task.getWorkAmount()+"</WorkAmount>");
					pw.println("			<Progress>"+task.getProgress()+"</Progress>");
					pw.println("			<AdditionalWorkAmount>"+task.getAdditionalWorkAmount()+"</AdditionalWorkAmount>");
					pw.println("			<NeedFacility>"+task.isNeedFacility()+"</NeedFacility>");
					pw.println("		</TaskNode>");
				}else if(node instanceof ComponentNode){
					ComponentNode component = (ComponentNode) node;
					pw.println("		<ComponentNode id=\""+component.getId()+"\" Left=\""+((NodeElement) node).getX()+"\" Top=\""+((NodeElement) node).getY()+"\" Width=\""+((NodeElement) node).getWidth()+"\" Height=\""+((NodeElement) node).getHeight()+"\">");
					pw.println("			<Name>"+this.xmlEscape((component.getName()).toString())+"</Name>");
					pw.println("			<ErrorTolerance>"+component.getErrorTolerance()+"</ErrorTolerance>");
					pw.println("		</ComponentNode>");
				}else if(node instanceof SubWorkflowNode){
					SubWorkflowNode subWorkflow = (SubWorkflowNode) node;
					pw.println("		<SubWorkflowNode id=\""+subWorkflow.getId()+"\" Left=\""+((NodeElement) node).getX()+"\" Top=\""+((NodeElement) node).getY()+"\" Width=\""+((NodeElement) node).getWidth()+"\" Height=\""+((NodeElement) node).getHeight()+"\">");
					pw.println("			<Name>"+this.xmlEscape((subWorkflow.getName()).toString())+"</Name>");
					pw.println("			<Filename>"+subWorkflow.getFilename()+"</Filename>");
					pw.println("		</SubWorkflowNode>");
				}
			}
			pw.println("	</NodeElementList>");
			
			//2. Link
			pw.println("	<LinkList>");
			for(Link link : linkList){
				String linkType = link.getLinkTypeName();
				pw.println("		<Link type=\""+linkType+"\" org=\""+link.getOriginNode().getId()+"\" dst=\""+link.getDestinationNode().getId()+"\">");
				link.getBendPoints().forEach(p -> pw.println("			<Point X=\""+p.x+"\" Y=\""+p.y+"\"/>"));
				pw.println("		</Link>");
			}
			pw.println("	</LinkList>");
			
			pw.println("</ProjectDiagram>");
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Read Project file on "filePath".
	 * @param filePath
	 * @return
	 */
	public boolean readProjectFile(String filePath) {
		Document xml = null;
		try{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			xml = builder.parse(new FileInputStream(new File(filePath)));
			
			// Concurrency Limit of workflow
			NodeList nodeList = xml.getElementsByTagName("ConcurrencyLimit");
			concurrencyLimitOfWorkflow = Integer.parseInt(nodeList.item(0).getFirstChild().getNodeValue());
			
			/////////////TeamNode///////////////////////
			nodeList = xml.getElementsByTagName("TeamNode");
			for(int i=0;i<nodeList.getLength();i++){
				
				TeamNode team = new TeamNode();
				Node node = nodeList.item(i);
				NamedNodeMap attrs = node.getAttributes();
				if(attrs!=null){
					team.setId(attrs.getNamedItem("id").getNodeValue());
					team.setX(Integer.parseInt(attrs.getNamedItem("Left").getNodeValue()));
					team.setY(Integer.parseInt(attrs.getNamedItem("Top").getNodeValue()));
					team.setWidth(Integer.parseInt(attrs.getNamedItem("Width").getNodeValue()));
					team.setHeight(Integer.parseInt(attrs.getNamedItem("Height").getNodeValue()));
				}
				
				NodeList childNodeList = node.getChildNodes();
				for(int j=0;j<childNodeList.getLength();j++){
					Node childNode = childNodeList.item(j);
					if(childNode.getNodeName()==null||childNode.getNodeName().equals("#text")){
					}else{
						String tagName = childNode.getNodeName();
						if(childNode.getFirstChild() != null){
							String value = childNode.getFirstChild().getNodeValue();
							if(tagName.equals("Name")) team.setName(value);
							else if(tagName.equals("WorkerList")||tagName.equals("FacilityList")){
								setResourceListInTeamNode(team,childNode.getChildNodes());
							}
						}
					}
				}
				
				
				this.addNodeElement(team);
			}
			///////////////////////////////
			
			/////////////TaskNode///////////////////////
			nodeList = xml.getElementsByTagName("TaskNode");
			for(int i=0;i<nodeList.getLength();i++){
				
				TaskNode task = new TaskNode();
				Node node = nodeList.item(i);
				NamedNodeMap attrs = node.getAttributes();
				if(attrs!=null){
					task.setId(attrs.getNamedItem("id").getNodeValue());
					task.setX(Integer.parseInt(attrs.getNamedItem("Left").getNodeValue()));
					task.setY(Integer.parseInt(attrs.getNamedItem("Top").getNodeValue()));
					task.setWidth(Integer.parseInt(attrs.getNamedItem("Width").getNodeValue()));
					task.setHeight(Integer.parseInt(attrs.getNamedItem("Height").getNodeValue()));
				}
				NodeList tags = node.getChildNodes();
				for(int j=0;j<tags.getLength();j++){
					Node tag = tags.item(j);
					if(tag.getNodeName()==null||tag.getNodeName().equals("#text")){
					}else{
						String tagName = tag.getNodeName();
						String value = tag.getFirstChild().getNodeValue();
						if(tagName.equals("Name")) task.setName(value);
						else if(tagName.equals("WorkAmount")) task.setWorkAmount(Double.parseDouble(value));
						else if(tagName.equals("Progress")) task.setProgress(Double.parseDouble(value));
						else if(tagName.equals("AdditionalWorkAmount")) task.setAdditionalWorkAmount(Double.parseDouble(value));
						else if(tagName.equals("NeedFacility")) task.setNeedFacility(Boolean.parseBoolean(value));
					}
				}
				this.addNodeElement(task);
			}
			///////////////////////////////
			
			/////////////ComponentNode///////////////////////
			nodeList = xml.getElementsByTagName("ComponentNode");
			for(int i=0;i<nodeList.getLength();i++){
				
				ComponentNode component = new ComponentNode();
				Node node = nodeList.item(i);
				NamedNodeMap attrs = node.getAttributes();
				if(attrs!=null){
					component.setId(attrs.getNamedItem("id").getNodeValue());
					component.setX(Integer.parseInt(attrs.getNamedItem("Left").getNodeValue()));
					component.setY(Integer.parseInt(attrs.getNamedItem("Top").getNodeValue()));
					component.setWidth(Integer.parseInt(attrs.getNamedItem("Width").getNodeValue()));
					component.setHeight(Integer.parseInt(attrs.getNamedItem("Height").getNodeValue()));
				}
				NodeList tags = node.getChildNodes();
				for(int j=0;j<tags.getLength();j++){
					Node tag = tags.item(j);
					if(tag.getNodeName()==null||tag.getNodeName().equals("#text")){
					}else{
						String tagName = tag.getNodeName();
						String value = tag.getFirstChild().getNodeValue();
						if(tagName.equals("Name")) component.setName(value);
						else if(tagName.equals("ErrorTolerance")) component.setErrorTolerance(Double.parseDouble(value));
					}
				}
				this.addNodeElement(component);
			}
			///////////////////////////////
			
			/////////////SubWorkflowNode///////////////////////
			nodeList = xml.getElementsByTagName("SubWorkflowNode");
			for(int i=0;i<nodeList.getLength();i++){
				
				SubWorkflowNode subWorkflow = new SubWorkflowNode();
				Node node = nodeList.item(i);
				NamedNodeMap attrs = node.getAttributes();
				if(attrs!=null){
					subWorkflow.setId(attrs.getNamedItem("id").getNodeValue());
					subWorkflow.setX(Integer.parseInt(attrs.getNamedItem("Left").getNodeValue()));
					subWorkflow.setY(Integer.parseInt(attrs.getNamedItem("Top").getNodeValue()));
					subWorkflow.setWidth(Integer.parseInt(attrs.getNamedItem("Width").getNodeValue()));
					subWorkflow.setHeight(Integer.parseInt(attrs.getNamedItem("Height").getNodeValue()));
				}
				NodeList tags = node.getChildNodes();
				for(int j=0;j<tags.getLength();j++){
					Node tag = tags.item(j);
					if(tag.getNodeName()==null||tag.getNodeName().equals("#text")){
					}else{
						String tagName = tag.getNodeName();
						String value = tag.getFirstChild().getNodeValue();
						if(tagName.equals("Name")) subWorkflow.setName(value);
						else if(tagName.equals("Filename")) subWorkflow.setFilename(value);
					}
				}
				this.addNodeElement(subWorkflow);
			}
			///////////////////////////////
			
			/////////////Link///////////////////////
			nodeList = xml.getElementsByTagName("Link");
			for(int i=0;i<nodeList.getLength();i++){
				Node node = nodeList.item(i);
				NamedNodeMap attrs = node.getAttributes();
				if(attrs!=null){
					Link link = null;
					String linkType = attrs.getNamedItem("type").getNodeValue();
					if(linkType.equals("TeamLink")) link = new TeamLink();
					else if(linkType.equals("TaskLink")) link = new TaskDependencyLink();
					else if(linkType.equals("AllocationLink")) link = new AllocationLink();
					else if(linkType.equals("ComponentLink")) link = new ComponentHierarchyLink();
					else if(linkType.equals("TargetComponentLink")) link = new TargetComponentLink();
					
					if(link != null){
						NodeElement originNode = this.getNodeElement(attrs.getNamedItem("org").getNodeValue());
						NodeElement destinationNode = this.getNodeElement(attrs.getNamedItem("dst").getNodeValue());
						link.setOriginNode(originNode);
						originNode.addOutgoingLink(link);
						link.setDestinationNode(destinationNode);
						destinationNode.addIncomingLink(link);
					}
					
					List<Point> bendPoints = new ArrayList<Point>();
					
					NodeList tags = node.getChildNodes();
					for(int j=0;j<tags.getLength();j++){
						Node tag = tags.item(j);
						if(tag.getNodeName()==null||tag.getNodeName().equals("#text")){
						}else{
							String tagName = tag.getNodeName();
							if(tagName.equals("Point")){
								NamedNodeMap attrs2 = tag.getAttributes();
								//Attributes
								if (attrs2!=null){
									Node attr;
									attr = attrs2.getNamedItem("X");
									int x=Integer.parseInt(attr.getNodeValue());
									attr = attrs2.getNamedItem("Y");
									int y=Integer.parseInt(attr.getNodeValue());
									Point bendPoint = new Point(x,y);
									bendPoints.add(bendPoint);
								}
							}
						}
					}
					
					if (link != null) link.setBendPoints(bendPoints);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Set Resource(Worker, Facility) list in TeamNode.
	 * @param team
	 * @param resourceType
	 * @param nodes
	 */
	private void setResourceListInTeamNode(TeamNode team, NodeList nodes) {
		for(int i=0;i<nodes.getLength();i++){
			Node tag = nodes.item(i);
			if(tag.getNodeName()==null||tag.getNodeName().equals("#text")){
			}else{
				String tagName = tag.getNodeName();
				ResourceElement resource = null;
				if(tagName.equals("Worker")) {
					resource = new WorkerElement();
					team.addWorker((WorkerElement) resource);
				}else if(tagName.equals("Facility")){
					resource = new FacilityElement();
					team.addFacility((FacilityElement) resource);
				}
				NodeList childNodeList = tag.getChildNodes();
				for(int j=0;j<childNodeList.getLength();j++){
					Node childTag = childNodeList.item(j);
					if(childTag.getNodeName()==null||childTag.getNodeName().equals("#text")){
					}else{
						String childTagName = childTag.getNodeName();
						if(childTagName.equals("Name")) resource.setName(childTag.getFirstChild().getNodeValue());
						else if(childTagName.equals("Cost")) resource.setCost(Double.valueOf(childTag.getFirstChild().getNodeValue()));
						else if(childTagName.equals("WorkAmountSkill")) resource.addSkillInWorkAmountSkillMap(childTag.getAttributes().getNamedItem("name").getNodeValue(), Double.valueOf(childTag.getAttributes().getNamedItem("value").getNodeValue()));
						else if(childTagName.equals("QualitySkill")) resource.addSkillInQualitySkillMap(childTag.getAttributes().getNamedItem("name").getNodeValue(), Double.valueOf(childTag.getAttributes().getNamedItem("value").getNodeValue()));
					}
				}
			}
		}
	}


	/**
	 * Check whether TaskNode which has the name of "taskNodeName" is existed in this Diagram or not.<br>
	 * @param taskNodeName
	 * @return
	 */
	public boolean hasTaskNode(String taskNodeName) {
		if(this.getNodeElementList().stream().filter(node -> node instanceof TaskNode).anyMatch(node -> ((TaskNode)node).getName().equals(taskNodeName))) return true;
		return false;
	}
	
	/**
	 * Get TaskNode list in this ProjectDiagram.
	 * @return
	 */
	public List<TaskNode> getTaskNodeList(){
		return this.getNodeElementList().stream().filter(node -> node instanceof TaskNode).map(node -> (TaskNode)node).collect(Collectors.toList());
	}
	
	/**
	 * Get TaskLink list in this ProjectDiagram.
	 * @return
	 */
	public List<TaskDependencyLink> getTaskLinkList(){
		return this.getLinkList().stream().filter(link -> link instanceof TaskDependencyLink).map(link -> (TaskDependencyLink)link).collect(Collectors.toList());
	}
	
	/**
	 * Get TeamLink list in this ProjectDiagram.
	 * @return
	 */
	public List<TeamLink> getTeamLinkList(){
		return this.getLinkList().stream().filter(link -> link instanceof TeamLink).map(link -> (TeamLink)link).collect(Collectors.toList());
	}
	
	/**
	 * Get AllocationLink list in this ProjectDiagram.
	 * @return
	 */
	public List<AllocationLink> getAllocationLinkList(){
		return this.getLinkList().stream().filter(link -> link instanceof AllocationLink).map(link -> (AllocationLink)link).collect(Collectors.toList());
	}
	
	/**
	 * Get TeamNode list in this ProjectDiagram.
	 * @return
	 */
	public List<TeamNode> getTeamNodeList(){
		return this.getNodeElementList().stream().filter(node -> node instanceof TeamNode).map(node -> (TeamNode)node).collect(Collectors.toList());
	}
	
	/**
	 * Get ComponentNode list in this ProjectDiagram.
	 * @return
	 */
	public List<ComponentNode> getComponentNodeList(){
		return this.getNodeElementList().stream().filter(node -> node instanceof ComponentNode).map(node -> (ComponentNode)node).collect(Collectors.toList());
	}
	
	/**
	 * Get SubWorkflowNode list in this ProjectDiagram.
	 * @return
	 */
	public List<SubWorkflowNode> getSubWorkflowNodeList(){
		return this.getNodeElementList().stream().filter(node -> node instanceof SubWorkflowNode).map(node -> (SubWorkflowNode)node).collect(Collectors.toList());
	}
	
	/**
	 * Get ComponentLink list in this ProjectDiagram.
	 * @return
	 */
	public List<ComponentHierarchyLink> getComponentLinkList(){
		return this.getLinkList().stream().filter(link -> link instanceof ComponentHierarchyLink).map(link -> (ComponentHierarchyLink)link).collect(Collectors.toList());
	}
	
	/**
	 * Get TargetComponentLink list in this ProjectDiagram.
	 * @return
	 */
	public List<TargetComponentLink> getTargetComponentLinkList(){
		return this.getLinkList().stream().filter(link -> link instanceof TargetComponentLink).map(link -> (TargetComponentLink)link).collect(Collectors.toList());
	}

	/**
	 * Get concurrency limit of workflow.
	 * @return the concurrencyLimit
	 */
	public int getConcurrencyLimitOfWorkflow() {
		return concurrencyLimitOfWorkflow;
	}

	/**
	 * Set concurrency limit of workflow.
	 * @param concurrencyLimit the concurrencyLimit to set
	 */
	public void setConcurrencyLimitOfWorkflow(int concurrencyLimitOfWorkflow) {
		int old = concurrencyLimitOfWorkflow;
		this.concurrencyLimitOfWorkflow = concurrencyLimitOfWorkflow;
		firePropertyChange("concurrencyLimitOfWorkflow", old, concurrencyLimitOfWorkflow);
	}
}
