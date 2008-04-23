package sharpen.builder;

import org.eclipse.core.resources.IFolder;

public interface ISharpenProject {

	void setTargetFolder(IFolder folder);

	IFolder getTargetFolder();

}