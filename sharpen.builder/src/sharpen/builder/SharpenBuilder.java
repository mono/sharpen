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

package sharpen.builder;

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

import sharpen.core.*;

public class SharpenBuilder extends IncrementalProjectBuilder {

	class ChangedCompilationUnitCollector implements IResourceDeltaVisitor {
		
		private final ArrayList<ICompilationUnit> _changes = new ArrayList<ICompilationUnit>();
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				changed(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				changed(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
		
		public List<ICompilationUnit> changes() {
			return _changes;
		}

		private void changed(IResource resource) {
			if (isSourceFile(resource)) {
				_changes.add((ICompilationUnit) JavaCore.create(resource));
			}
		}

		private boolean isSourceFile(IResource resource) {
			if (resource.getType() != IResource.FILE) {
				return false;
			}
			return resource.getFileExtension().equals("java");
		}
	}
	
	public static final String BUILDER_ID = Activator.PLUGIN_ID + ".sharpenBuilder";

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		try {
			if (kind == FULL_BUILD) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					incrementalBuild(delta, monitor);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException, IOException {
		sharpen(JavaModelUtility.collectCompilationUnits(getJavaProject()), monitor);
	}
	
	private IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException, IOException {
		sharpen(changedCompilationUnits(delta), monitor);
	}

	private void sharpen(List<ICompilationUnit> compilationUnits,
			IProgressMonitor monitor) throws CoreException, IOException {
		SharpenConversionBatch converter = new SharpenConversionBatch();
		converter.setTargetProject(getTargetProject(monitor));
		converter.setSource(compilationUnits);
		converter.setProgressMonitor(monitor);
		converter.run();
	}

	private IProject getTargetProject(IProgressMonitor monitor) throws CoreException {
		IProject targetFolder = getConfiguration(monitor).getTargetProject();
		return targetFolder;
	}

	private ISharpenProject getConfiguration(IProgressMonitor monitor) throws CoreException {
		return SharpenProject.create(getProject(), monitor);
	}

	private List<ICompilationUnit> changedCompilationUnits(IResourceDelta delta)
			throws CoreException {
		ChangedCompilationUnitCollector collector = new ChangedCompilationUnitCollector();
		delta.accept(collector);
		return collector.changes();
	}
}
