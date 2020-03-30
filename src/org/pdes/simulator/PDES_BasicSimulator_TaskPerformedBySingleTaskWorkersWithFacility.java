package org.pdes.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pdes.simulator.base.PDES_AbstractSimulator;
import org.pdes.simulator.model.base.BaseFacility;
import org.pdes.simulator.model.base.BaseFacilityGroup;
import org.pdes.simulator.model.base.BaseFactory;
import org.pdes.simulator.model.base.BaseProjectInfo;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseWorker;

public class PDES_BasicSimulator_TaskPerformedBySingleTaskWorkersWithFacility extends PDES_AbstractSimulator {
    private boolean considerReworkOfErrorTorelance = false;
    private final BaseFactory factory;
	/**
	 * This is the constructor.
	 * @param workflowList
	 * @param organization
	 * @param productList
	 * @param simultaneousWorkflowLimit
	 */
	public PDES_BasicSimulator_TaskPerformedBySingleTaskWorkersWithFacility(BaseProjectInfo project) {
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
			List<BaseTask> workingTaskList = this.getWorkingTaskList();
			List<BaseTask> readyAndWorkingTaskList = Arrays.asList(readyTaskList,workingTaskList).stream().flatMap(list -> list.stream()).collect(Collectors.toList());
			List<BaseWorker> freeWorkerList = organization.getFreeWorkerList();
			List<BaseFacility> freeFacilityList = factory.getFreeFacilityList();
			
			//2. Sort ready task and free resources
			this.sortTasks(readyAndWorkingTaskList);
			this.sortWorkers(freeWorkerList);
			this.sortFacilities(freeFacilityList);
			
			//3. Allocate ready tasks to free resources
			this.allocateReadyTasksToFreeResources(readyAndWorkingTaskList, freeWorkerList, freeFacilityList);
			
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
	
	private void allocateReadyTasksToFreeResources(List<BaseTask> readyAndWorkingTaskList, List<BaseWorker> freeWorkerList, List<BaseFacility> freeFacilityList) {
		for(BaseTask task : readyAndWorkingTaskList) {
			if(this.checkSatisfyingWorkflowLimitForStartingTask(task)){
				if(task.isReady()) {
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
						List<BaseWorker> allocatingWorkers = freeWorkerList.stream().filter(w -> w.hasSkill(task)).collect(Collectors.toList());
						if(allocatingWorkers.size() == 0) {
							task.getAllocatedFacilityList().stream().forEach(f -> {
								freeFacilityList.add(f);
							});
							task.setAllocatedFacility(new ArrayList<BaseFacility>());
						}
						for(BaseWorker worker : allocatingWorkers) {
							task.addAllocatedWorker(worker);
							freeWorkerList.remove(worker);
						}
					} else {
						task.getAllocatedFacilityList().stream().forEach(f -> {
							freeFacilityList.add(f);
						});
						task.setAllocatedFacility(new ArrayList<BaseFacility>());
					}
				}else if(task.isWorking()) {
					List<BaseWorker> allocatingWorkers = freeWorkerList.stream().filter(w -> w.hasSkill(task)).collect(Collectors.toList());
					for(BaseWorker worker : allocatingWorkers) {
						task.addAllocatedWorker(worker);
						freeWorkerList.remove(worker);
					}
				}
			}
		}
	}
}
