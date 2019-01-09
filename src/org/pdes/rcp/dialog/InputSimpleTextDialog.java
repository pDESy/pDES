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
package org.pdes.rcp.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog class for editing one line text.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class InputSimpleTextDialog extends Dialog {
	private Text text;
	private String textString = "";
	
	private String title="";
	private String message="";
	
	/**
	 * This is the constructor.
	 * @param parentShell
	 */
	public InputSimpleTextDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * Set the title and message of this Dialog.
	 * @param title
	 * @param message
	 */
	public void setTitleAndMessage(String title,String message){
		this.title = title;
		this.message = message;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent){		
		Composite composite = (Composite)super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2,false));
		Label numberLabel = new Label(parent, SWT.SINGLE|SWT.BORDER);
		numberLabel.setText(message);
		numberLabel.setFont(new Font(null, "", 10, 0));
		GridData numberLabelGD = new GridData();
		numberLabelGD.horizontalAlignment = GridData.FILL;
		numberLabelGD.grabExcessHorizontalSpace=true;
		numberLabel.setLayoutData(numberLabelGD);
		
		text = new Text(parent, SWT.BORDER|SWT.SINGLE);
		text.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character == SWT.CR){
					ok(IDialogConstants.OK_ID);
				}
			}
			
		});
		text.setText(textString);
		
		GridData textGD = new GridData();
		textGD.horizontalAlignment = GridData.FILL;
		textGD.grabExcessHorizontalSpace=true;
		text.setLayoutData(textGD);
		return composite;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent,IDialogConstants.OK_ID,"OK", false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId){
		if(buttonId == IDialogConstants.OK_ID){
			ok(buttonId);
			super.buttonPressed(buttonId);
		}else if(buttonId == IDialogConstants.CLOSE_ID){
			setReturnCode(buttonId);
		}
	}
	
	/**
	 * "OK" operation.
	 * @param buttonId
	 */
	private void ok(int buttonId){
		this.textString = text.getText();
		setReturnCode(buttonId);
		close();
	}
	
	/**
	 * Get the string data from this dialog.
	 * @return textString
	 */
	public String getTextString(){
		return textString;
	}
	
	/**
	 * Set the string data from this dialog.
	 * @param textString
	 */
	public void setTextString(String textString) {
		this.textString = textString;
	}
}
