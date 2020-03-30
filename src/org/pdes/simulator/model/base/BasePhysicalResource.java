package org.pdes.simulator.model.base;

import org.pdes.rcp.model.base.ResourceElement;

public class BasePhysicalResource extends BaseResource{

	protected BaseFacilityGroup group;
	
	public BasePhysicalResource(ResourceElement resourceElement, BaseFacilityGroup group) {
		super(resourceElement);
		this.group = group;
	}
	public boolean hasSkill(BaseTask task) {
			return (task.getAllocatedFacilityGroupList().stream().anyMatch(t -> t.equals(group)) && workAmountSkillMap.containsKey(task.getName()) && workAmountSkillMap.get(task.getName())[0] > 0.0);
	}
	
	/**
	 * Get the team which has this Resource.
	 * @return the team
	 */
	public BaseFacilityGroup getGroup() {
		return group;
	}

	/**
	 * Set the team which has this Resource.
	 * @param team the team to set
	 */
	public void setGroup(BaseFacilityGroup group) {
		this.group = group;
	}
}
