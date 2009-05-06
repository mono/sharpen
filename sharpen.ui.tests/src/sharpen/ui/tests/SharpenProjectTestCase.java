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

package sharpen.ui.tests;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import sharpen.builder.*;

public class SharpenProjectTestCase extends AbstractConversionTestCase {
	
	public void testCreateReturnsNullForInvalidNature() throws CoreException {
		
		assertNull(SharpenProject.create(_project.getProject()));
	}
	
	public void testProjectWithNatureIsAdaptable() throws CoreException {
		assertNull(getAdapter(ISharpenProject.class));
		
		addSharpenNature();
		
		assertNotNull(getAdapter(ISharpenProject.class));
	}

	private Object getAdapter(Class<?> klass) {
		return _project.getProject().getAdapter(klass);
	}
	
	public void testDefaultConfiguration() throws CoreException {
		addSharpenNature();
		
		IProject srcProject = _project.getProject();
		ISharpenProject sharpen = SharpenProject.create(srcProject);
		assertNotNull(sharpen);
		
		// default configuration points to a non existent folder
		// which will be automatically created by the builder
		// the first time is needed
		IProject targetProject = sharpen.getTargetProject();
		assertNotNull(targetProject);
		
		assertTrue(!targetProject.exists());
		
		IPath targetPath = srcProject.getParent().getFullPath().append(srcProject.getName() + ".net");
		assertPath(targetPath, targetProject);
	}

	private void addSharpenNature() throws CoreException {
		_project.addNature(SharpenNature.NATURE_ID);
	}
	
	private void assertPath(IPath targetPath, IResource targetFolder) {
		assertEquals(targetPath.toPortableString(), targetFolder.getFullPath().toPortableString());
	}

}
