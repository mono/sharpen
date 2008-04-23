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
		setOutputFolder(configuration.getTargetFolder());
		
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