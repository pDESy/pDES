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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pdes.rcp.model.base.NodeElement;

/**
 * This is the TeamNode class.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class TeamNode extends NodeElement {
	
	private static final long serialVersionUID = -4627432256592098860L;
	
	////////////////Variables//////////////////////////////////////////////////////////
	private String name = "";
	protected List<WorkerElement> workerList = new ArrayList<WorkerElement>();
	//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This is the constructor.
	 */
	public TeamNode(){
		String newName = "New Team";
		this.setName(newName);
	}
	
	/**
	 * Get the name of TeamNode
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of TeamNode.
	 * @param name the name to set
	 */
	public void setName(String name) {
		String old = this.name;
		this.name=name;
		firePropertyChange("name",old,name);
	}
	
	/**
	 * Get the list of workers.
	 * @return the workerList
	 */
	public List<WorkerElement> getWorkerList() {
		return workerList;
	}

	/**
	 * Set the list of workers.
	 * @param workerList the workerList to set
	 */
	public void setWorkerList(List<WorkerElement> workerList) {
		this.workerList = workerList;
	}
	
	/**
	 * Add a worker to the list of workers.
	 * @param worker
	 */
	public void addWorker(WorkerElement worker){
		this.workerList.add(worker);
	}
	
	/**
	 * Remove "number"th worker from the list of workers.
	 * @param worker
	 */
	public void deleteWorker(int number){
		this.workerList.remove(number);
	}
	
	/**
	 * Initialize the list of workers.
	 */
	public void initializeWorkerList(){
		this.workerList = new ArrayList<WorkerElement>();
	}
	
	/**
	 * Get the list of worker's name.
	 * @return
	 */
	public List<String> getWorkerNameList(){
		List<String> workerNameList = new ArrayList<String>();
		for(WorkerElement worker:this.workerList){
			workerNameList.add(worker.getName());
		}
		return workerNameList;
	}

	/**
	 * Get the name list of allocated tasks.
	 * @return
	 */
	public List<String> getNameListOfAllocatedTasks(){
		return this.getOutgoingLinkList().stream()
				.filter(s -> s instanceof AllocationLink)
				.map(aLink -> ((TaskNode)aLink.getDestinationNode()).getName())
				.distinct()
				.collect(Collectors.toList());
	}
}
