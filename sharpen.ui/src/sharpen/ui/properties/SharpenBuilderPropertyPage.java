/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

package sharpen.ui.properties;

import sharpen.core.*;
import sharpen.ui.dialogs.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;

import sharpen.builder.*;

public class SharpenBuilderPropertyPage extends PropertyPage {
	
	private Text _outputPath;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public SharpenBuilderPropertyPage() {
		super();
	}
	
	private Group createGroup(Composite parent, String groupLabel) {
		Group group = new Group(parent, SWT.SHADOW_IN);
	    group.setText(groupLabel);
	    group.setLayout(new RowLayout(SWT.VERTICAL));
		return group;
	}
	
	private void createOutputFolderGroup(Composite composite, final ISharpenProject configuration) {
		
		Group group = createGroup(composite, "Output Folder: ");
	    
		_outputPath = new Text(group, SWT.NONE);
		_outputPath.setEditable(false);
		setOutputFolder(configuration.getTargetProject());
		
		Button button = new Button(group, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				FolderSelectionDialog dlg = new FolderSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dlg.setTitle("Select Output Location: ");
				dlg.setInput(JavaModelUtility.workspaceRoot());
				dlg.setInitialSelection(getOutputFolder());
				dlg.addFilter(new ViewerFilter() {
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						return element instanceof IContainer;
					}
				});
				if (FolderSelectionDialog.OK == dlg.open())
				{
					setOutputFolder((IContainer) dlg.getFirstResult());
				}
			}
		});
	}

	private void setOutputFolder(IContainer outputFolder) {
		String path = outputFolder.getFullPath().toPortableString();
		_outputPath.setText(path);
		_outputPath.setData(outputFolder);
		_outputPath.setToolTipText(path);
		_outputPath.pack();
	}
	
	private IFolder getOutputFolder() {
		return (IFolder)_outputPath.getData();
	}


	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		try {
			ISharpenProject configuration = SharpenProject.create(selectedProject());
			createOutputFolderGroup(composite, configuration);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return composite;
	}

	private IProject selectedProject() {
		return (IProject) getElement().getAdapter(IProject.class);
	}


}