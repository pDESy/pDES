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
package org.pdes.rcp.model.base;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the ResourceElement class under NodeElement.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class ResourceElement extends AbstractModel{

	private static final long serialVersionUID = -7986118143833430393L;
	
	protected String id;
	protected NodeElement parentNodeElement;
	
	protected String name;
	protected double cost;
	protected Map<String, Double[]> workAmountSkillMap = new HashMap<>();
	protected Map<String, Double> qualitySkillMap = new HashMap<>();
	
	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id.
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the NodeElement which has this ResourceElement.
	 * @return the parentNodeElement
	 */
	public NodeElement getParentNodeElement() {
		return parentNodeElement;
	}
	
	/**
	 * Set the NodeElement which has this ResourceElement.
	 * @param parentNodeElement the parentNodeElement to set
	 */
	public void setParentNodeElement(NodeElement parentNodeElement) {
		this.parentNodeElement = parentNodeElement;
	}
	
	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name.
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the cost information.
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}
	
	/**
	 * Set the cost information.
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	/**
	 * Get the work amount skill map of this ResourceElement.
	 * @return the workAmountSkillMap
	 */
	public Map<String, Double[]> getWorkAmountSkillMap() {
		return workAmountSkillMap;
	}
	
	/**
	 * Set the work amount skill map of this ResourceElement.
	 * @param workAmountSkillMap the workAmountSkillMap to set
	 */
	public void setWorkAmountSkillMap(Map<String, Double[]> workAmountSkillMap) {
		this.workAmountSkillMap = workAmountSkillMap;
	}

	/**
	 * Get the quality skill map of this ResourceElement.
	 * @return the qualitySkillMap
	 */
	public Map<String, Double> getQualitySkillMap() {
		return qualitySkillMap;
	}

	/**
	 * Set the quality skill map of this ResourceElement.
	 * @param qualitySkillMap the qualitySkillMap to set
	 */
	public void setQualitySkillMap(Map<String, Double> qualitySkillMap) {
		this.qualitySkillMap = qualitySkillMap;
	}

	/**
	 * Add or revise of work amount skill information.
	 * @param skillName
	 * @param skillLevel
	 */
	public void addSkillInWorkAmountSkillMap(String skillName, Double[] skillLevel) {
		this.workAmountSkillMap.put(skillName, skillLevel);
	}
	
	/**
	 * Get the work amount skill level {mean, s.d.} of "skillName" skill.
	 * @param skillName
	 * @return
	 */
	public Double[] getWorkAmountSkillLevel(String skillName) {
		if(this.workAmountSkillMap.get(skillName) == null) {
			Double[] zero = {0.0, 0.0};
			return zero;
		}
		return this.workAmountSkillMap.get(skillName);
	}
	
	/**
	 * Add or revise of quality skill information.
	 * @param skillName
	 * @param skillLevel
	 */
	public void addSkillInQualitySkillMap(String skillName, double skillLevel) {
		this.qualitySkillMap.put(skillName, skillLevel);
	}
	
	/**
	 * Get the quality skill level of "skillName" skill.
	 * @param skillName
	 * @return
	 */
	public double getQualitySkillLevel(String skillName) {
		if(this.qualitySkillMap.get(skillName) == null) return 0;
		return this.qualitySkillMap.get(skillName);
	}
	
	/**
	 * Initialize all skill information.
	 * @return
	 */
	public void resetSkills(){
		for(String skillName:workAmountSkillMap.keySet()){
			addSkillInWorkAmountSkillMap(skillName, new Double[]{0.0, 0.0});
		}
		for(String skillName:qualitySkillMap.keySet()){
			addSkillInWorkAmountSkillMap(skillName, new Double[]{0.0, 0.0});
		}
	}
}
