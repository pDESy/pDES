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
package org.pdes.rcp.view.parts;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.pdes.rcp.dialog.InputSimpleTextDialog;
import org.pdes.rcp.dialog.SelectSimpleDataDialog;
import org.pdes.rcp.model.ComponentNode;
import org.pdes.rcp.model.FacilityElement;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.rcp.model.SubWorkflowNode;
import org.pdes.rcp.model.TaskNode;
import org.pdes.rcp.model.TeamNode;
import org.pdes.rcp.model.WorkerElement;
import org.pdes.rcp.model.base.AbstractModel;
import org.pdes.rcp.model.base.Link;

/**
 * This is the ViewPart class for editing the attributes of clicked model in ProjectDiagram.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class SelectedModelViewPart extends ViewPart {
	
	private Composite view;
	private AbstractModel selectedModel;
	private List<String> allocatedTaskNameList;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//1. Define SWT of clicking TeamNode
	//If you add some attributes in TeamNode, add "setVisibleOfTeamSWT" method.
	private Label teamNameLabel;
	private Text teamNameText;
	@SuppressWarnings("unused")
	private Label workerTableLabel, facilityTableLabel;
	private Table workerTable, facilityTable;
	@SuppressWarnings("unused")
	private Button addWorkerButton, deleteWorkerButton, addFacilityButton, deleteFacilityButton;
	
	/**
	 * Set visible mode of each attributes of model.
	 * @param visible
	 */
	private void setVisibleOfTeamSWT(boolean visible){
		teamNameLabel.setVisible(visible);
		teamNameText.setVisible(visible);
		workerTableLabel.setVisible(visible);
//		facilityTableLabel.setVisible(visible);
//		facilityTable.setVisible(visible);
		workerTable.setVisible(visible);
		addWorkerButton.setVisible(visible);
		deleteWorkerButton.setVisible(visible);
//		addFacilityButton.setVisible(visible);
//		deleteFacilityButton.setVisible(visible);
		if(visible){
			teamNameText.setText(((TeamNode) selectedModel).getName());
			this.redrawAllTableForTeam();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//2. Define SWT of clicking TaskNode
	//If you add some attributes in TaskNode, add "setVisibleOfTaskSWT" method.
	@SuppressWarnings("unused")
	private Label taskNameLabel, taskWorkAmountLabel, taskAdditionalWorkAmountLabel, taskProgressLabel;
	@SuppressWarnings("unused")
	private Text taskNameText, taskWorkAmountText, taskAdditionalWorkAmountText, taskProgressText;
	@SuppressWarnings("unused")
	private Button taskNeedFacilityCheckBox;
	
	/**
	 * Set visible mode of each attributes of model.
	 * @param visible
	 */
	private void setVisibleOfTaskSWT(boolean visible){
		taskNameLabel.setVisible(visible);
		taskNameText.setVisible(visible);
		taskWorkAmountLabel.setVisible(visible);
		taskWorkAmountText.setVisible(visible);
		taskProgressLabel.setVisible(visible);
		taskProgressText.setVisible(visible);
//		taskAdditionalWorkAmountLabel.setVisible(visible);
//		taskAdditionalWorkAmountText.setVisible(visible);
//		taskNeedFacilityCheckBox.setVisible(visible);
		
		if(visible){
			taskNameText.setText(((TaskNode) selectedModel).getName());
			taskWorkAmountText.setText(String.valueOf(((TaskNode) selectedModel).getWorkAmount()));
			taskProgressText.setText(String.valueOf(((TaskNode) selectedModel).getProgress()));
//			taskAdditionalWorkAmountText.setText(String.valueOf(((TaskNode)selectedModel).getAdditionalWorkAmount()));
//			taskNeedFacilityCheckBox.setSelection(((TaskNode)selectedModel).isNeedFacility());
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//3. Define SWT of clicking ComponentNode
	//If you add some attributes in ComponentNode, add "setVisibleOfComponentSWT" method.
	@SuppressWarnings("unused")
	private Label componentNameLabel, componentErrorToleranceLabel;
	@SuppressWarnings("unused")
	private Text componentNameText, componentErrorToleranceText;
	
	/**
	 * Set visible mode of each attributes of model.
	 * @param visible
	 */
	private void setVisibleOfComponentSWT(boolean visible){
		componentNameLabel.setVisible(visible);
		componentNameText.setVisible(visible);
//		componentErrorToleranceLabel.setVisible(visible);
//		componentErrorToleranceText.setVisible(visible);
		
		if(visible){
			componentNameText.setText(((ComponentNode) selectedModel).getName());
//			componentErrorToleranceText.setText(String.valueOf(((ComponentNode) selectedModel).getErrorTolerance()));
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//4. Define SWT of clicking SubWorkflowNode
	//If you add some attributes in ComponentNode, add "setVisibleOfSubWorkflowSWT" method.
	private Label subWorkflowNameLabel, subWorkflowFilenameLabel;
	private Text subWorkflowNameText, subWorkflowFilenameText;
	
	/**
	 * Set visible mode of each attributes of model.
	 * @param visible
	 */
	private void setVisibleOfSubWorkflowSWT(boolean visible){
		subWorkflowNameLabel.setVisible(visible);
		subWorkflowNameText.setVisible(visible);
		subWorkflowFilenameLabel.setVisible(visible);
		subWorkflowFilenameText.setVisible(visible);
		
		if(visible){
			subWorkflowNameText.setText(((SubWorkflowNode) selectedModel).getName());
			subWorkflowFilenameText.setText(String.valueOf(((SubWorkflowNode) selectedModel).getFilename()));
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//5. Define SWT of clicking Link
	//If you add some attributes in TeamNode, add "setVisibleOfLinkSWT" method.
	private Label linkTypeNameLabel, linkTypeName;
	
	/**
	 * Set visible mode of each attributes of model.
	 * @param visible
	 */
	private void setVisibleOfLinkSWT(boolean visible){
		linkTypeNameLabel.setVisible(visible);
		linkTypeName.setVisible(visible);
		if(visible) {
			linkTypeName.setText(((Link)selectedModel).getLinkTypeName());
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//6. Define SWT of clicking Diagram
	//If you add some attributes in ProjectEditor, add "setVisibleOfDiagramSWT" method.
	private Label diagramSimulationConstraintsLabel, diagramConcurrencyLimitLabel;
	private Text diagramConcurrencyLimitText;
	
	/**
	 * Set visible mode of each attributes of model.
	 * @param visible
	 */
	private void setVisibleOfDiagramSWT(boolean visible){
		diagramSimulationConstraintsLabel.setVisible(visible);
		diagramConcurrencyLimitLabel.setVisible(visible);
		diagramConcurrencyLimitText.setVisible(visible);
		
		if(visible){
			diagramConcurrencyLimitText.setText(String.valueOf(((ProjectDiagram) selectedModel).getConcurrencyLimitOfWorkflow()));
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * This is the interface method from other class.<br>
	 * By using this method, this view can recognize which model is selected.<br>
	 * @param selectedModel
	 */
	public void readSelectedModel(AbstractModel selectedModel){
		this.selectedModel = selectedModel;
		this.setVisibleValueOfAllSWT();
		this.view.redraw();
	}
	
	
	/**
	 * Set visible mode of each SWT considering selected model.
	 */
	private void setVisibleValueOfAllSWT(){
		this.allocatedTaskNameList = null;
		if(selectedModel instanceof TeamNode){
			this.allocatedTaskNameList = ((TeamNode)selectedModel).getNameListOfAllocatedTasks();
			this.setVisibleOfTeamSWT(true);
			this.setVisibleOfTaskSWT(false);
			this.setVisibleOfLinkSWT(false);
			this.setVisibleOfComponentSWT(false);
			this.setVisibleOfSubWorkflowSWT(false);
			this.setVisibleOfDiagramSWT(false);
		}else if(selectedModel instanceof TaskNode){
			this.setVisibleOfTeamSWT(false);
			this.setVisibleOfTaskSWT(true);
			this.setVisibleOfLinkSWT(false);
			this.setVisibleOfComponentSWT(false);
			this.setVisibleOfSubWorkflowSWT(false);
			this.setVisibleOfDiagramSWT(false);
		}else if(selectedModel instanceof Link){
			this.setVisibleOfTeamSWT(false);
			this.setVisibleOfTaskSWT(false);
			this.setVisibleOfLinkSWT(true);
			this.setVisibleOfComponentSWT(false);
			this.setVisibleOfSubWorkflowSWT(false);
			this.setVisibleOfDiagramSWT(false);
		}else if(selectedModel instanceof ComponentNode){
			this.setVisibleOfTeamSWT(false);
			this.setVisibleOfTaskSWT(false);
			this.setVisibleOfLinkSWT(false);
			this.setVisibleOfComponentSWT(true);
			this.setVisibleOfSubWorkflowSWT(false);
			this.setVisibleOfDiagramSWT(false);
		}else if(selectedModel instanceof SubWorkflowNode){
			this.setVisibleOfTeamSWT(false);
			this.setVisibleOfTaskSWT(false);
			this.setVisibleOfLinkSWT(false);
			this.setVisibleOfComponentSWT(false);
			this.setVisibleOfSubWorkflowSWT(true);
			this.setVisibleOfDiagramSWT(false);
		}else if(selectedModel instanceof ProjectDiagram){
			this.setVisibleOfTeamSWT(false);
			this.setVisibleOfTaskSWT(false);
			this.setVisibleOfLinkSWT(false);
			this.setVisibleOfComponentSWT(false);
			this.setVisibleOfSubWorkflowSWT(false);
			this.setVisibleOfDiagramSWT(true);
		}else{
			this.setVisibleOfTeamSWT(false);
			this.setVisibleOfTaskSWT(false);
			this.setVisibleOfLinkSWT(false);
			this.setVisibleOfComponentSWT(false);
			this.setVisibleOfSubWorkflowSWT(false);
			this.setVisibleOfDiagramSWT(false);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.view = parent;
		parent.setLayout(new FormLayout());
		
		///////////////////////TeamNode////////////////////////////
		teamNameLabel = new Label(parent, SWT.NULL);
		teamNameLabel.setText("Name : ");
		teamNameLabel.setFont(new Font(null, "", 10, 0));
		FormData teamNameLabelFD = new FormData();
		teamNameLabelFD.top= new FormAttachment(0,10);
		teamNameLabelFD.left= new FormAttachment(0,10);
		teamNameLabel.setLayoutData(teamNameLabelFD);
		
		teamNameText = new Text(parent, SWT.BORDER|SWT.SINGLE);
		teamNameText.addKeyListener(new KeyListener(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {}
			
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = teamNameText.getText();
					((TeamNode)selectedModel).setName(textString);
				}
			}
		});
		FormData teamNameTextFD = new FormData();
		teamNameTextFD.top= new FormAttachment(0,10);
		teamNameTextFD.left= new FormAttachment(teamNameLabel,10);
		teamNameTextFD.right = new FormAttachment(95);
		teamNameText.setLayoutData(teamNameTextFD);
		
		workerTableLabel = new Label(parent, SWT.NULL);
		workerTableLabel.setText("[Workers]\n[skill_mean: progress[person-day]]/[skill_s.d.: progress[person-day]]/[error probability of component]");
		workerTableLabel.setFont(new Font(null, "", 10, 0));
		FormData workerTableLabelFD = new FormData();
		workerTableLabelFD.top= new FormAttachment(teamNameLabel,12);
		workerTableLabelFD.left= new FormAttachment(0,10);
		workerTableLabel.setLayoutData(workerTableLabelFD);
		
		workerTable = new Table(parent, SWT.MULTI|SWT.BORDER|SWT.FULL_SELECTION);
		FormData workerTableFD = new FormData();
		workerTableFD.top= new FormAttachment(workerTableLabel,10);
		workerTableFD.left = new FormAttachment(0,20);
		workerTableFD.bottom= new FormAttachment(55);
		workerTableFD.right = new FormAttachment(95);
		workerTable.setLayoutData(workerTableFD);
		workerTable.setLinesVisible(true);
		workerTable.setHeaderVisible(true);
		workerTable.setEnabled(true);
		final TableEditor workerTableEditor = new TableEditor(workerTable);
		workerTableEditor.grabHorizontal = true;
		workerTable.addMouseListener(new MouseAdapter(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDoubleClick(MouseEvent e){
				int index = workerTable.getSelectionIndex();
				if(index==-1) return;
				
				WorkerElement worker = ((TeamNode)selectedModel).getWorkerList().get(index);
				workerTable.setSelection(new int[0]);
				TableItem item = workerTable.getItem(index);
				Point point = new Point(e.x,e.y);
				for(int i=0;i < workerTable.getColumnCount();i++){
					if(item.getBounds(i).contains(point)){
						final int column = i;
						final Text text = new Text(workerTable, SWT.NONE);
						text.setText(item.getText(i));
						text.addFocusListener(new FocusListener(){
							@Override
							public void focusLost(FocusEvent e){
								text.dispose();
							}
							@Override
							public void focusGained(FocusEvent e) {}
						});
						
						text.addKeyListener(new KeyListener(){
							@Override
							public void keyPressed(KeyEvent e) {
								if(e.character==SWT.CR){
									if(column==0){ // name
										worker.setName(text.getText());
									}else if(column==1 && doubleCheck(text.getText())){ // cost
										if(Double.valueOf(text.getText()) < 0.00) return;
										worker.setCost(Double.valueOf(text.getText()));
									}else{ // skill ( <work amount skill value>/<quality skill value> )
										String[] skillTexts = text.getText().split("/");
										if (skillTexts.length != 3) return;
										String workAmountSkill_mean_Text = skillTexts[0];
										String workAmountSkill_sd_Text = skillTexts[1];
										String qualitySkillText = skillTexts[2];
										if (!(doubleCheck(workAmountSkill_mean_Text) && doubleCheck(workAmountSkill_sd_Text) && doubleCheck(qualitySkillText))) return;
										if( Double.valueOf(workAmountSkill_mean_Text) < 0.00 || Double.valueOf(workAmountSkill_sd_Text) < 0.00 || Double.valueOf(qualitySkillText) < 0.00 ) return;
										worker.addSkillInWorkAmountSkillMap(allocatedTaskNameList.get(column-2), new Double[] {Double.valueOf(workAmountSkill_mean_Text), Double.valueOf(workAmountSkill_sd_Text)});
										worker.addSkillInQualitySkillMap(allocatedTaskNameList.get(column-2), Double.valueOf(qualitySkillText));
									}
									redrawAllTableForTeam();
									text.dispose();
								} else if(e.keyCode==SWT.ESC){
									text.dispose();
								}
							}
							@Override
							public void keyReleased(KeyEvent e) {}
						});
						workerTableEditor.setEditor (text, item, i);
						text.setFocus();
						text.selectAll();
					}
				}
			}
		});
		
		addWorkerButton = new Button(parent,SWT.PUSH);
		addWorkerButton.setText("ADD");
		addWorkerButton.setEnabled(true);
		addWorkerButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				InputSimpleTextDialog dialog = new InputSimpleTextDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				dialog.setTitleAndMessage("Add worker", "Please input the name of new worker.");
				if(dialog.open()==0){
					String name = dialog.getTextString();
					WorkerElement member = new WorkerElement();
					member.setName(name);
					member.setCost(0);
					((TeamNode) selectedModel).addWorker(member);
					redrawAllTableForTeam();
				}
			}
		});
		FormData addWorkerButtonFD = new FormData();
		addWorkerButtonFD.top= new FormAttachment(teamNameLabel,9);
		addWorkerButtonFD.left= new FormAttachment(workerTableLabel,10);
		addWorkerButton.setLayoutData(addWorkerButtonFD);
		
		deleteWorkerButton = new Button(parent,SWT.PUSH);
		deleteWorkerButton.setText("DELETE");
		deleteWorkerButton.setEnabled(true);
		deleteWorkerButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(((TeamNode) selectedModel).getWorkerList().size()==0) return;
				List<String> memberNameList = ((TeamNode) selectedModel).getWorkerNameList();
				SelectSimpleDataDialog dialog = new SelectSimpleDataDialog((PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()));
				dialog.setTitleAndMessage("Delete worker", "Please select deleting workers");
				dialog.setItemList(memberNameList);
				if(dialog.open()==0){
					List<Integer> selectedItemNumber = dialog.getSelectedItemNumber();
					for(int i=0;i<selectedItemNumber.size();i++){
						((TeamNode) selectedModel).deleteWorker(selectedItemNumber.get(i));
					}
					redrawAllTableForTeam();
				}
			}
		});
		FormData deleteWorkerButtonFD = new FormData();
		deleteWorkerButtonFD.top= new FormAttachment(teamNameLabel,9);
		deleteWorkerButtonFD.left= new FormAttachment(addWorkerButton,10);
		deleteWorkerButton.setLayoutData(deleteWorkerButtonFD);
		
		
//		facilityTableLabel = new Label(parent, SWT.NULL);
//		facilityTableLabel.setText("[Facilities]\nskill: work amount[parson-day]/error probability");
//		facilityTableLabel.setFont(new Font(null, "", 10, 0));
//		FormData facilityTableLabelFD = new FormData();
//		facilityTableLabelFD.top= new FormAttachment(workerTable,20);
//		facilityTableLabelFD.left= new FormAttachment(0,10);
//		facilityTableLabel.setLayoutData(facilityTableLabelFD);
//		
//		facilityTable = new Table(parent, SWT.MULTI|SWT.BORDER|SWT.FULL_SELECTION);
//		FormData facilityTableFD = new FormData();
//		facilityTableFD.top= new FormAttachment(facilityTableLabel,10);
//		facilityTableFD.left = new FormAttachment(0,20);
//		facilityTableFD.bottom= new FormAttachment(98);
//		facilityTableFD.right = new FormAttachment(95);
//		facilityTable.setLayoutData(facilityTableFD);
//		facilityTable.setLinesVisible(true);
//		facilityTable.setHeaderVisible(true);
//		facilityTable.setEnabled(true);
//		final TableEditor facilityTableEditor = new TableEditor(facilityTable);
//		facilityTableEditor.grabHorizontal = true;
//		facilityTable.addMouseListener(new MouseAdapter(){
//			/*
//			 * (non-Javadoc)
//			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
//			 */
//			public void mouseDoubleClick(MouseEvent e){
//				int index = facilityTable.getSelectionIndex();
//				if(index==-1) return;
//				
//				FacilityElement facility = ((TeamNode)selectedModel).getFacilityList().get(index);
//				facilityTable.setSelection(new int[0]);
//				TableItem item = facilityTable.getItem(index);
//				Point point = new Point(e.x,e.y);
//				for(int i=0;i < facilityTable.getColumnCount();i++){
//					if(item.getBounds(i).contains(point)){
//						final int column = i;
//						final Text text = new Text(facilityTable, SWT.NONE);
//						text.setText(item.getText(i));
//						text.addFocusListener(new FocusListener(){
//							@Override
//							public void focusLost(FocusEvent e){
//								text.dispose();
//							}
//							@Override
//							public void focusGained(FocusEvent e) {}
//						});
//						
//						text.addKeyListener(new KeyListener(){
//
//							@Override
//							public void keyPressed(KeyEvent e) {
//								if(e.character==SWT.CR){
//									if(column==0){ // name
//										facility.setName(text.getText());
//									}else if(column==1 && doubleCheck(text.getText())){ // cost
//										if(Double.valueOf(text.getText()) < 0.00) return;
//										facility.setCost(Double.valueOf(text.getText()));
//									}else{ // skill ( <work amount skill value>/<quality skill value> )
//										String[] skillTexts = text.getText().split("/");
//										if (skillTexts.length != 2) return;
//										String workAmountSkillText = skillTexts[0];
//										String qualitySkillText = skillTexts[1];
//										if (!(doubleCheck(workAmountSkillText) && doubleCheck(qualitySkillText))) return;
//										if((Double.valueOf(workAmountSkillText) < 0.00) || (Double.valueOf(qualitySkillText) < 0.00)) return;
//										facility.addSkillInWorkAmountSkillMap(allocatedTaskNameList.get(column-2), Double.valueOf(workAmountSkillText));
//										facility.addSkillInQualitySkillMap(allocatedTaskNameList.get(column-2), Double.valueOf(qualitySkillText));
//									}
//									redrawAllTableForTeam();
//									text.dispose();
//								} else if(e.keyCode==SWT.ESC){
//									text.dispose();
//								}
//							}
//							
//							@Override
//							public void keyReleased(KeyEvent e) {}
//						});
//						facilityTableEditor.setEditor (text, item, i);
//						text.setFocus();
//						text.selectAll();
//					}
//				}
//			}
//		});
//		
//		addFacilityButton = new Button(parent,SWT.PUSH);
//		addFacilityButton.setText("ADD");
//		addFacilityButton.setEnabled(true);
//		addFacilityButton.addSelectionListener(new SelectionAdapter(){
//			public void widgetSelected(SelectionEvent e){
//				InputSimpleTextDialog dialog = new InputSimpleTextDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//				dialog.setTitleAndMessage("Add facility", "Please input the name of new facility");
//				if(dialog.open()==0){
//					String name = dialog.getTextString();
//					FacilityElement member = new FacilityElement();
//					member.setName(name);
//					member.setCost(0);
//					((TeamNode) selectedModel).addFacility(member);
//					redrawAllTableForTeam();
//				}
//			}
//		});
//		FormData addFacilityButtonFD = new FormData();
//		addFacilityButtonFD.top= new FormAttachment(workerTable,15);
//		addFacilityButtonFD.left= new FormAttachment(facilityTableLabel,10);
//		addFacilityButton.setLayoutData(addFacilityButtonFD);
//		
//		deleteFacilityButton = new Button(parent,SWT.PUSH);
//		deleteFacilityButton.setText("DELETE");
//		deleteFacilityButton.setEnabled(true);
//		deleteFacilityButton.addSelectionListener(new SelectionAdapter(){
//			public void widgetSelected(SelectionEvent e){
//				if(((TeamNode) selectedModel).getFacilityList().size()==0) return;
//				List<String> memberNameList = ((TeamNode) selectedModel).getFacilityNameList();
//				SelectSimpleDataDialog dialog = new SelectSimpleDataDialog((PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()));
//				dialog.setTitleAndMessage("Delete facility", "Please select deleting facilities");
//				dialog.setItemList(memberNameList);
//				if(dialog.open()==0){
//					List<Integer> selectedItemNumber = dialog.getSelectedItemNumber();
//					for(int i=0;i<selectedItemNumber.size();i++){
//						((TeamNode) selectedModel).deleteFacility(selectedItemNumber.get(i));
//					}
//					redrawAllTableForTeam();
//				}
//			}
//		});
//		FormData deleteFacilityButtonFD = new FormData();
//		deleteFacilityButtonFD.top= new FormAttachment(workerTable,15);
//		deleteFacilityButtonFD.left= new FormAttachment(addFacilityButton,10);
//		deleteFacilityButton.setLayoutData(deleteFacilityButtonFD);
		
		///////////////////////////////////////////////////////////////////////////
		
		
		///////////////////////Task////////////////////////////
		taskNameLabel = new Label(parent, SWT.NULL);
		taskNameLabel.setText("Name : ");
		taskNameLabel.setFont(new Font(null, "", 10, 0));
		FormData taskNameLabelFD = new FormData();
		taskNameLabelFD.top= new FormAttachment(0,10);
		taskNameLabelFD.left= new FormAttachment(0,10);
		taskNameLabel.setLayoutData(taskNameLabelFD);
		
		taskNameText = new Text(parent, SWT.BORDER|SWT.SINGLE);
		taskNameText.addKeyListener(new KeyListener(){

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {}

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = taskNameText.getText();
					((TaskNode)selectedModel).setName(textString);
				}
			}
		});
		FormData taskNameTextFD = new FormData();
		taskNameTextFD.top= new FormAttachment(0,10);
		taskNameTextFD.left= new FormAttachment(taskNameLabel,10);
		taskNameTextFD.right = new FormAttachment(95);
		taskNameText.setLayoutData(taskNameTextFD);
		
		taskWorkAmountLabel = new Label(parent, SWT.NULL);
		taskWorkAmountLabel.setText("Default workload : ");
		taskWorkAmountLabel.setFont(new Font(null, "", 10, 0));
		FormData taskWorkAmountLabelFD = new FormData();
		taskWorkAmountLabelFD.top= new FormAttachment(taskNameLabel,10);
		taskWorkAmountLabelFD.left= new FormAttachment(0,10);
		taskWorkAmountLabel.setLayoutData(taskWorkAmountLabelFD);
		
		taskWorkAmountText = new Text(parent, SWT.BORDER|SWT.SINGLE);
		taskWorkAmountText.addKeyListener(new KeyListener(){

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {}

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = taskWorkAmountText.getText();
					if(doubleCheck(textString)) {
						((TaskNode)selectedModel).setWorkAmount(Double.parseDouble(textString));
					}else{
						taskWorkAmountText.setText(String.valueOf(((TaskNode)selectedModel).getWorkAmount()));
					}
				}
			}
		});
		FormData taskWorkAmountTextFD = new FormData();
		taskWorkAmountTextFD.top= new FormAttachment(taskNameLabel,10);
		taskWorkAmountTextFD.left= new FormAttachment(taskWorkAmountLabel,10);
		taskWorkAmountTextFD.width = 50;
		taskWorkAmountText.setLayoutData(taskWorkAmountTextFD);
		
        taskProgressLabel = new Label(parent, SWT.NULL);
        taskProgressLabel.setText("Progress rate (0.0 - 1.0) : ");
        taskProgressLabel.setFont(new Font(null, "", 10, 0));
        FormData taskProgressLabelFD = new FormData();
        taskProgressLabelFD.top= new FormAttachment(taskWorkAmountLabel,10);
        taskProgressLabelFD.left= new FormAttachment(0,10);
        taskProgressLabel.setLayoutData(taskProgressLabelFD);
        
        taskProgressText = new Text(parent, SWT.BORDER|SWT.SINGLE);
        taskProgressText.addKeyListener(new KeyListener(){

            /*
             * (non-Javadoc)
             * @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
             */
            @Override
            public void keyPressed(KeyEvent e) {}

            /*
             * (non-Javadoc)
             * @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
             */
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.character==SWT.CR){
                    String textString = taskProgressText.getText();
                    if(doubleCheck(textString)) {
                        ((TaskNode)selectedModel).setProgress(Double.parseDouble(textString));
                    }else{
                        taskProgressText.setText(String.valueOf(((TaskNode)selectedModel).getProgress()));
                    }
                }
            }
        });
        FormData taskProgressTextFD = new FormData();
        taskProgressTextFD.top= new FormAttachment(taskWorkAmountLabel,10);
        taskProgressTextFD.left= new FormAttachment(taskProgressLabel,10);
        taskProgressTextFD.width = 50;
        taskProgressText.setLayoutData(taskProgressTextFD);		
		
//		taskAdditionalWorkAmountLabel = new Label(parent, SWT.NULL);
//		taskAdditionalWorkAmountLabel.setText("Additional Work Amount : ");
//		taskAdditionalWorkAmountLabel.setFont(new Font(null, "", 10, 0));
//		FormData taskAdditionalWorkAmountLabelFD = new FormData();
//		taskAdditionalWorkAmountLabelFD.top= new FormAttachment(taskWorkAmountLabel,10);
//		taskAdditionalWorkAmountLabelFD.left= new FormAttachment(0,10);
//		taskAdditionalWorkAmountLabel.setLayoutData(taskAdditionalWorkAmountLabelFD);
//		
//		taskAdditionalWorkAmountText = new Text(parent, SWT.BORDER|SWT.SINGLE);
//		taskAdditionalWorkAmountText.addKeyListener(new KeyListener(){
//
//			/*
//			 * (non-Javadoc)
//			 * @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
//			 */
//			@Override
//			public void keyPressed(KeyEvent e) {}
//
//			/*
//			 * (non-Javadoc)
//			 * @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
//			 */
//			@Override
//			public void keyReleased(KeyEvent e) {
//				if(e.character==SWT.CR){
//					String textString = taskAdditionalWorkAmountText.getText();
//					if(intCheck(textString)) {
//						((TaskNode)selectedModel).setAdditionalWorkAmount(Integer.parseInt(textString));
//					}else{
//						taskAdditionalWorkAmountText.setText(String.valueOf(((TaskNode)selectedModel).getAdditionalWorkAmount()));
//					}
//				}
//			}
//		});
//		FormData taskAdditionalWorkAmountTextFD = new FormData();
//		taskAdditionalWorkAmountTextFD.top= new FormAttachment(taskWorkAmountLabel,10);
//		taskAdditionalWorkAmountTextFD.left= new FormAttachment(taskAdditionalWorkAmountLabel,10);
//		taskAdditionalWorkAmountTextFD.width = 50;
//		taskAdditionalWorkAmountText.setLayoutData(taskAdditionalWorkAmountTextFD);
//
//		taskNeedFacilityCheckBox = new Button(parent, SWT.CHECK);
//		taskNeedFacilityCheckBox.setText("Need Facility");
//		taskNeedFacilityCheckBox.setFont(new Font(null, "", 10, 0));
//		taskNeedFacilityCheckBox.addSelectionListener(new SelectionListener() {
//			/*
//			 * (non-Javadoc)
//			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
//			 */
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) { }
//			
//			/*
//			 * (non-Javadoc)
//			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
//			 */
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				Button checkBox = (Button)e.widget;
//				((TaskNode)selectedModel).setNeedFacility(checkBox.getSelection());
//			}
//		});
//		FormData taskNeedFacilityCheckBoxFD = new FormData();
//		taskNeedFacilityCheckBoxFD.top = new FormAttachment(taskAdditionalWorkAmountLabel, 10);
//		taskNeedFacilityCheckBoxFD.left = new FormAttachment(0, 10);
//		taskNeedFacilityCheckBox.setLayoutData(taskNeedFacilityCheckBoxFD);
		///////////////////////////////////////////////////////////////////////////
		
		///////////////////////Link////////////////////////////
		linkTypeNameLabel = new Label(parent, SWT.NULL);
		linkTypeNameLabel.setText("LinkType : ");
		linkTypeNameLabel.setFont(new Font(null, "", 10, 0));
		FormData linkTypeNameLabelFD = new FormData();
		linkTypeNameLabelFD.top= new FormAttachment(0,10);
		linkTypeNameLabelFD.left= new FormAttachment(0,10);
		linkTypeNameLabel.setLayoutData(linkTypeNameLabelFD);
		
		linkTypeName = new Label(parent, SWT.NULL);
		linkTypeName.setText("--------------------------------");
		linkTypeName.setFont(new Font(null, "", 10, 0));
		FormData linkTypeNameFD = new FormData();
		linkTypeNameFD.top= new FormAttachment(0,10);
		linkTypeNameFD.left= new FormAttachment(linkTypeNameLabel,10);
		linkTypeName.setLayoutData(linkTypeNameFD);
		//////////////////////////////////////////////////////////////////////////
		
		///////////////////////Component////////////////////////////
		componentNameLabel = new Label(parent, SWT.NULL);
		componentNameLabel.setText("Name : ");
		componentNameLabel.setFont(new Font(null, "", 10, 0));
		FormData componentNameLabelFD = new FormData();
		componentNameLabelFD.top= new FormAttachment(0,10);
		componentNameLabelFD.left= new FormAttachment(0,10);
		componentNameLabel.setLayoutData(componentNameLabelFD);
		
		componentNameText = new Text(parent, SWT.BORDER|SWT.SINGLE);
		componentNameText.addKeyListener(new KeyListener(){
		
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			*/
			@Override
			public void keyPressed(KeyEvent e) {}
			
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			*/
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = componentNameText.getText();
					((ComponentNode)selectedModel).setName(textString);
				}
			}
		});
		FormData componentNameTextFD = new FormData();
		componentNameTextFD.top= new FormAttachment(0,10);
		componentNameTextFD.left= new FormAttachment(componentNameLabel,10);
		componentNameTextFD.right = new FormAttachment(95);
		componentNameText.setLayoutData(componentNameTextFD);
		
//		componentErrorToleranceLabel = new Label(parent, SWT.NULL);
//		componentErrorToleranceLabel.setText("Error Tolerance : ");
//		componentErrorToleranceLabel.setFont(new Font(null, "", 10, 0));
//		FormData componentErrorToleranceLabelFD = new FormData();
//		componentErrorToleranceLabelFD.top= new FormAttachment(componentNameLabel,10);
//		componentErrorToleranceLabelFD.left= new FormAttachment(0,10);
//		componentErrorToleranceLabel.setLayoutData(componentErrorToleranceLabelFD);
//		
//		componentErrorToleranceText = new Text(parent, SWT.BORDER|SWT.SINGLE);
//		componentErrorToleranceText.addKeyListener(new KeyListener(){
//		
//			/*
//			* (non-Javadoc)
//			* @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
//			*/
//			public void keyPressed(KeyEvent e) {}
//			
//			/*
//			* (non-Javadoc)
//			* @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
//			*/
//			public void keyReleased(KeyEvent e) {
//				if(e.character==SWT.CR){
//					String textString = componentErrorToleranceText.getText();
//					if(doubleCheck(textString)) {
//						((ComponentNode)selectedModel).setErrorTolerance(Double.parseDouble(textString));
//					}else{
//						componentErrorToleranceText.setText(String.valueOf(((ComponentNode)selectedModel).getErrorTolerance()));
//					}
//				}
//			}
//		});
//		FormData componentErrorToleranceTextFD = new FormData();
//		componentErrorToleranceTextFD.top= new FormAttachment(componentNameLabel,10);
//		componentErrorToleranceTextFD.left= new FormAttachment(componentErrorToleranceLabel,10);
//		componentErrorToleranceTextFD.width = 50;
//		componentErrorToleranceText.setLayoutData(componentErrorToleranceTextFD);
		///////////////////////////////////////////////////////////////////////////
		
		///////////////////////SubWorkflow////////////////////////////
		subWorkflowNameLabel = new Label(parent, SWT.NULL);
		subWorkflowNameLabel.setText("Name : ");
		subWorkflowNameLabel.setFont(new Font(null, "", 10, 0));
		FormData subWorkflowNameLabelFD = new FormData();
		subWorkflowNameLabelFD.top = new FormAttachment(0,10);
		subWorkflowNameLabelFD.left = new FormAttachment(0,10);
		subWorkflowNameLabel.setLayoutData(subWorkflowNameLabelFD);
		
		subWorkflowNameText = new Text(parent, SWT.BORDER | SWT.SINGLE);
		subWorkflowNameText.addKeyListener(new KeyListener() {
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			*/
			@Override
			public void keyPressed(KeyEvent e) {}
			
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			*/
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = subWorkflowNameText.getText();
					((SubWorkflowNode)selectedModel).setName(textString);
				}
			}
		});
		
		FormData subWorkflowNameTextFD = new FormData();
		subWorkflowNameTextFD.top = new FormAttachment(0,10);
		subWorkflowNameTextFD.left = new FormAttachment(subWorkflowNameLabel,10);
		subWorkflowNameTextFD.right = new FormAttachment(95);
		subWorkflowNameText.setLayoutData(subWorkflowNameTextFD);
		
		subWorkflowFilenameLabel = new Label(parent, SWT.NULL);
		subWorkflowFilenameLabel.setText("Filename : ");
		subWorkflowFilenameLabel.setFont(new Font(null, "", 10, 0));
		FormData subWorkflowFilenameLabelFD = new FormData();
		subWorkflowFilenameLabelFD.top = new FormAttachment(subWorkflowNameLabel,10);
		subWorkflowFilenameLabelFD.left = new FormAttachment(0,10);
		subWorkflowFilenameLabel.setLayoutData(subWorkflowFilenameLabelFD);
		
		subWorkflowFilenameText = new Text(parent, SWT.BORDER|SWT.SINGLE);
		subWorkflowFilenameText.addKeyListener(new KeyListener() {
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			*/
			@Override
			public void keyPressed(KeyEvent e) {}
			
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			*/
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = subWorkflowFilenameText.getText();
					((SubWorkflowNode)selectedModel).setFilename(textString);
				}
			}
		});
		
		FormData subWorkflowFilenameTextFD = new FormData();
		subWorkflowFilenameTextFD.top = new FormAttachment(subWorkflowNameLabel,10);
		subWorkflowFilenameTextFD.left = new FormAttachment(subWorkflowFilenameLabel,10);
		subWorkflowFilenameTextFD.right = new FormAttachment(95);
		subWorkflowFilenameText.setLayoutData(subWorkflowFilenameTextFD);
		///////////////////////////////////////////////////////////////////////////
		
		///////////////////////Diagram////////////////////////////
		diagramSimulationConstraintsLabel = new Label(parent, SWT.NULL);
		diagramSimulationConstraintsLabel.setText("[Simulation Constraints]");
		diagramSimulationConstraintsLabel.setFont(new Font(null, "", 10, 0));
		FormData diagramSimulationConstraintsLabelFD = new FormData();
		diagramSimulationConstraintsLabelFD.top = new FormAttachment(0,10);
		diagramSimulationConstraintsLabelFD.left = new FormAttachment(0,10);
		diagramSimulationConstraintsLabel.setLayoutData(diagramSimulationConstraintsLabelFD);
		
		diagramConcurrencyLimitLabel = new Label(parent, SWT.NULL);
		diagramConcurrencyLimitLabel.setText("Concurrency Limit of Workflow: ");
		diagramConcurrencyLimitLabel.setFont(new Font(null, "", 10, 0));
		FormData diagramConcurrencyLimitLabelFD = new FormData();
		diagramConcurrencyLimitLabelFD.top = new FormAttachment(diagramSimulationConstraintsLabel,10);
		diagramConcurrencyLimitLabelFD.left = new FormAttachment(5,10);
		diagramConcurrencyLimitLabel.setLayoutData(diagramConcurrencyLimitLabelFD);
		
		diagramConcurrencyLimitText = new Text(parent, SWT.BORDER|SWT.SINGLE);
		diagramConcurrencyLimitText.addKeyListener(new KeyListener() {
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyPressed(org.eclipse.draw2d.KeyEvent)
			*/
			public void keyPressed(KeyEvent e) {}
			
			/*
			* (non-Javadoc)
			* @see org.eclipse.draw2d.KeyListener#keyReleased(org.eclipse.draw2d.KeyEvent)
			*/
			public void keyReleased(KeyEvent e) {
				if(e.character==SWT.CR){
					String textString = diagramConcurrencyLimitText.getText();
					if(intCheck(textString)) {
						((ProjectDiagram)selectedModel).setConcurrencyLimitOfWorkflow(Integer.parseInt(textString));
					}else{
						diagramConcurrencyLimitText.setText(String.valueOf(((ProjectDiagram)selectedModel).getConcurrencyLimitOfWorkflow()));
					}
				}
			}
		});
		FormData diagramConcurrencyLimitTextFD = new FormData();
		diagramConcurrencyLimitTextFD.top = new FormAttachment(diagramSimulationConstraintsLabel,10);
		diagramConcurrencyLimitTextFD.left = new FormAttachment(diagramConcurrencyLimitLabel,10);
		diagramConcurrencyLimitTextFD.width = 50;
		diagramConcurrencyLimitText.setLayoutData(diagramConcurrencyLimitTextFD);
		///////////////////////////////////////////////////////////////////////////
		
		this.setVisibleValueOfAllSWT();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {}

	/**
	 * Check whether "str" can be transferred to double or not.<br>
	 * @param str
	 * @return
	 */
	private boolean doubleCheck(String str){
		try {
			Double.parseDouble(str);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * Check whether "str" can be transferred to int or not.<br>
	 * @param str
	 * @return
	 */
	private boolean intCheck(String str){
		try {
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	
	/**
	 * Refresh and redraw all table for TeamNode.
	 * @param editSection
	 */
	public void redrawAllTableForTeam(){
		redrawWorkerTable();
		//redrawFacilityTable();
	}
	
	/**
	 * Redraw workerTable
	 */
	private void redrawWorkerTable(){
		//Initialize
		while ( workerTable.getColumnCount() > 0 ) {
			workerTable.getColumns()[ 0 ].dispose();
		}
		while ( workerTable.getItemCount() > 0 ) {
			workerTable.getItems()[ 0 ].dispose();
		}
		
		//Column setting
		List<String> teamSkillNameList = ((TeamNode) selectedModel).getNameListOfAllocatedTasks();
		TableColumn column = new TableColumn(workerTable,SWT.NONE);
		column.setText("Name");
		column.setWidth(60);
		
		column = new TableColumn(workerTable,SWT.NONE);
		column.setText("Cost");
		column.setWidth(60);
		
		for (int i=0;i<teamSkillNameList.size();i++) {
			column = new TableColumn(workerTable,SWT.NONE);
			column.setText(teamSkillNameList.get(i));
			column.setWidth(60);
		}
		
		//item setting
		List<WorkerElement> memberList = ((TeamNode) selectedModel).getWorkerList();
		for(int i=0;i<memberList.size();i++){
			TableItem item = new TableItem(workerTable,SWT.NONE);
			WorkerElement member = memberList.get(i);
			item.setText(0, member.getName());
			item.setText(1, String.valueOf(member.getCost()));
			for(int j=0;j<teamSkillNameList.size();j++){
				String text = "";
				String delimiter = "/";
				
				// skill of work amount (mean)
				if(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j)) != null){
					text = String.valueOf(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j))[0]) + delimiter;
				}else{
					text = "0.0" + delimiter;
				}
				
				// skill of work amount (standard deviation)
				if(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j)) != null){
					text = text + String.valueOf(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j))[1]) + delimiter;
				}else{
					text = text + "0.0" + delimiter;
				}
				
				// skill of quality
				if(member.getQualitySkillMap().get(teamSkillNameList.get(j)) != null){
					text = text + String.valueOf(member.getQualitySkillMap().get(teamSkillNameList.get(j)));
				}else{
					text = text + "0.0";
				}
				item.setText(j+2, text);
			}
		}
	}
	
	/**
	 * Redraw facilityTable
	 */
	@SuppressWarnings("unused")
	private void redrawFacilityTable(){
		//Initialize
		while ( facilityTable.getColumnCount() > 0 ) {
			facilityTable.getColumns()[ 0 ].dispose();
		}
		while ( facilityTable.getItemCount() > 0 ) {
			facilityTable.getItems()[ 0 ].dispose();
		}
		
		//Column setting
		List<String> teamSkillNameList = ((TeamNode) selectedModel).getNameListOfAllocatedTasks();
		TableColumn column = new TableColumn(facilityTable,SWT.NONE);
		column.setText("Name");
		column.setWidth(60);
		
		column = new TableColumn(facilityTable,SWT.NONE);
		column.setText("Cost");
		column.setWidth(60);
		
		for (int i=0;i<teamSkillNameList.size();i++) {
			column = new TableColumn(facilityTable,SWT.NONE);
			column.setText(teamSkillNameList.get(i));
			column.setWidth(60);
		}
		
		//item setting
		List<FacilityElement> memberList = ((TeamNode) selectedModel).getFacilityList();
		for(int i=0;i<memberList.size();i++){
			TableItem item = new TableItem(facilityTable,SWT.NONE);
			FacilityElement member = memberList.get(i);
			item.setText(0, member.getName());
			item.setText(1, String.valueOf(member.getCost()));
			for(int j=0;j<teamSkillNameList.size();j++){
				String text = "/";
				
				// skill of work amount
				if(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j)) != null){
					text = String.valueOf(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j))) + text;
				}else{
					text = "0.0" + text;
				}
				
				// skill of work amount (standard deviation)
				if(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j)) != null){
					text = String.valueOf(member.getWorkAmountSkillMap().get(teamSkillNameList.get(j))[1]) + text;
				}else{
					text = "0.0" + text;
				}
				
				// skill of quality
				if(member.getQualitySkillMap().get(teamSkillNameList.get(j)) != null){
					text = text + String.valueOf(member.getQualitySkillMap().get(teamSkillNameList.get(j)));
				}else{
					text = text + "0.0";
				}
				item.setText(j+2, text);
			}
		}
	}
}
