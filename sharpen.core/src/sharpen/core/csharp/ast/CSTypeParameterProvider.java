/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;

import java.util.*;

public interface CSTypeParameterProvider {
	void addTypeParameter(CSTypeParameter typeParameter);
	
	List<CSTypeParameter> typeParameters();
}
