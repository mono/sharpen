# Sharpen - Automated Java->C# coversion

[![master](https://img.shields.io/travis/imazen/sharpen/master.svg?label=master)](https://travis-ci.org/imazen/sharpen/builds) [![develop](https://img.shields.io/travis/imazen/slimmage/sharpen.svg?label=develop)](https://travis-ci.org/imazen/sharpen/builds) [![commandline](https://img.shields.io/travis/imazen/slimmage/sharpen.svg?label=commandline)](https://travis-ci.org/imazen/sharpen/builds) 

Sharpen is a library and command-line tool for automating Java to C# code conversion. You can provide configuration classes to control a wide range of class and functionality mapping.

Sharpen doesn’t provide a compatibility runtime (i.e, an implementation of all java functionality on top of .NET), but it does provide some utility classes to meet the most common needs. 

It’s likely that you will need to create a configuration class to customize and perfect your conversion, and you may need to apply patches to the result as well.

Sharpen was originally created by db40 [svn source here](https://source.db4o.com/db4o/trunk) in the format of an Eclipse plugin, but it has since been refactored to work from the command line and on build servers.


### Building and testing sharpen itself

1. Clone this repository
2. Install Java 7 and maven
3. Run ‘mvn clean test’ to test
4. Run ‘mvn install ’ to generate .jar files in /sharpen.core/target

### Running sharpen

1. `mvn install` should have created a file named `sharpencore-0.0.1-SNAPSHOT-jar-with-dependencies.jar`. This is a self-contained copy of sharpen that can be run anywhere.
2. Run `java -jar sharpencore-0.0.1-SNAPSHOT-jar-with-dependencies.jar SOURCEPATH -cp JAR_DEPENDENCY_A JAR_DEPENDENCY_B`  
    Each dependecy needed by the java source should be specified as a full path to the jar file. SOURCEPATH should also be a full path.
3. Run -help for syntax
Minimum command to for conversion 
  
