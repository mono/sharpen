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

/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.framework;

public class DynamicVariable<T> {
	
	private static final class SingleVariableCapture<T> implements DynamicVariableCapture {

		private final T _value;
		private final DynamicVariable<T> _variable;

	    private SingleVariableCapture(DynamicVariable<T> variable, T value) {
	    	_variable = variable;
		    _value = value;
	    }

	    @Override
	    public <TRESULT> TRESULT run(Producer<TRESULT> producer) {
	    	return _variable.using(_value, producer);
	    }

	    @Override
	    public DynamicVariableCapture combine(final DynamicVariableCapture context) {
	    	return CombinedVariableCapture.combine(this, context);
	    }
    }
	
	private static final class CombinedVariableCapture implements DynamicVariableCapture {
		
		public static DynamicVariableCapture combine(DynamicVariableCapture head, DynamicVariableCapture tail) {
			if (head == tail) return head;
	    	return new CombinedVariableCapture(head, tail);
	    }
		
        private final DynamicVariableCapture _head;
		private final DynamicVariableCapture _tail;

        private CombinedVariableCapture(DynamicVariableCapture head, DynamicVariableCapture tail) {
	        _head = head;
	        _tail = tail;
        }

        @Override
        public DynamicVariableCapture combine(DynamicVariableCapture context) {
        	return combine(this, context);
        }

		@Override
        public <T> T run(final Producer<T> producer) {
            return _head.run(new Producer<T>() {
        		@Override
                public T produce() {
        			return _tail.run(producer);
                }
            });
        }
    }

	private T _value;
	
	public DynamicVariable(T initialValue) {
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
	
	public <TRESULT> TRESULT using(final T value, final Producer<TRESULT> producer) {
		final ByRef<TRESULT> result = new ByRef<TRESULT>();
		using(value, new Runnable() {
			public void run() {
				result.value = producer.produce();
			}
		});
		return result.value;
	}

	public DynamicVariableCapture using(final T value) {
		return new SingleVariableCapture(this, value);
    }
	
}
