package sharpen.ui.popup.actions;

import java.util.ArrayList;
import java.util.List;

import sharpen.core.JavaModelUtility;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;

public class ConvertPackageAction extends AbstractConverterAction {

	protected void safeRun() throws Throwable {
		IPackageFragmentRoot root = (IPackageFragmentRoot) _selection.getFirstElement();
		
		List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
		JavaModelUtility.collectCompilationUnits(units, root);
		
		scheduleConversionJob("C# Conversion of package '" + root.getPath() + "'", units, root.getJavaProject());
	}

}
