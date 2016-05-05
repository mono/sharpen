package sharpen.ui.tests.configuration;

import sharpen.core.Configuration;

public class CustomConfiguration extends Configuration {

    public CustomConfiguration(String runtimeTypeName) {
        super(runtimeTypeName);
    }

    @Override
    public boolean isIgnoredExceptionType(String exceptionType) {
        return false;
    }

    @Override
    public boolean mapByteToSbyte() {
        return false;
    }
}
