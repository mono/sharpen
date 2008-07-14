/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com

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

import sharpen.core.SharpenConstants;
import sharpen.core.framework.resources.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SharpenProject implements ISharpenProject {
	
	private static QualifiedName PROJECT_SESSION_KEY = new QualifiedName("sharpen.core.builder", "SharpenProject");
	
	private static String SETTINGS_FILE = ".sharpen";
	
	private static String SETTINGS_CHARSET = "utf-8";
	
	public static ISharpenProject create(IProject project) throws CoreException {
		return create(project, null);
	}

	public static ISharpenProject create(IProject project, IProgressMonitor monitor) throws CoreException {
		if (!project.hasNature(SharpenNature.NATURE_ID)) {
			return null;
		}
		ISharpenProject cached = (ISharpenProject) project.getSessionProperty(PROJECT_SESSION_KEY);
		if (null == cached) {
			cached = new SharpenProject(project);
			project.setSessionProperty(PROJECT_SESSION_KEY, cached);
		}
		return cached;
	}
	
	private final IProject _project;
	
	private IFolder _targetFolder;
	
	private SharpenProject(IProject project) {
		_project = project;
		_targetFolder = getUninitializedTargetFolder(project);
	}

	private IFolder getUninitializedTargetFolder(IProject project) {
		return getUninitializedTargetProject(project).getFolder(SharpenConstants.DEFAULT_TARGET_FOLDER);
	}

	private IProject getUninitializedTargetProject(IProject project) {
		return WorkspaceUtilities.getWorkspaceRoot().getProject(project.getName() + SharpenConstants.SHARPENED_PROJECT_SUFFIX);
	}

	public void setTargetFolder(IFolder folder) {
		if (null == folder) {
			throw new IllegalArgumentException("folder");
		}
		_targetFolder = folder;
	}
	
	public IFolder getTargetFolder() {
		return _targetFolder;
	}
	
	public void save(IProgressMonitor monitor) throws CoreException {
		IFile file = getSettingsFile();
		if (!file.exists()) {
			file.create(toEncodedXml(), true, monitor);
		} else {
			file.setContents(toEncodedXml(), true, true, monitor);
		}
		file.setCharset(SETTINGS_CHARSET, monitor);
	}

	private InputStream toEncodedXml() throws CoreException {
		return encode(toXml());
	}

	private String toXml() {
		XStream stream = createXStream();
		return stream.toXML(new Remembrance(this));
	}
	
	private IFile getSettingsFile() {
		return _project.getFile(SETTINGS_FILE);
	}
	
	private XStream createXStream() {
		XStream stream = new XStream(new DomDriver());
		stream.alias("sharpen", Remembrance.class);
		return stream;
	}

	private InputStream encode(String xml) throws CoreException {
		return WorkspaceUtilities.encode(xml, SETTINGS_CHARSET);
	}

	public static final class Remembrance {
		public String targetFolder;
		
		public Remembrance(SharpenProject project) {
			targetFolder = project.getTargetFolder().getFullPath().toPortableString();
		}
	}
}
