package sharpen.ui.popup.actions;

import sharpen.core.*;

import org.eclipse.jdt.core.*;

public class ConvertProjectAction extends AbstractConverterAction {

	public void safeRun() throws Throwable {
		IJavaProject project = (IJavaProject) _selection.getFirstElement();
		scheduleConversionJob("C# conversion of " + project.getElementName(), JavaModelUtility.collectCompilationUnits(project), project);
	}
}