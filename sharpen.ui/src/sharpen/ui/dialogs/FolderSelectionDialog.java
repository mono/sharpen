package sharpen.ui.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
  */
public class FolderSelectionDialog extends ElementTreeSelectionDialog implements ISelectionChangedListener {

	private Button fNewFolderButton;
	private IContainer fSelectedContainer;

	public FolderSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
		super(parent, labelProvider, contentProvider);
		setComparator(new ResourceComparator(ResourceComparator.NAME));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite result= (Composite)super.createDialogArea(parent);
		
		getTreeViewer().addSelectionChangedListener(this);
		
		Button button = new Button(result, SWT.PUSH);
		button.setText("Select Output Folder"); 
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				newFolderButtonPressed();
			}
		});
		button.setFont(parent.getFont());
		fNewFolderButton= button;
		
		applyDialogFont(result);
		
		return result;
	}

	private void updateNewFolderButtonState() {
		IStructuredSelection selection= (IStructuredSelection) getTreeViewer().getSelection();
		fSelectedContainer= null;
		if (selection.size() == 1) {
			Object first= selection.getFirstElement();
			if (first instanceof IContainer) {
				fSelectedContainer= (IContainer) first;
			}
		}
		fNewFolderButton.setEnabled(fSelectedContainer != null);
	}	
	
	protected void newFolderButtonPressed() {
		NewFolderDialog dialog= new NewFolderDialog(getShell(), fSelectedContainer);
		if (dialog.open() == Window.OK) {
			TreeViewer treeViewer= getTreeViewer();
			treeViewer.refresh(fSelectedContainer);
			Object createdFolder= dialog.getResult()[0];
			treeViewer.reveal(createdFolder);
			treeViewer.setSelection(new StructuredSelection(createdFolder));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		updateNewFolderButtonState();
	}
	


}
