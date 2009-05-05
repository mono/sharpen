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

package sharpen.ui.popup.actions;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.*;

import sharpen.builder.*;
import sharpen.core.*;
import sharpen.core.framework.resources.*;
import sharpen.ui.*;

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
					IProject targetProject = null;
					if (null != sourceProject) {
						targetProject = JavaModelUtility.getTargetProject(sourceProject, monitor);
						WorkspaceUtilities.addProjectNature(sourceProject.getProject(), SharpenNature.NATURE_ID);
					}
					
					monitor.beginTask(jobName, compilationUnits.size());
					SharpenConversionBatch converter = new SharpenConversionBatch();
					converter.setSource((ICompilationUnit[])compilationUnits.toArray(new ICompilationUnit[compilationUnits.size()]));
					converter.setProgressMonitor(monitor);
					converter.setTargetProject(targetProject);
					final Configuration configuration = converter.getConfiguration();
					configuration.setNamingStrategy(new PascalCaseIdentifiersNamingStrategy());
					configuration.setCreateProblemMarkers(true);
					converter.run();
					int status = monitor.isCanceled() ? IStatus.CANCEL : IStatus.OK;
					return new Status(status, SharpenUI.ID, status, "conversion finished", null);
				} catch (Exception x) {
					x.printStackTrace();
					return new Status(IStatus.ERROR, SharpenUI.ID, IStatus.ERROR, "conversion finished with errors", x);
				}
			}
		};
		job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		job.setUser(true);
		job.schedule();

	}

}
