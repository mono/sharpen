package sharpen.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;

public class SharpenProjectAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType != ISharpenProject.class) {
			return null;
		}
		try {
			return SharpenProject.create((IProject)adaptableObject);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { ISharpenProject.class };
	}

}
