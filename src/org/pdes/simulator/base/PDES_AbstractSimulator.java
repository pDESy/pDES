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
package org.pdes.simulator.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.pdes.simulator.model.base.BaseFacility;
import org.pdes.simulator.model.base.BaseOrganization;
import org.pdes.simulator.model.base.BaseProduct;
import org.pdes.simulator.model.base.BaseProjectInfo;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseTeam;
import org.pdes.simulator.model.base.BaseWorker;
import org.pdes.simulator.model.base.BaseWorkflow;

/**
 * This is the abstract simulator for pDES application.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class PDES_AbstractSimulator {
	protected final BaseProjectInfo project;
	protected final List<BaseWorkflow> workflowList;
	protected final BaseOrganization organization;
	protected final List<BaseProduct> productList;
	protected final int concurrencyWorkflowLimit;
	
	protected int time = 0;
	
	protected boolean considerReworkOfErrorTorelance = false;
	
	/**
	 * This is the constructor.
	 * @param workflowList
	 * @param organization
	 * @param productList
	 * @param simultaneousWorkflowLimit
	 */
	public PDES_AbstractSimulator(BaseProjectInfo project){
		this.project = project;
		this.workflowList = project.getWorkflowList();
		this.organization = project.getOrganization();
		this.productList = project.getProductList();
		this.concurrencyWorkflowLimit = project.getConcurrencyWorkflowLimit();
	}
	
	/**
	 * Execute simulator.
	 */
	abstract public void execute();

	/**
	 * Get the time.
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
	
	/**
	 * Initialize before starting simulation
	 */
	public void initialize(){
		this.time = 0;
		workflowList.forEach(w -> w.initialize());
		organization.initialize();
		productList.forEach(p -> p.initialize());
	}
	
	/**
	 * Check whether all tasks are finished or not.
	 * @return
	 */
	public boolean checkAllTasksAreFinished(){
		return workflowList.stream().allMatch(w -> w.isFinished());
	}
	
	/**
	 * Get the list of READY tasks.
	 * @return
	 */
	public List<BaseTask> getReadyTaskList(){
		return workflowList.stream()
				.map(w -> w.getReadyTaskList())
				.collect(
						() -> new ArrayList<>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}
	
	/**
	 * Get the list of Working tasks.
	 * @return
	 */
	public List<BaseTask> getWorkingTaskList(){
		return workflowList.stream()
				.map(w -> w.getWorkingTaskList())
				.collect(
						() -> new ArrayList<>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}
	
	
	/**
	 * Sort Tasks as followings:<br>
	 * 1. Due date<br>
	 * 2. TSLACK (a task which Slack time(LS-ES) is lower has high priority)
	 * @param resourceList
	 */
	public void sortTasks(List<BaseTask> taskList){
		taskList.sort((t1, t2) -> {
			int dd1 = t1.getDueDate();
			int dd2 = t2.getDueDate();
			if (dd1 < dd2) return -1;
			if (dd1 > dd2) return 1;
			double slack1 = t1.getLst() - t1.getEst();
			double slack2 = t2.getLst() - t2.getEst();
			return Double.compare(slack1, slack2);
		});
	}
	
	/**
	 * Sort Worker as followings:<br>
	 * 1. SSP (a resource which amount of skill point is lower has high priority)
	 * @param resourceList
	 */
	public void sortWorkers(List<BaseWorker> resourceList){
		resourceList.sort((w1, w2) -> {
			double sp1 = w1.getTotalWorkAmountSkillPoint();
			double sp2 = w2.getTotalWorkAmountSkillPoint();
			return Double.compare(sp1, sp2);
		});
	}
	
	/**
	 * Sort Facilities as followings:<br>
	 * 1. SSP (a resource which amount of skill point is lower has high priority)
	 * @param resourceList
	 */
	public void sortFacilities(List<BaseFacility> resourceList){
		resourceList.sort((w1, w2) -> {
			double sp1 = w1.getTotalWorkAmountSkillPoint();
			double sp2 = w2.getTotalWorkAmountSkillPoint();
			return Double.compare(sp1, sp2);
		});
	}
	
	/**
	 * Allocate ready tasks to free workers and facilities if necessary.<br>
	 * This method is only for single-task worker simulator.
	 * @param time 
	 * @param readyTaskList
	 * @param freeWorkerList
	 * @param freeFacilityList
	 */
	public void allocateReadyTasksToFreeResourcesForSingleTaskWorkerSimulation(List<BaseTask> readyTaskList, List<BaseWorker> freeWorkerList){
		this.sortTasks(readyTaskList);
		readyTaskList.stream().forEachOrdered(task -> {
			if(this.checkSatisfyingWorkflowLimitForStartingTask(task)){
				Optional<BaseWorker> availableWorker = freeWorkerList.stream().filter(w -> w.hasSkill(task)).findFirst();
				availableWorker.ifPresent(worker ->{
					task.addAllocatedWorker(worker);;
					freeWorkerList.remove(worker);
					
				});
			}
		});
	}
	
	/**
	 * Allocate ready tasks to free workers and facilities if necessary.<br>
	 * This method is only for single-task workers simulator.
	 * @param time 
	 * @param readyAndWorkingTaskList
	 * @param freeWorkerList
	 * @param freeFacilityList
	 */
	public void allocateReadyTasksToFreeResourcesForSingleTaskWorkersSimulation(List<BaseTask> readyAndWorkingTaskList, List<BaseWorker> freeWorkerList){
		this.sortTasks(readyAndWorkingTaskList);
		readyAndWorkingTaskList.stream().forEachOrdered(task -> {
			if(this.checkSatisfyingWorkflowLimitForStartingTask(task)){
				List<BaseWorker> allocatingWorkers = freeWorkerList.stream().filter(w -> w.hasSkill(task)).collect(Collectors.toList());
				for(BaseWorker worker : allocatingWorkers) {
					task.addAllocatedWorker(worker);
					freeWorkerList.remove(worker);
				}
			}
		});
	}
	
	/**
	 * Allocate ready and working tasks to all workers and free facilities if necessary.<br>
	 * This method is only for multi-task worker simulation.
	 * @param readyTaskAndWorkingTaskList
	 * @param allWorkerList
	 * @param freeFacilityList
	 */
	public void allocateTaskToResourcesForMultiTaskWorkerSimulation(List<BaseTask> readyTaskAndWorkingTaskList, List<BaseWorker> allWorkerList) {
		readyTaskAndWorkingTaskList.stream().forEachOrdered(task->{
			if(this.checkSatisfyingWorkflowLimitForStartingTask(task)){
				allWorkerList.stream().filter(w -> w.hasSkill(task)).forEach(w -> {
					if(!task.isAlreadyAssigned(w)) {
						task.addAllocatedWorker(w);
						
					}
				});
			}
		});
	}
	
	/**
	 * Perform and update all workflow in this time.
	 * @param time 
	 * @param componentErrorRework 
	 */
	public void performAndUpdateAllWorkflow(int time, boolean componentErrorRework){
		workflowList.forEach(w -> w.checkWorking(time));//READY -> WORKING
		organization.getWorkingWorkerList().stream().forEach(w -> w.addLaborCost());//pay labor cost
		workflowList.forEach(w -> {
			w.perform(time, componentErrorRework);
		});//update information of WORKING task in each workflow
		workflowList.forEach(w -> w.checkFinished(time));// WORKING -> WORKING_ADDITIONALLY or FINISHED
		workflowList.forEach(w -> w.checkReady(time));// NONE -> READY
		workflowList.forEach(w -> w.updatePERTData(time));//Update PERT information
	}
	
	/**
	 * Check if this task can start by considering workflow limit.
	 * @param task
	 * @return
	 */
	public boolean checkSatisfyingWorkflowLimitForStartingTask(BaseTask task){
		long numOfRunningWorkflow = workflowList.stream()
				.filter(w -> w.isRunning())
				.map(w -> w.getId())
				.count();
		Optional<String> runningWorkflow = workflowList.stream()
				.filter(w -> w.isRunning())
				.filter(w -> w.hasTask(task.getId()))
				.map(w -> w.getId()).findFirst();
		return runningWorkflow.isPresent() || numOfRunningWorkflow < concurrencyWorkflowLimit;
	}
	
	/**
	 * Save the simulation result to the given directory.
	 * @param outputDir
	 */
	public void saveResultFilesInDirectory(String outputDir, String no){
		String fileName = this.getResultFileName(no);//the list of file name
		this.saveResultFileByCsv(outputDir, fileName+".csv");//1. Gantt chart data by csv format.
	}
	
	/**
	 * Get the file name considering 
	 * @param type
	 * @param extension
	 */
	private String getResultFileName(String no){
		return no;
	}
	
	/**
	 * Save result file by csv format.
	 * @param outputDirName
	 * @param resultFileName
	 */
	public void saveResultFileByCsv(String outputDirName, String resultFileName){
		File resultFile = new File(outputDirName, resultFileName);
		String separator = ",";
		try {
			// BOM
			FileOutputStream os = new FileOutputStream(resultFile);
			os.write(0xef);
			os.write(0xbb);
			os.write(0xbf);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
			
			// header
			pw.println(String.join(separator, new String[]{"Total Cost", String.valueOf(project.getTotalCost()), "Duration", String.valueOf(project.getDuration()+1), "Total Work Amount", String.valueOf(project.getTotalActualWorkAmount())}));
			
			// workflow
			pw.println();
			pw.println("Gantt chart of each Task");
			pw.println(String.join(separator , new String[]{"Workflow", "Task", "Assigned Team", "Ready Time", "Start Time", "Finish Time", "Start Time", "Finish Time", "Start Time", "Finish Time"}));
			this.workflowList.forEach(w -> {
				String workflowName = "Workflow ("+w.getDueDate()+")";
				w.getTaskList().forEach(t ->{
					List<String> baseInfo = new ArrayList<String>();
					baseInfo.add(workflowName);
					baseInfo.add(t.getName());
					//baseInfo.add(t.getAllocatedTeam().getName());
					baseInfo.add(t.getAllocatedTeamList().stream().map(BaseTeam::getName).collect(Collectors.joining("+")));
					IntStream.range(0, t.getFinishTimeList().size()).forEach(i -> {
						baseInfo.add(String.valueOf(t.getReadyTimeList().get(i)));
						baseInfo.add(String.valueOf(t.getStartTimeList().get(i)));
						baseInfo.add(String.valueOf(t.getFinishTimeList().get(i)));
					});
					pw.println(String.join(separator ,baseInfo.stream().toArray(String[]::new)));
				});
			});
			
			// product
			pw.println();
			pw.println("Gantt chart of each Component");
			pw.println(String.join(separator , new String[]{"Product", "Component", "Error/Error Torerance", "Start Time", "Finish Time", "Start Time", "Finish Time", "Start Time", "Finish Time"}));
			this.productList.forEach(p -> {
				String productName = "Product ("+p.getDueDate()+")";
				p.getComponentList().forEach(c -> {
					List<String> baseInfo = new ArrayList<String>();
					baseInfo.add(productName);
					baseInfo.add(c.getName());
					baseInfo.add(String.valueOf(c.getError())+"/"+String.valueOf(c.getErrorTolerance()));
					IntStream.range(0, c.getFinishTimeList().size()).forEach(i -> {
						baseInfo.add(String.valueOf(c.getStartTimeList().get(i)));
						baseInfo.add(String.valueOf(c.getFinishTimeList().get(i)));
					});
					pw.println(String.join(separator ,baseInfo.stream().toArray(String[]::new)));
				});
			});
			// Organization
			pw.println();
			pw.println("Gantt chart of each Resource");
			pw.println(String.join(separator , new String[]{"Team", "Type", "Name", "Start Time", "Finish Time"}));
			this.organization.getTeamList().forEach(t -> {
				String teamName = t.getName();
				
				//Workers
				t.getWorkerList().forEach(w -> {;
					List<String> baseInfo = new ArrayList<String>();
					baseInfo.add(teamName);
					baseInfo.add("Worker");
					baseInfo.add(w.getName());
					IntStream.range(0, w.getAssignedTaskList().size()).forEach(i -> {
						baseInfo.add(String.valueOf(w.getStartTimeList().get(i)));
						baseInfo.add(String.valueOf(w.getFinishTimeList().get(i)));
					});
					pw.println(String.join(separator, baseInfo.stream().toArray(String[]::new)));
				});
			});

			pw.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check whether this simulator considers rework of error tolerance or not.
	 * @return the considerReworkOfErrorTorelance
	 */
	public boolean isConsiderReworkOfErrorTorelance() {
		return considerReworkOfErrorTorelance;
	}

	/**
	 * Set the simulation condition whether this simulator considers rework of error tolerance or not.
	 * @param considerReworkOfErrorTorelance the considerReworkOfErrorTorelance to set
	 */
	public void setConsiderReworkOfErrorTorelance(boolean considerReworkOfErrorTorelance) {
		this.considerReworkOfErrorTorelance = considerReworkOfErrorTorelance;
	}
}
