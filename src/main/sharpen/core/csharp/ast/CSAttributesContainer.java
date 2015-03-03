package sharpen.core.csharp.ast;

import java.util.List;

public interface CSAttributesContainer {

    void addAttribute(CSAttribute attribute);

    boolean removeAttribute(String name);

    List<CSAttribute> attributes();
}
