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
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.pdes.rcp.model.base.ResourceElement;

/**
 * Task model for discrete event simulation.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class BaseResource {
	
	protected enum ResourceState {
		FREE,
		WORKING,
	}
	
	// Constraint variables on simulation
	protected final String id; // ID
	protected final String nodeId; // ResourceElement ID
	protected final String name;
	protected final double costPerTime;
	protected Map<String, Double[]> workAmountSkillMap; // skill map of work amount <taskname, skillpoint_mean, skillpoint_standard_deviation>
	protected Map<String, Double> qualitySkillMap; // skill map of quality <taskname, skill point>
	protected BaseTeam team;
	
	// Changeable variable on simulation
	protected ResourceState state;
	protected double totalCost = 0;
	protected final List<Integer> startTimeList = new ArrayList<Integer>(); // list of start time of one task
	protected final List<Integer> finishTimeList = new ArrayList<Integer>(); // list of finish time of one task
	protected final List<BaseTask> assignedTaskList = new ArrayList<BaseTask>(); // list of worked task
	
	private Random random = new Random();//random
	
	/**
	 * This is the constructor.
	 * @param resourceElement
	 * @param team
	 */
	public BaseResource(ResourceElement resourceElement, BaseTeam team) {
		this.id = UUID.randomUUID().toString();
		this.nodeId = resourceElement.getId();
		this.name = resourceElement.getName();
		this.costPerTime = resourceElement.getCost();
		this.workAmountSkillMap = resourceElement.getWorkAmountSkillMap();
		this.qualitySkillMap = resourceElement.getQualitySkillMap();
		this.team = team;
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		state = ResourceState.FREE;
		totalCost = 0;
		startTimeList.clear();
		finishTimeList.clear();
		assignedTaskList.clear();
	}
	
	/**
	 * Check whether this resource is free or not.
	 * @return
	 */
	public boolean isFree() {
		return state == ResourceState.FREE;
	}
	
	/**
	 * Check whether this resource is working or not.
	 * @return
	 */
	public boolean isWorking() {
		return state == ResourceState.WORKING;
	}
	
	/**
	 * Set the state of this resource to "FREE".
	 */
	public void setStateFree() {
		state = ResourceState.FREE;
	}
	
	/**
	 * Set the state of this resource to "WORKING".
	 */
	public void setStateWorking() {
		state = ResourceState.WORKING;
	}
	
	/**
	 * Working in simulation. In this simulation, total cost of this resource have to be updated.
	 */
	public void addLaborCost() {
		totalCost += costPerTime;
	}
	
	/**
	 * Check whether this resource has "task" skill or not.
	 * @param task
	 * @return
	 */
	public boolean hasSkill(BaseTask task) {
		return (task.getAllocatedTeamList().stream().anyMatch(t -> t.equals(team)) && workAmountSkillMap.containsKey(task.getName()) && workAmountSkillMap.get(task.getName())[0] > 0.0);
	}
	
	/**
	 * Get the work amount skill point of "task".
	 * if this resource will do multi-tasks at the same time, allocation is divided to working tasks equally. 
	 * @param task
	 * @return
	 */
	public double getWorkAmountSkillPoint(BaseTask task){
		if (!hasSkill(task)) return 0.0;
		double skillPoint = workAmountSkillMap.get(task.getName())[0];
		double standardDeviation = workAmountSkillMap.get(task.getName())[1];
		double baseProgress = random.nextGaussian() * standardDeviation + skillPoint;
		long sumOfWorkingTaskInThisTime = this.getAssignedTaskList().stream().filter(t -> t.getStateInt()==2||task.getStateInt()==3).count();
		double progress = baseProgress / sumOfWorkingTaskInThisTime;
		return progress;
	}
	
	/**
	 * Get the quality skill point of "task".
	 * @param task
	 * @return
	 */
	public double getQualitySkillPoint(BaseTask task){
		if (!hasSkill(task)) return 0.0;
		return qualitySkillMap.get(task.getName());
	}
	
	/**
	 * Get total work amount skill point only mean.
	 * @return
	 */
	public double getTotalWorkAmountSkillPoint() {
		return workAmountSkillMap.values().stream().mapToDouble(v -> v[0]).sum();
	}
	
	/**
	 * Add assigned task.
	 * @param task
	 */
	public void addAssignedTask(BaseTask task) {
		assignedTaskList.add(task);
	}
	
	/**
	 * Add start time.
	 * @param time
	 */
	public void addStartTime(int time) {
		startTimeList.add(time);
	}
	
	/**
	 * Add finish time.
	 * @param time
	 */
	public void addFinishTime(int time) {
		finishTimeList.add(time);
	}
	
	/**
	 * Get first start time in the list of start time.
	 * @return
	 */
	public int getFirstStartTime() {
		if (startTimeList.size() == 0) return -1;
		return startTimeList.get(0);
	}
	
	/**
	 * Get last finish time in the list of finish time.
	 * @return
	 */
	public int getLastFinishTime() {
		if (finishTimeList.size() == 0) return -1;
		return finishTimeList.get(finishTimeList.size() - 1);
	}

	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the cost per time.
	 * @return the costPerTime
	 */
	public double getCostPerTime() {
		return costPerTime;
	}

	/**
	 * Get the skill map of work amount.
	 * @return the workAmountSkillMap
	 */
	public Map<String, Double[]> getWorkAmountSkillMap() {
		return workAmountSkillMap;
	}

	/**
	 * Get the skill map of quality.
	 * @return the qualitySkillMap
	 */
	public Map<String, Double> getQualitySkillMap() {
		return qualitySkillMap;
	}

	/**
	 * Get the team which has this Resource.
	 * @return the team
	 */
	public BaseTeam getTeam() {
		return team;
	}

	/**
	 * Set the team which has this Resource.
	 * @param team the team to set
	 */
	public void setTeam(BaseTeam team) {
		this.team = team;
	}

	/**
	 * Get total cost.
	 * @return the totalCost
	 */
	public double getTotalCost() {
		return totalCost;
	}

	/**
	 * Get the list of start time list.
	 * @return the startTimeList
	 */
	public List<Integer> getStartTimeList() {
		return startTimeList;
	}

	/**
	 * Get the list of finish time list.
	 * @return the finishTimeList
	 */
	public List<Integer> getFinishTimeList() {
		return finishTimeList;
	}

	/**
	 * Get the list of worked task.
	 * @return the assignedTaskList
	 */
	public List<BaseTask> getAssignedTaskList() {
		return assignedTaskList;
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(name);
		sb.append("] WA(");
		for (Map.Entry<String, Double[]> entry : workAmountSkillMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") Q(");
		for (Map.Entry<String, Double> entry : qualitySkillMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}
}
