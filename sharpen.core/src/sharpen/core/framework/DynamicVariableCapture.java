package sharpen.core.framework;


public interface DynamicVariableCapture {

	<T> T run(Producer<T> producer);

	DynamicVariableCapture combine(DynamicVariableCapture context);

}
