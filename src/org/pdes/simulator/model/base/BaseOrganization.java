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
import java.util.stream.Collectors;


/**
 * Organization model for discrete event simulation.<br>
 * This model has the list of Teams.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class BaseOrganization {
	private final List<BaseTeam> teamList;
	
	/**
	 * This is the constructor.
	 * @param teamList
	 */
	public BaseOrganization(List<BaseTeam> teamList) {
		this.teamList = teamList;
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		teamList.forEach(t -> t.initialize());
	}
	
	/**
	 * Get the team which has same id.
	 * @param id
	 * @return
	 */
	public BaseTeam getTeam(String id){
		for(BaseTeam team : this.teamList){
			if(team.getId().equals(id)) return team;
		}
		return null;
	}
	
	/**
	 * Get the list of free workers.
	 * @return
	 */
	public List<BaseWorker> getFreeWorkerList() {
		return teamList.stream()
				.map(t -> t.getFreeWorkerList())
				.collect(
					() -> new ArrayList<BaseWorker>(),
					(l, t) -> l.addAll(t),
					(l1, l2) -> l1.addAll(l2)
				);
	}
	
	/**
	 * Get the list of working workers.
	 * @return
	 */
	public List<BaseWorker> getWorkingWorkerList() {
		return teamList.stream()
				.map(t -> t.getWorkingWorkerList())
				.collect(
					() -> new ArrayList<BaseWorker>(),
					(l, t) -> l.addAll(t),
					(l1, l2) -> l1.addAll(l2)
				);
	}
	
	/**
	 * Get the list of all workers.
	 * @return
	 */
	public List<BaseWorker> getWorkerList() {
		return teamList.stream()
				.map(t -> t.getWorkerList())
				.collect(
					() -> new ArrayList<BaseWorker>(),
					(l, t) -> l.addAll(t),
					(l1, l2) -> l1.addAll(l2)
				);
	}

	
	/**
	 * Get total cost of this organization.
	 * @return
	 */
	public double getTotalCost() {
		return teamList.stream().mapToDouble(t -> t.getTotalCost()).sum();
	}

	/**
	 * Get the list of team.
	 * @return the teamList
	 */
	public List<BaseTeam> getTeamList() {
		return teamList;
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		return String.join("\n", teamList.stream().map(t -> t.toString()).collect(Collectors.toList()));
	}
}
