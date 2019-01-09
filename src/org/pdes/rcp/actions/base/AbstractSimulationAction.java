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
package org.pdes.rcp.actions.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.pdes.rcp.core.Activator;
import org.pdes.rcp.dialog.InputSimpleTextDialog;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.rcp.view.editor.ProjectEditor;
import org.pdes.simulator.model.ProjectInfo;
import org.pdes.simulator.model.base.BaseProjectInfo;
import org.pdes.simulator.model.base.BaseTask;

/**
 * This is the abstract One Run Simulation Action.<br>
 * Simulation should do on other UI thread, so concurrent callable class should be developed in upper class.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class AbstractSimulationAction extends Action {
	
	protected final MessageConsoleStream msgStream = Activator.getDefault().getMsgStream();
	
	protected boolean aggregateMode = false;
	protected String outputDir;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		
		//0. Check whether Project is opened or not.
		IWorkbench ib = PlatformUI.getWorkbench();
		ProjectEditor pe = (ProjectEditor) ib.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(pe == null){
			MessageDialog.openError(ib.getActiveWorkbenchWindow().getShell(), "Error", "Project is not opened.");
			return;
		}
		
		//1. Check whether Project will be finished or not by checking workers skill.
		boolean skillCheckResult = true;
		ProjectDiagram pd = (ProjectDiagram)pe.getDiagram();
		BaseProjectInfo project = new ProjectInfo(pd, 1);
		List<BaseTask> taskList = project.getWorkflowList().stream().flatMap(w -> w.getTaskList().stream()).collect(Collectors.toList());
		for(BaseTask task : taskList){
			if(!project.getOrganization().getWorkerList().stream().filter(worker -> worker.hasSkill(task)==true).findFirst().isPresent()) {
				msgStream.println(String.format("\"%s\" cannot be done because of skill information. Exit.", task.getName()));
				skillCheckResult = false;
			}
		}
		if(!skillCheckResult) {
			MessageDialog.openError(ib.getActiveWorkbenchWindow().getShell(), "Error", "Simulation will not be finished because of skill loss.");
			return;
		}
		
		//2. Set the number of workflow and product
		int workflowCount = 1;
//		InputSimpleTextDialog workflowCountTextDialog = new InputSimpleTextDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//		workflowCountTextDialog.setTitleAndMessage("The number of products (or workflows)", "Enter the number of products creating or workflows running.");
//		if (workflowCountTextDialog.open() == Window.OK) {
//			String text = workflowCountTextDialog.getTextString();
//			try {
//				workflowCount = Integer.valueOf(text);
//			} catch (NumberFormatException e) {
//				msgStream.println(String.format("\"%s\" is not integer value. Exit.", text));
//				return;
//			}
//		}
//		if (workflowCount <= 0) {
//			msgStream.println("Enter a positive integer value. Exit.");
//			return;
//		}
		
		
		//3. Set directory for save result.
		DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		directoryDialog.setText("Select a directory to save results.");
		directoryDialog.setFilterPath(new File(pe.getFilePath()).getParent());
		this.outputDir = directoryDialog.open();
		if (outputDir == null) {
			return;
		}
		String dateString = this.getDateString();
		File saveDir = new File(outputDir, dateString);
		if(!saveDir.mkdir()){
			msgStream.println("Not creating the folder for saving resutls.");
			return;
		}
		this.outputDir = saveDir.getPath();
		
		//4. Run simulation
		List<Future<String>> result = this.doSimulation(pd, workflowCount);
		
		//5. Save the result of simulation
		if(aggregateMode) this.saveResult("aggregate.csv", result);
		
		msgStream.println("A result will be saved to " + outputDir);
	}
	
	
	/**
	 * Run simulation.
	 * @param workflowCount 
	 * @param outputDirectoryPath 
	 */
	protected abstract List<Future<String>> doSimulation(ProjectDiagram diagram, int workflowCount);
	
	/**
	 * Save result of simulation.
	 * @param outputDirName
	 * @param result
	 */
	public void saveResult(String resultFileName, List<Future<String>> resultList){
		File resultFile = new File(outputDir, resultFileName);
		try {
			// BOM
			FileOutputStream os = new FileOutputStream(resultFile);
			os.write(0xef);
			os.write(0xbb);
			os.write(0xbf);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
			
			// header
			pw.println(FilenameUtils.getBaseName(resultFile.toString()));
			pw.println(String.join(",", "No", "Cost", "Duration", "Total Work amount"));
			
			try {
				resultList.forEach(result -> {
					try {
						pw.println(result.get());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				msgStream.println("Error was occurred.");
				pw.close();
				os.close();
				return;
			}
			pw.close();
			os.close();
		} catch (IOException e) {
			msgStream.println(e.getMessage());
		}
	}
	
	/**
	 * Get the text of Date for file name.
	 * @return
	 */
	public String getDateString(){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String resultFileName = String.join("_", sdf.format(date));
		return resultFileName;
	}
	
	/**
	 * Set the number of Simulation.
	 * @return
	 */
	public int setNumOfSimulation(){
		int numOfSimulation = 0;
		InputSimpleTextDialog workflowCountTextDialog = new InputSimpleTextDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		workflowCountTextDialog.setTitleAndMessage("The number of simulation running", "Enter the number of simulation running.");
		workflowCountTextDialog.setTextString("1");
		if (workflowCountTextDialog.open() == Window.OK) {
			String text = workflowCountTextDialog.getTextString();
			try {
				numOfSimulation = Integer.valueOf(text);
			} catch (NumberFormatException e) {
				msgStream.println(String.format("\"%s\" is not integer value. Exit.", text));
				return -1;
			}
		}
		if (numOfSimulation <= 0) {
			msgStream.println("Enter a positive integer value. Exit.");
			return -1;
		}
		return numOfSimulation;
	}
}
