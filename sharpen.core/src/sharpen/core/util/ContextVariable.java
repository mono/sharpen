/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.util;

public class ContextVariable<T> {
	
	private T _value;
	
	public ContextVariable(T initialValue) {
		_value = initialValue;
	}
	
	public T value() {
		return _value;
	}
	
	public void using(T value, Runnable block) {
		T oldValue = _value;
		_value = value;
		try {
			block.run();
		} finally {
			_value = oldValue;
		}
	}

}
