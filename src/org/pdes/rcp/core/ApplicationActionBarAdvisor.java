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
package org.pdes.rcp.core;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.pdes.rcp.actions.NewProjectFileAction;
import org.pdes.rcp.actions.RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkerAction;
import org.pdes.rcp.actions.RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersAction;
import org.pdes.rcp.actions.RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersWithFacilityAction;
import org.pdes.rcp.actions.RunPDES_BasicSimulator_TaskPerfromedBySingleTaskWorkerWithFacilityAction;
import org.pdes.rcp.actions.RunPDES_BasicSimulator_TaskPerformedByMultiTaskWorkersAction;
import org.pdes.rcp.actions.OpenProjectFileAction;
import org.pdes.rcp.actions.SaveAsFileAction;
import org.pdes.rcp.actions.SaveFileAction;

/**
 * This is the subclass of ActionBarAdvisor to configure a window's action bars in this application.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private NewProjectFileAction newProject = new NewProjectFileAction();
	private SaveFileAction save = new SaveFileAction();
	private SaveAsFileAction saveAs = new SaveAsFileAction();
	private OpenProjectFileAction open = new OpenProjectFileAction();
	private RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkerAction basicSimSingleTaskWorker = new RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkerAction();
	private RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersAction basicSimSingleTaskWorkers = new RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersAction();
	private RunPDES_BasicSimulator_TaskPerformedByMultiTaskWorkersAction basicSimMultiTaskWorker = new RunPDES_BasicSimulator_TaskPerformedByMultiTaskWorkersAction();
	private RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersWithFacilityAction basicSimSingleTaskWorkersWithFacility = new RunPDES_BasicSimulator_TaskPerformedBySingleTaskWorkersWithFacilityAction();
	private RunPDES_BasicSimulator_TaskPerfromedBySingleTaskWorkerWithFacilityAction basicSimSingleTaskWorkerWithFacility = new RunPDES_BasicSimulator_TaskPerfromedBySingleTaskWorkerWithFacilityAction();
//	private RunPDES_SimulatorConsideringReworkOfErrorToleranceAction retSim = new RunPDES_SimulatorConsideringReworkOfErrorToleranceAction();
	
	/**
	 * This is the constructor.
	 * @param configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	protected void makeActions(IWorkbenchWindow window) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("File",IWorkbenchActionConstants.GO_INTO);
		menuBar.add(fileMenu);
		
		MenuManager newFileMenu = new MenuManager("New",IWorkbenchActionConstants.M_FILE);
		fileMenu.add(newFileMenu);
		newFileMenu.add(newProject);
		fileMenu.add(open);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		
		MenuManager simulationMenu = new MenuManager("Simulation","Simulation");
		menuBar.add(simulationMenu);
		MenuManager runMenu = new MenuManager("Run");
		simulationMenu.add(runMenu);
		runMenu.add(basicSimSingleTaskWorker);
		runMenu.add(basicSimSingleTaskWorkers);
		runMenu.add(basicSimMultiTaskWorker);
		runMenu.add(basicSimSingleTaskWorkerWithFacility);
		runMenu.add(basicSimSingleTaskWorkersWithFacility);
//		runMenu.add(retSim);
	}
	
}
