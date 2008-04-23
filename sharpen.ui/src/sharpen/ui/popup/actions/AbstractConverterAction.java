package sharpen.ui.popup.actions;

import java.util.*;

import sharpen.core.*;
import sharpen.core.resources.*;
import sharpen.ui.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.*;

import sharpen.builder.*;

public abstract class AbstractConverterAction implements IObjectActionDelegate{

	protected IStructuredSelection _selection;

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		_selection = (IStructuredSelection) selection;
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		try {
			
			safeRun();
			
		} catch (Throwable e) {
		    MessageDialog.openError(null, "Error", e.toString());
			e.printStackTrace();
		}
	}
	
	protected abstract void safeRun() throws Throwable;
	
	protected void scheduleConversionJob(final String jobName, final List compilationUnits, final IJavaProject sourceProject) {
		WorkspaceJob job = new WorkspaceJob(jobName) {
			
			@SuppressWarnings("unchecked")
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				try {
					IFolder targetFolder = null;
					if (null != sourceProject) {
						targetFolder = JavaModelUtility.getTargetProjectAndFolder(sourceProject, monitor);
						WorkspaceUtilities.addProjectNature(sourceProject.getProject(), SharpenNature.NATURE_ID);
					}
					
					monitor.beginTask(jobName, compilationUnits.size());
					BatchConverter converter = new BatchConverter();
					converter.setSource((ICompilationUnit[])compilationUnits.toArray(new ICompilationUnit[compilationUnits.size()]));
					converter.setProgressMonitor(monitor);
					converter.setTargetFolder(targetFolder);
					final Configuration configuration = converter.getConfiguration();
					configuration.setNamingStrategy(new PascalCaseIdentifiersNamingStrategy());
					configuration.setCreateProblemMarkers(true);
					converter.run();
					int status = monitor.isCanceled() ? IStatus.CANCEL : IStatus.OK;
					return new Status(status, JavaToCSharpUI.ID, status, "conversion finished", null);
				} catch (Exception x) {
					x.printStackTrace();
					return new Status(IStatus.ERROR, JavaToCSharpUI.ID, IStatus.ERROR, "conversion finished with errors", x);
				}
			}
		};
		job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		job.setUser(true);
		job.schedule();

	}

}
