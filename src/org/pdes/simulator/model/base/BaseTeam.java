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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pdes.rcp.model.TeamNode;

import org.pdes.simulator.model.Facility;
import org.pdes.simulator.model.Worker;

/**
 * Team model for discrete event simulator.<br>
 * Team model has the list of workers and facilities.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class BaseTeam {
	private final String id; // ID
	private final String nodeId; // TeamNode ID
	private final String name;
	private final List<BaseWorker> workerList;
	private BaseTeam superiorTeam;
	
	/**
	 * This is the constructor.
	 * @param teamNode
	 */
	public BaseTeam(TeamNode teamNode) {
		
		this.id = UUID.randomUUID().toString();
		this.nodeId = teamNode.getId();
		this.name = teamNode.getName();
		this.workerList = teamNode.getWorkerList().stream().map(w -> new Worker(w, this)).collect(Collectors.toList());
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		workerList.forEach(w -> w.initialize());

	}
	
	/**
	 * Get the list of free workers.
	 * @return
	 */
	public List<BaseWorker> getFreeWorkerList() {
		return workerList.stream().filter(w -> w.isFree()).collect(Collectors.toList());
	}
	
	/**
	 * Get the list of working workers.
	 * @return
	 */
	public List<BaseWorker> getWorkingWorkerList() {
		return workerList.stream().filter(w -> w.isWorking()).collect(Collectors.toList());
	}

	
	/**
	 * Get the total cost of this team.
	 * @return
	 */
	public double getTotalCost() {
		double workerTotalCost = workerList.stream().mapToDouble(w -> w.getTotalCost()).sum();
		return workerTotalCost;
	}

	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the node id.
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the list of workers.
	 * @return the workerList
	 */
	public List<BaseWorker> getWorkerList() {
		return workerList;
	}


	/**
	 * Get the superior Team.
	 * @return the superiorTeam
	 */
	public BaseTeam getSuperiorTeam() {
		return superiorTeam;
	}

	/**
	 * Set the superior Team.
	 * @param superiorTeam the superiorTeam to set
	 */
	public void setSuperiorTeam(BaseTeam superiorTeam) {
		this.superiorTeam = superiorTeam;
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		String str = "[" + name + "]\n";
		str += String.join("\n", workerList.stream().map(w -> w.toString()).collect(Collectors.toList()));
		str += "\n";
		return str;
	}
}
