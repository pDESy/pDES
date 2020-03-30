package org.pdes.simulator;

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

import org.pdes.simulator.base.PDES_AbstractSimulator;
import org.pdes.simulator.model.base.BaseFacility;
import org.pdes.simulator.model.base.BaseFacilityGroup;
import org.pdes.simulator.model.base.BaseFactory;
import org.pdes.simulator.model.base.BaseProjectInfo;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseTeam;
import org.pdes.simulator.model.base.BaseWorker;

public class PDES_BasicSimulator_TaskPerformedBySingleTaskWorkerWithFacility extends PDES_AbstractSimulator{
	

	private boolean considerReworkOfErrorTorelance = false;
    private final BaseFactory factory;
	/**
	 * This is the constructor.
	 * @param workflowList
	 * @param organization
	 * @param productList
	 * @param simultaneousWorkflowLimit
	 */
    public PDES_BasicSimulator_TaskPerformedBySingleTaskWorkerWithFacility(BaseProjectInfo project) {
		super(project);
		this.factory = project.factory;
	}

	/* (non-Javadoc)
	 * @see org.pdes.simulator.base.PDES_AbstractSimulator#execute()
	 */
	@Override
	public void execute() {
		this.initialize();

		while(true){
			
			//0. Check finished or not.
			if(checkAllTasksAreFinished()) {
				System.out.println("Finished");
				return;
			}
			
			//1. Get ready task and free resources
			List<BaseTask> readyTaskList = this.getReadyTaskList();
			List<BaseWorker> freeWorkerList = organization.getFreeWorkerList();
			List<BaseFacility> freeFacilityList = factory.getFreeFacilityList();
			
			//2. Sort ready task and free resources
			this.sortTasks(readyTaskList);
			this.sortWorkers(freeWorkerList);
			this.sortFacilities(freeFacilityList);
			
			//3. Allocate ready tasks to free resources
			this.allocateReadyTasksToFreeResources(readyTaskList, freeWorkerList, freeFacilityList);
			
			//4. Perform WORKING tasks and update the status of each task.
			this.performAndUpdateAllWorkflow(time, considerReworkOfErrorTorelance);
			time++;
		}
	}
	public void initialize() {
		super.initialize();
		this.factory.initialize();
	}
	public void sortFacilities(List<BaseFacility> resourceList) {
		resourceList.sort((w1, w2) -> {
			double sp1 = w1.getTotalWorkAmountSkillPoint();
			double sp2 = w2.getTotalWorkAmountSkillPoint();
			return Double.compare(sp1, sp2);
		});
	}
	
	private void allocateReadyTasksToFreeResources(List<BaseTask> readyTaskList, List<BaseWorker> freeWorkerList, List<BaseFacility> freeFacilityList) {
		for(BaseTask task : readyTaskList) {
			if(this.checkSatisfyingWorkflowLimitForStartingTask(task)){
				List<BaseFacilityGroup> necessaryFacility = task.getAllocatedFacilityGroupList().stream().collect(Collectors.toList());
				List<BaseFacility> allocatingFacilityList = freeFacilityList.stream().filter(f -> f.hasSkill(task)).collect(Collectors.toList());
				for (BaseFacility facility : allocatingFacilityList) {
					if(necessaryFacility.contains(facility.getGroup()) && !task.isReadyWithFacility()){
						task.addAllocatedFacility(facility);
						freeFacilityList.remove(facility);
						necessaryFacility.remove(facility.getGroup());
					}
				}
				if(task.isReadyWithFacility()) {
					Optional<BaseWorker> availableWorker = freeWorkerList.stream().filter(w -> w.hasSkill(task)).findFirst();
					availableWorker.ifPresent(worker ->{
						task.addAllocatedWorker(worker);
						freeWorkerList.remove(worker);
						
					});
					if(!availableWorker.isPresent()){
						task.getAllocatedFacilityList().stream().forEach(f -> {
							freeFacilityList.add(f);
						});
						task.setAllocatedFacility(new ArrayList<BaseFacility>());
					}
				} else {
					task.getAllocatedFacilityList().stream().forEach(f -> {
						freeFacilityList.add(f);
					});
					task.setAllocatedFacility(new ArrayList<BaseFacility>());
				}
			}
		}
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
			pw.println("Gantt chart of each Worker");
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

			// Factory
			pw.println();
			pw.println("Gantt chart of each Facility");
			pw.println(String.join(separator , new String[]{"Team", "Type", "Name", "Start Time", "Finish Time"}));
			this.factory.getFacilityGroupList().forEach(f -> {
				String groupName = f.getName();
				
				//Facilities
				f.getFacilityList().forEach(w -> {;
					List<String> baseInfo = new ArrayList<String>();
					baseInfo.add(groupName);
					baseInfo.add("Facility");
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
}
