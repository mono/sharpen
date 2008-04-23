package sharpen.ui.popup.actions;

public class ConvertCompilationUnitAction extends AbstractConverterAction {


	protected void safeRun() throws Throwable {
		scheduleConversionJob("C# Conversion", _selection.toList(), null);
	}

}