package org.pdes.rcp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pdes.rcp.model.base.NodeElement;

/**
 * This is the FacilityNode class.<br>
 * @author Shinnosuke Wanaka <wanaka-s@m.mpat.go.jp>
 */
@SuppressWarnings("serial") 
public class FacilityNode extends NodeElement{

	private String name = "";
	protected List<FacilityElement> facilityList = new ArrayList<FacilityElement>();
	
	/**
	 * This is the constructor.
	 */
	public FacilityNode(){
		String newName = "New Facility";
		this.setName(newName);
	}

	/**
	 * Get the name of TeamNode
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of TeamNode.
	 * @param name the name to set
	 */
	public void setName(String name) {
		String old = this.name;
		this.name=name;
		firePropertyChange("name",old,name);
	}

	/**
	 * Get the list of facilities.
	 * @return the facilityList
	 */
	public List<FacilityElement> getFacilityList() {
		return facilityList;
	}

	/**
	 * Set the list of facilities
	 * @param facilityList the facilityList to set
	 */
	public void setFacilityList(List<FacilityElement> facilityList) {
		this.facilityList = facilityList;
	}
	
	/**
	 * Add a facility to the list of facility.
	 * @param worker
	 */
	public void addFacility(FacilityElement facility){
		this.facilityList.add(facility);
	}
	
	/**
	 * Remove "number"th facility from the list of facility.
	 * @param worker
	 */
	public void deleteFacility(int number){
		this.facilityList.remove(number);
	}
	
	/**
	 * Initialize the list of facilities.
	 */
	public void initializeFacilityList(){
		this.facilityList = new ArrayList<FacilityElement>();
	}
	
	/**
	 * Get the list of facility's name.
	 * @return
	 */
	public List<String> getFacilityNameList(){
		List<String> facilityNameList = new ArrayList<String>();
		for(FacilityElement facility:this.facilityList){
			facilityNameList.add(facility.getName());
		}
		return facilityNameList;
	}

	/**
	 * Get the name list of allocated tasks.
	 * @return
	 */
	public List<String> getNameListOfAllocatedTasks(){
		return this.getOutgoingLinkList().stream()
				.filter(s -> s instanceof AllocationLink)
				.map(aLink -> ((TaskNode)aLink.getDestinationNode()).getName())
				.distinct()
				.collect(Collectors.toList());
	}
}
