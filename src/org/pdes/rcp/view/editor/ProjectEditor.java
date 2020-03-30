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
package org.pdes.rcp.view.editor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.pdes.rcp.controller.editpart.ProjectEditorEditPartFactory;
import org.pdes.rcp.model.AllocationLink;
import org.pdes.rcp.model.ComponentHierarchyLink;
import org.pdes.rcp.model.ComponentNode;
import org.pdes.rcp.model.FacilityNode;
import org.pdes.rcp.model.TargetComponentLink;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.rcp.model.SubWorkflowNode;
import org.pdes.rcp.model.TaskDependencyLink;
import org.pdes.rcp.model.TaskNode;
import org.pdes.rcp.model.TeamLink;
import org.pdes.rcp.model.TeamNode;
import org.pdes.rcp.model.base.AbstractModel;
import org.pdes.rcp.model.base.Link;
import org.pdes.rcp.core.Activator;
import org.pdes.rcp.view.parts.SelectedModelViewPart;
import org.pdes.rcp.view.figure.FigureConstants;
import org.pdes.rcp.view.editor.base.DiagramEditor;
import org.pdes.rcp.view.editor.base.DiagramEditorContextMenuProvider;

/**
 * This is the DiagramEditor for editing ProjectDiagram.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class ProjectEditor extends DiagramEditor {
	
	private Map<String,Boolean> visibleMap;
	
	public ProjectEditor(){
		super();
		visibleMap = new HashMap<String,Boolean>();
		Arrays.stream(ProjectEditorConstVariables.visibleVariables)
				.forEach(visibleTargetString -> visibleMap.put(visibleTargetString, true));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ProjectEditorEditPartFactory());
		DiagramEditorContextMenuProvider menuProvider = new DiagramEditorContextMenuProvider(viewer, getActionRegistry());
		viewer.setContextMenu(menuProvider);
		getSite().registerContextMenu(menuProvider, viewer);
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithPalette#getPaletteRoot()
	 */
	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		PaletteGroup group = new PaletteGroup("Tool");
		group.add(new PanningSelectionToolEntry());
		group.add(new MarqueeToolEntry());
		root.add(group);
		
		// for Node
		PaletteDrawer drawer = new PaletteDrawer("Node");
		
		CreationToolEntry entry = new CreationToolEntry("Team", "Make team", new SimpleFactory(
				TeamNode.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.TEAM_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.TEAM_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		entry = new CreationToolEntry("Task", "Make task", new SimpleFactory(
				TaskNode.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.TASK_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.TASK_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		entry = new CreationToolEntry("FacilityGroup", "Make facility group", new SimpleFactory(
		FacilityNode.class), Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, FigureConstants.FACILITY_IMAGE_PATH), Activator
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						FigureConstants.FACILITY_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
//		entry = new CreationToolEntry("Sub-workflow", "Make sub-workflow", new SimpleFactory(
//				SubWorkflowNode.class), Activator.imageDescriptorFromPlugin(
//						Activator.PLUGIN_ID, FigureConstants.WORKFLOW_IMAGE_PATH), Activator
//						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
//								FigureConstants.WORKFLOW_IMAGE_PATH)); 
//		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
//		drawer.add(entry);
		
		entry = new CreationToolEntry("Component", "Make component", new SimpleFactory(
				ComponentNode.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.COMPONENT_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.COMPONENT_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		root.add(drawer);
		// end of for Node
		
		// for Link
		drawer = new PaletteDrawer("Link");
		entry = new ConnectionCreationToolEntry("Task:DependencyLink", "Connect between tasks", new SimpleFactory(
				TaskDependencyLink.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.ARROW_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.ARROW_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		entry = new ConnectionCreationToolEntry("Component:HierarchyLink", "Connect between components", new SimpleFactory(
				ComponentHierarchyLink.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.ARROW_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.ARROW_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		entry = new ConnectionCreationToolEntry("AllocationLink", "Connect from team to task", new SimpleFactory(
				AllocationLink.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.ARROW_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.ARROW_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		entry = new ConnectionCreationToolEntry("ComponentToTaskLink", "Connect from component to task", new SimpleFactory(
				TargetComponentLink.class), Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, FigureConstants.ARROW_IMAGE_PATH), Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								FigureConstants.ARROW_IMAGE_PATH)); 
		entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
		drawer.add(entry);
		
		root.add(drawer);
		// end of for Link
		
		return root;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		ProjectEditorInput pei = (ProjectEditorInput) getEditorInput();
		
		diagram = new ProjectDiagram();
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			DiagramReader reader = new DiagramReader((ProjectDiagram)diagram, pei);
			dialog.run(true, false, reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String fileName = "";
		if(new File(pei.getFilePath()).exists()){
			fileName = new File(pei.getFilePath()).getName();
		}else{
			fileName = "New Project";
		}
		this.setTabTitle(fileName);
		
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(diagram);
	}
	
	/**
	 * This is the DiagramReader.<br>
	 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
	 */
	public class DiagramReader implements IRunnableWithProgress {

		ProjectDiagram diagram;
		ProjectEditorInput input;
		
		/**
		 * This is the constructor.
		 * @param diagram
		 * @param input
		 */
		public DiagramReader(ProjectDiagram diagram, ProjectEditorInput input){
			this.diagram = diagram;
			this.input = input;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			
			monitor.beginTask("open", 100);
			filePath = input.getFilePath();
			if(!filePath.equals("New Project")){
				boolean success = ((ProjectDiagram)diagram).readProjectFile(input.getFilePath());
				if(!success) MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", "Error!!");
			}
			monitor.worked(50);
			monitor.done();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#dispose()
	 */
	@Override
	public void dispose() {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage(); 
		if(page == null) return;
		IViewPart view = page.findView("pDES.SelectedModelViewPart");
		SelectedModelViewPart selectedModelView = (SelectedModelViewPart) view;
		selectedModelView.readSelectedModel(null);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!this.equals(getSite().getWorkbenchWindow().getActivePage().getActiveEditor())) return;
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage(); 
		if(page == null) return;
		IViewPart view = page.findView("pDES.SelectedModelViewPart");
		SelectedModelViewPart selectedModelView = (SelectedModelViewPart) view;
		
		if(selection instanceof IStructuredSelection){
		
			@SuppressWarnings("unchecked")
			Iterator<EditPart> iterator = ((IStructuredSelection)selection).iterator();
			
			while(iterator.hasNext()) {
				EditPart i_part = iterator.next();
				EditPart selectedPart = i_part;
				AbstractModel model = (AbstractModel)selectedPart.getModel();
				if(model instanceof TeamNode){
					selectedModelView.readSelectedModel((TeamNode) model);
				}else if(model instanceof FacilityNode) {
					selectedModelView.readSelectedModel((FacilityNode) model);
				}else if(model instanceof TaskNode){
					selectedModelView.readSelectedModel((TaskNode) model);
				}else if(model instanceof SubWorkflowNode){
					selectedModelView.readSelectedModel((SubWorkflowNode) model);
				}else if(model instanceof ComponentNode){
					selectedModelView.readSelectedModel((ComponentNode) model);
				}else if(model instanceof ProjectDiagram){
					selectedModelView.readSelectedModel((ProjectDiagram) model);
				}else if(model instanceof Link){
					selectedModelView.readSelectedModel((Link) model);
				}
				break;
			}
		}
		super.selectionChanged(part, selection);
	}

	/**
	 * Get the map including the visible information of each model.
	 * @return the visibleMap
	 */
	public Map<String, Boolean> getVisibleMap() {
		return visibleMap;
	}

	/**
	 * Change the visible mode of all models in this ProjectEditor.
	 * @param visible
	 */
	public void setSameBooleanInVisibleMap(boolean visible){
		
		Arrays.stream(ProjectEditorConstVariables.visibleVariables).forEach(visibleTargetString -> visibleMap.put(visibleTargetString, visible));
		this.diagram.getNodeElementList().forEach(nodeElement -> nodeElement.setVisibleFigure(visible));
		this.diagram.getLinkList().forEach(link -> link.setVisible(visible));
		
	}
	
	/**
	 * Change the visible mode of all TaskNodes in this ProjectEditor.
	 */
	public void setVisibleAboutAllTaskNode(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleTask, visible);
		this.diagram.getNodeElementList().stream().filter(node -> node instanceof TaskNode).forEach(node -> node.setVisibleFigure(visible));
	}
	
	/**
	 * Change the visible mode of all SubWorkflowNodes in this ProjectEditor.
	 */
	public void setVisibleAboutAllSubWorkflowNode(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleSubWorkflow, visible);
		this.diagram.getNodeElementList().stream().filter(node -> node instanceof SubWorkflowNode).forEach(node -> node.setVisibleFigure(visible));
	}
	
	/**
	 * Change the visible mode of all TeamNodes in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllTeamNode(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleTeam, visible);
		this.diagram.getNodeElementList().stream().filter(node -> node instanceof TeamNode).forEach(node -> node.setVisibleFigure(visible));
	}
	
	/**
	 * Change the visible mode of all ComponentNodes in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllComponentNode(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleComponent, visible);
		this.diagram.getNodeElementList().stream().filter(node -> node instanceof ComponentNode).forEach(node -> node.setVisibleFigure(visible));
	}
	
	/**
	 * Change the visible mode of all AllocationLinks in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllAllocationLink(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleAllocation, visible);
		this.diagram.getLinkList().stream().filter(link -> link instanceof AllocationLink).forEach(link -> link.setVisible(visible));
	}
	
	/**
	 * Change the visible mode of all TaskDependencyLink in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllTaskDependencyLink(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleDependency, visible);
		this.diagram.getLinkList().stream().filter(link -> link instanceof TaskDependencyLink).forEach(link -> link.setVisible(visible));
	}
	
	/**
	 * Change the visible mode of all TeamLinks in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllTeamLink(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleHierarchy, visible);
		this.diagram.getLinkList().stream().filter(link -> link instanceof TeamLink).forEach(link -> link.setVisible(visible));
	}
	
	/**
	 * Change the visible mode of all ComponentHierarchyLink in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllComponentHierarchyLink(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleComponentHierarchy, visible);
		this.diagram.getLinkList().stream().filter(link -> link instanceof ComponentHierarchyLink).forEach(link -> link.setVisible(visible));
	}
	
	/**
	 * Change the visible mode of all TargetComponentLink in this ProjectEditor.
	 * @param visible
	 */
	public void setVisibleAboutAllTargetComponentLink(boolean visible){
		visibleMap.put(ProjectEditorConstVariables.visibleComponentToTask, visible);
		this.diagram.getLinkList().stream().filter(link -> link instanceof TargetComponentLink).forEach(link -> link.setVisible(visible));
	}
}
