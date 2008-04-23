/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core;

import org.eclipse.jdt.core.dom.*;

public interface WellKnownTypeResolver {
	ITypeBinding resolveWellKnownType(String typeName);
}
