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
import java.util.UUID;
import java.util.stream.Collectors;

import org.pdes.rcp.model.TaskNode;

/**
 * Task model for discrete event simulation.
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class BaseTask {
	
	private enum TaskState {
		/** Cannot start task (stateInt=0)*/
		NONE,
		/** Can start task but not start (stateInt=1)*/
		READY,
		/** Doing this task (stateInt=2)*/
		WORKING,
		/** Doing Additional or Exceptional work of this task (stateInt=3)*/
		WORKING_ADDITIONALLY,
		/** Finished this task (stateInt=4)*/
		FINISHED,
	}
	
	// Constraint variables on simulation
	private final String id; // ID
	private final String nodeId; // TaskNode ID
	private final String name;
	private final double defaultWorkAmount;
	private final double progress;
	private final double additionalWorkAmount;
	private final boolean needFacility;
	private final List<BaseTask> inputTaskList = new ArrayList<>();
	private final List<BaseTask> outputTaskList = new ArrayList<>();
	private int dueDate;
	private List<BaseTeam> allocatedTeamList = new ArrayList<>();
	private List<BaseFacilityGroup> allocatedFacilityGroupList = new ArrayList<>();
	private final List<BaseComponent> targetComponentList = new ArrayList<>();
	
	// Changeable variable on simulation
	private double est = 0; // Earliest start time
	private double eft = 0; // Earliest finish time
	private double lst = 0; // Latest start time
	private double lft = 0; // Latest finish time
	private double remainingWorkAmount; // remaining work amount
	private double actualWorkAmount; // actual work amount
	private TaskState state = TaskState.NONE; // state of this task
	private int stateInt = 0;
	private List<Integer> readyTimeList = new ArrayList<Integer>(); // list of ready time of one task
	private List<Integer> startTimeList = new ArrayList<Integer>(); // list of start time of one task
	private List<Integer> finishTimeList = new ArrayList<Integer>(); // list of finish time of one task
	private boolean additionalTaskFlag = false;
	private List<BaseWorker> allocatedWorkerList = new ArrayList<>();
	private List<BaseFacility> allocatedFacilityList = new ArrayList<>();
	
	/**
	 * This is the constructor.
	 * @param taskNode
	 */
	public BaseTask(TaskNode taskNode) {
		this.id = UUID.randomUUID().toString();
		this.nodeId = taskNode.getId();
		this.name = taskNode.getName();
		this.defaultWorkAmount = taskNode.getWorkAmount();
		this.progress = taskNode.getProgress();
		this.additionalWorkAmount = taskNode.getAdditionalWorkAmount();
		this.needFacility = taskNode.isNeedFacility();
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		est = 0;
		eft = 0;
		lst = 0;
		lft = 0;
		
		remainingWorkAmount = defaultWorkAmount * (1.0 - progress);
		actualWorkAmount = defaultWorkAmount * (1.0 - progress);
		
		state = TaskState.NONE;
		stateInt = 0;
		if(progress > 0.0 && progress <1.0) {
			state = TaskState.READY;
			stateInt = 1;
			this.readyTimeList.add(-1);
		}else if(progress>=1.0){
			state = TaskState.FINISHED;
			stateInt = 4;
			this.readyTimeList.add(-1);
			this.startTimeList.add(-1);
			this.finishTimeList.add(-1);
		}
		
		additionalTaskFlag = false;
		allocatedWorkerList = new ArrayList<>();
		allocatedFacilityList = new ArrayList<>();
	}
	
	/**
	 * Add input task.
	 * @param task
	 */
	public void addInputTask(BaseTask task) {
		inputTaskList.add(task);
	}
	
	/**
	 * Add output task.
	 * @param task
	 */
	public void addOutputTask(BaseTask task) {
		outputTaskList.add(task);
	}
	
	/**
	 * Add target component of this task.
	 * @param component
	 */
	public void addTargetComponent(BaseComponent component) {
		targetComponentList.add(component);
	}
	
	/**
	 * Check whether this task has to be READY or not.<br>
	 * If all input tasks are FINISHED and the state of this task is NONE, change the state of this task to READY.
	 */
	public void checkReady(int time) {
		if (isNone() && inputTaskList.stream().allMatch(t -> t.isFinished())){
			state = TaskState.READY;
			stateInt = 1;
			addReadyTime(time);
		}
	}
	
	/**
	 * Check whether the state of this task has to be WORKING or not.<br>
	 * If the state of this task is READY and this task is already allocated someone,
	 * change the state of this task and allocated resources to WORKING, and add the information of start time to this task.
	 * @param time
	 */
	public void checkWorking(int time) {
		if (isReady() && allocatedWorkerList.size() > 0) {
			state = TaskState.WORKING;
			stateInt = 2;
			addStartTime(time);
			for(BaseWorker allocatedWorker : allocatedWorkerList) {
				allocatedWorker.setStateWorking();
				allocatedWorker.addStartTime(time);
				allocatedWorker.addAssignedTask(this);
			}
			if (needFacility) {
				for(BaseFacility allocatedFacility : allocatedFacilityList) {
					allocatedFacility.setStateWorking();
					allocatedFacility.addStartTime(time);
					allocatedFacility.addAssignedTask(this);
				}

			}
		}
	}
	
	/**
	 * If remaining work amount of this task is lower than 0, check whether the state of this task has to be FINISHED or not.<br>
	 * Whether additional work is assigned to this task or not is judged by additionalTaskFlag.
	 * If finishing normally, the state of this task is changed to FINISHED and the state of assigned resource is changed to FREE, and record the finish time to this task and assigned resource.
	 * If additional work is assigned, add the additional work amount to remaining work amount and record the start time of additional work.
	 * @param time
	 */
	public void checkFinished(int time) {
		if (remainingWorkAmount <= 0) {
			if (isWorking()) {
				addFinishTime(time);
				remainingWorkAmount = 0;
				
				//Finish normally.
				state = TaskState.FINISHED;
				stateInt = 4;
				for(BaseWorker allocatedWorker : allocatedWorkerList) {
					if(allocatedWorker.getAssignedTaskList().stream().filter(t -> t.getStateInt() < 4).count() == 0) {
						allocatedWorker.setStateFree();
					}
					allocatedWorker.addFinishTime(time);
				}
				if (needFacility) {
					for(BaseFacility allocatedFacility : allocatedFacilityList) {
						allocatedFacility.setStateFree();
						allocatedFacility.addFinishTime(time);

					}
					
				}
				
				if (additionalTaskFlag) {
					//Additional work
					//TODO check and update the logic of adding additional work.
					state = TaskState.WORKING_ADDITIONALLY;
					stateInt = 3;
					remainingWorkAmount = additionalWorkAmount;
					actualWorkAmount += additionalWorkAmount;
					addReadyTime(time + 1);
					addStartTime(time + 1);
					
					//Just assign worker and facility again.
					for(BaseWorker allocatedWorker : allocatedWorkerList) {
						allocatedWorker.addStartTime(time+1);
						allocatedWorker.addAssignedTask(this);
					}
					if (needFacility) {
						for(BaseFacility allocatedFacility : allocatedFacilityList) {
							allocatedFacility.addStartTime(time+1);
							allocatedFacility.addAssignedTask(this);
						}
					}
					
					additionalTaskFlag = false;
				}
				
			} else if (isWorkingAdditionally()) {
				addFinishTime(time);
				remainingWorkAmount = 0;
				state = TaskState.FINISHED;
				stateInt = 4;
				for(BaseWorker allocatedWorker : allocatedWorkerList) {
					allocatedWorker.setStateFree();
					allocatedWorker.addFinishTime(time);
				}
				if (needFacility) {
					for(BaseFacility allocatedFacility : allocatedFacilityList) {
						allocatedFacility.setStateFree();
						allocatedFacility.addFinishTime(time);
					}

				}
			}
		}
	}
	
	/**
	 * Performing this task by following steps:<br>
	 * 1. Decreasing remaining work amount and adding cost to resource.<br>
	 * 2. Updating error value considering quality skill point.<br>
	 * 3. Judging additional work is occurred or not by checking the error value.(if componentErrorRework is TRUE)
	 * @param time
	 * @param componentErrorRework
	 */
	public void perform(int time, boolean componentErrorRework) {
		if (isWorking() || isWorkingAdditionally()) {
			double workAmount = 0;
			double noErrorProbability = 1.0;
			for(BaseWorker allocatedWorker : allocatedWorkerList) {
				workAmount += allocatedWorker.getWorkAmountSkillPoint(this);
				noErrorProbability -= allocatedWorker.getQualitySkillPoint(this); // Probability of success this task
			}
			if (needFacility) {
				for(BaseFacility allocatedFacility : allocatedFacilityList) {
					workAmount *= allocatedFacility.getWorkAmountSkillPoint(this);
					//noErrorProbability *= 1.0 - allocatedFacility.getQualitySkillPoint(this);
				}
			}
			remainingWorkAmount -= workAmount;
			for (BaseComponent c : targetComponentList) {
				c.updateErrorValue(noErrorProbability);
			}

			// Additional work
			if(componentErrorRework){
				if (isWorking() && hasAdditionalTask() && !additionalTaskFlag) { // because additional work of each task is occurred only once in this simulation.
					// If additional work is occurred, all related components is added to additionalTaskFlag.
					if (targetComponentList.stream().anyMatch(c -> c.checkIfErrorIsOverTolerance())) additionalTaskFlag = true;
				}
			}
		}
	}
	
	/**
	 * Check whether the state of this task is NONE.
	 * @return
	 */
	public boolean isNone() {
		return state == TaskState.NONE;
	}
	
	/**
	 * Check whether the state of this task is READY.
	 * @return
	 */
	public boolean isReady() {
		return state == TaskState.READY;
	}
	
	/**
	 * Check whether the state of this task is WORKING.
	 * @return
	 */
	public boolean isWorking() {
		return state == TaskState.WORKING;
	}
	
	/**
	 * Check whether the state of this task is ADDITIONAL WORKING.
	 * @return
	 */
	public boolean isWorkingAdditionally() {
		return state == TaskState.WORKING_ADDITIONALLY;
	}
	
	/**
	 * Check whether the state of this task is FINISHED.
	 * @return
	 */
	public boolean isFinished() {
		return state == TaskState.FINISHED;
	}
	
	/**
	 * Check whether this task has the attributes of additional work.
	 * @return
	 */
	public boolean hasAdditionalTask() {
		return additionalWorkAmount > 0;
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
	 * Get the default or initial work amount.
	 * @return the defaultWorkAmount
	 */
	public double getDefaultWorkAmount() {
		return defaultWorkAmount;
	}
	
	/**
	 * Get the default or initial work amount.
	 * @return the defaultAdditionalWorkAmount
	 */
	public double getDefaultAdditionalWorkAmount() {
		return additionalWorkAmount;
	}

	/**
	 * Need facility or not for performing this task.
	 * @return the needFacility
	 */
	public boolean isNeedFacility() {
		return needFacility;
	}

	/**
	 * Get the list of target component.
	 * @return the targetComponentList
	 */
	public List<BaseComponent> getTargetComponentList() {
		return targetComponentList;
	}

	/**
	 * Get allocated team list of this task.
	 * @return the allocatedTeam
	 */
	public List<BaseTeam> getAllocatedTeamList() {
		return allocatedTeamList;
	}

	/**
	 * Add allocated team of this task.
	 * @param allocatedTeam the allocatedTeam to set
	 */
	public void addAllocatedTeam(BaseTeam allocatedTeam) {
		this.allocatedTeamList.add(allocatedTeam);
	}
	
	/**
	 * Get allocated team list of this task.
	 * @return the allocatedTeam
	 */
	public List<BaseFacilityGroup> getAllocatedFacilityGroupList() {
		return allocatedFacilityGroupList;
	}
	/**
	 * Add allocated team of this task.
	 * @param allocatedTeam the allocatedTeam to set
	 */
	public void addAllocatedFacilityGroup(BaseFacilityGroup allocatedFacilityGroup) {
		this.allocatedFacilityGroupList.add(allocatedFacilityGroup);
	}
	/**
	 * Get the list of input tasks.
	 * @return the inputTaskList
	 */
	public List<BaseTask> getInputTaskList() {
		return inputTaskList;
	}

	/**
	 * GEt the list of output tasks.
	 * @return the outputTaskList
	 */
	public List<BaseTask> getOutputTaskList() {
		return outputTaskList;
	}

	/**
	 * Get the due date.
	 * @return the dueDate
	 */
	public int getDueDate() {
		return dueDate;
	}

	/**
	 * Set the due date.
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Get the earliest start time.
	 * @return the est
	 */
	public double getEst() {
		return est;
	}

	/**
	 * Set the earliest start time.
	 * @param est the est to set
	 */
	public void setEst(double est) {
		this.est = est;
	}

	/**
	 * Get the earliest finish time.
	 * @return the eft
	 */
	public double getEft() {
		return eft;
	}

	/**
	 * Set the earliest finish time.
	 * @param eft the eft to set
	 */
	public void setEft(double eft) {
		this.eft = eft;
	}

	/**
	 * Get the latest start time.
	 * @return the lst
	 */
	public double getLst() {
		return lst;
	}

	/**
	 * Set the latest start time.
	 * @param lst the lst to set
	 */
	public void setLst(double lst) {
		this.lst = lst;
	}

	/**
	 * Get the latest finish time.
	 * @return the lft
	 */
	public double getLft() {
		return lft;
	}

	/**
	 * Set the latest start time.
	 * @param lft the lft to set
	 */
	public void setLft(double lft) {
		this.lft = lft;
	}
	/**
	 * Get the remaining work amount.
	 * @return the remainingWorkAmount
	 */
	public double getRemainingWorkAmount() {
		return remainingWorkAmount;
	}

	/**
	 * Get the actual work amount.
	 * @return the actualWorkAmount
	 */
	public double getActualWorkAmount() {
		return actualWorkAmount;
	}
	
	

	/**
	 * Get the state(int) of this task.
	 * @return the stateInt
	 */
	public int getStateInt() {
		return stateInt;
	}
	
	/**
	 * Get the ready time list
	 * @return the readyTimeList
	 */
	public List<Integer> getReadyTimeList() {
		return readyTimeList;
	}
	
	/**
	 * Get the start time list.
	 * @return the startTimeList
	 */
	public List<Integer> getStartTimeList() {
		return startTimeList;
	}
	
	/**
	 * Get the finish time.
	 * @return the finishTimeList
	 */
	public List<Integer> getFinishTimeList() {
		return finishTimeList;
	}
	
	/**
	 * Add ready time.
	 * @param time
	 */
	public void addReadyTime(int time) {
		readyTimeList.add(time);
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
	 * Get the allocated worker list.
	 * @return the allocatedWorkerList
	 */
	public List<BaseWorker> getAllocatedWorkerList() {
		return allocatedWorkerList;
	}
	
	/**
	 * Check whether worker is already assigned or not.
	 * @param worker
	 * @return
	 */
	public boolean isAlreadyAssigned(BaseWorker worker) {
		return allocatedWorkerList.stream().map(w -> w.getId()).anyMatch(worker.getId()::equals);
	}

	/**
	 * Add the allocated worker.
	 * @param allocatedWorker the allocatedWorker to set
	 */
	public void addAllocatedWorker(BaseWorker allocatedWorker) {
		this.allocatedWorkerList.add(allocatedWorker);
	}

	/**
	 * Get the allocated facility.
	 * @return the workingFacility
	 */
	public List<BaseFacility> getAllocatedFacilityList() {
		return allocatedFacilityList;
	}

	/**
	 * Set the allocated facility.
	 * @param arrayList the allocatedFacility to set
	 */
	public void setAllocatedFacility(ArrayList<BaseFacility> arrayList) {
		this.allocatedFacilityList = arrayList;
	}
	
	public void addAllocatedFacility(BaseFacility facility) {
		this.allocatedFacilityList.add(facility);
	}
	
	public void removeAllocatedFacility(BaseFacility facility) {
		this.allocatedFacilityList.remove(facility);
	}

	public boolean isReadyWithFacility() {
		List<BaseFacilityGroup> necessaryFacility = this.getAllocatedFacilityGroupList().stream().collect(Collectors.toList());
		for (BaseFacility facility : this.allocatedFacilityList) {
			necessaryFacility.remove(facility.getGroup());
		}
		if(necessaryFacility.size() == 0) {
			return true;
		}else {
			return false;
		}
	}
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		String worker = (allocatedWorkerList.size() > 0) ? String.join(",", allocatedWorkerList.stream().map(w -> w.getName()).collect(Collectors.toList())) : "";
		String facility = (allocatedFacilityList.size() > 0) ? String.join(",", allocatedFacilityList.stream().map(f -> f.getName()).collect(Collectors.toList())) : "";
		String inputTaskNames = String.join(",", inputTaskList.stream().map(t -> t.getName()).collect(Collectors.toList())); // DEBUG
		return String.format("[%s] %s WA=%f team=%s w=%s f=%s in=%s", name, state, remainingWorkAmount, allocatedTeamList.stream().map(BaseTeam::getName).collect(Collectors.joining(",")), worker, facility, inputTaskNames);
	}
}
