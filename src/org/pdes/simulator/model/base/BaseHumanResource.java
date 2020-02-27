package org.pdes.simulator.model.base;

import org.pdes.rcp.model.base.ResourceElement;

public class BaseHumanResource extends BaseResource{

	protected BaseTeam team;
	
	public BaseHumanResource(ResourceElement resourceElement, BaseTeam team) {
		super(resourceElement);
		this.team = team;
	}
	public boolean hasSkill(BaseTask task) {
			return (task.getAllocatedTeamList().stream().anyMatch(t -> t.equals(team)) && workAmountSkillMap.containsKey(task.getName()) && workAmountSkillMap.get(task.getName())[0] > 0.0);
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

}
