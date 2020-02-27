package org.pdes.simulator.model.base;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pdes.rcp.model.FacilityNode;

/**
 * Facility model for discrete event simulator.<br>
 * Facility model has the list of facilities.<br>
 * @author Shinnosuke Wanaka <wanaka-s@m.mpat.go.jp>
 *
 */

public class BaseFacilityGroup {
	private final String id;
	private final String nodeId;
	private final String name;
	private final List<BaseFacility> facilityList;

	public BaseFacilityGroup(FacilityNode facilityNode) {
		this.id = UUID.randomUUID().toString();
		this.nodeId = facilityNode.getId();
		this.name = facilityNode.getName();
		this.facilityList = facilityNode.getFacilityList().stream().map(f -> new BaseFacility(f, this)).collect(Collectors.toList());
	}
	
	public void initialize() {
		facilityList.forEach(f -> f.initialize());
	}
	
	public List<BaseFacility> getFreeFacilityList() {
		return facilityList.stream().filter(f -> f.isFree()).collect(Collectors.toList());
	}
	
	public List<BaseFacility> getWorkingFacilityList() {
		return facilityList.stream().filter(f -> f.isWorking()).collect(Collectors.toList());
	}
	
	public double getTotalCost() {
		double facilityTotalCost = facilityList.stream().mapToDouble(f -> f.getTotalCost()).sum();
		return facilityTotalCost;
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
	 * Get the list of workers.
	 * @return the workerList
	 */
	public List<BaseFacility> getFacilityList() {
		return facilityList;
	}
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		String str = "[" + name + "]\n";
		str += String.join("\n", facilityList.stream().map(w -> w.toString()).collect(Collectors.toList()));
		str += "\n";
		return str;
	}
	
}
