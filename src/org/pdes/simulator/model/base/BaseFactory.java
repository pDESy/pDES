package org.pdes.simulator.model.base;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BaseFactory {
	/**
	 * Factory model for discrete event simulation.<br>
	 * This model has the list of Facility groups.<br>
	 * @author Shinnosuke Wanaka <wanaka-s@m.mpat.go.jp>
	 *
	 */
	private final List<BaseFacilityGroup> facilityGroupList;

	/**
	 * This is the constructor.
	 * @param facilityGroupList
	 */
	public BaseFactory(List<BaseFacilityGroup> facilityGroupList) {
		this.facilityGroupList = facilityGroupList;
	}

	/**
	 * Initialize
	 */
	public void initialize() {
		facilityGroupList.forEach(f -> f.initialize());
	}

	/**
	 * Get the facility group which has same id.
	 * @param id
	 * @return
	 */
	public BaseFacilityGroup getFacilityGroup(String id){
		for(BaseFacilityGroup group : this.facilityGroupList){
			if(group.getId().equals(id)) return group;
		}
		return null;
	}

	/**
	 * Get the list of free facilities.
	 * @return
	 */
	public List<BaseFacility> getFreeFacilityList() {
		return facilityGroupList.stream()
				.map(f -> f.getFreeFacilityList())
				.collect(
						() -> new ArrayList<BaseFacility>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}

	/**
	 * Get the list of working facilities.
	 * @return
	 */
	public List<BaseFacility> getWorkingFacilityList() {
		return facilityGroupList.stream()
				.map(t -> t.getWorkingFacilityList())
				.collect(
						() -> new ArrayList<BaseFacility>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}

	/**
	 * Get the list of all facilities.
	 * @return
	 */
	public List<BaseFacility> getWorkerList() {
		return facilityGroupList.stream()
				.map(t -> t.getFacilityList())
				.collect(
						() -> new ArrayList<BaseFacility>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}


	/**
	 * Get total cost of this organization.
	 * @return
	 */
	public double getTotalCost() {
		return facilityGroupList.stream().mapToDouble(t -> t.getTotalCost()).sum();
	}

	/**
	 * Get the list of facility group.
	 * @return the facilityGroupList
	 */
	public List<BaseFacilityGroup> getFacilityGroupList() {
		return facilityGroupList;
	}

	/**
	 * Transfer to text data.
	 */
	public String toString() {
		return String.join("\n", facilityGroupList.stream().map(t -> t.toString()).collect(Collectors.toList()));
	}
}

