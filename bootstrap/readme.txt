Set-up and run a build on windows
---------------------------------

0. Create c:\build3

1. Copy these files into it. They go here:

   c:\build3\build.bat
   c:\build3\build.xml

2. Create the tools structure:

   \build3\tools\apache-ant-1.7.1   -- ant lives in .\bin
   \build3\tools\cygwin             -- install cvs package, cvs lives in .\bin
   \build3\tools\eclipse.sdk\eclipse-SDK-3.6M5-win32.zip
   \build3\tools\jdk1.5.0_18        -- javac lives in .\bin
   \build3\tools\testutils\pde.test.utils_3.5.0.jar -- from riena CVS
                                      
   (Adjust c:\build3\build.{bat,xml} if you use other version numbers)
   (Adjust J2SE-1.5 in org.eclipse.riena.build3/build.properties if you 
    move java elsewhere. Need a 1.5 JDK so we compile against the 'proper' 
    version)

3. Create the target directory with these files:

   c:\build3\target\equinox-SDK-3.6M5.zip
   c:\build3\target\org.eclipse.rcp.source-3.6M5.zip

4. Create the  c:\build3\prebuild\plugins  directory and copy 
   these files into it. The files can be found in a 3.6 SDK - we ship those,
   but don't build them.

   org.eclipse.core.net_1.2.1.r35x_20090812-1200.jar    -- from 3.6.SDK
   org.eclipse.core.variables_3.2.300.v20090911.jar     -- from 3.6 SDK
   org.junit_3.8.2.v20090203-1005.zip                   -- from http://download.eclipse.org/tools/orbit/downloads/drops/R20090529135407/bundles/org.junit_3.8.2.v20090203-1005.zip
   org.easymock_2.4.0.v20090202-0900.jar                -- from http://download.eclipse.org/tools/orbit/downloads/drops/R20090529135407/bundles/org.easymock_2.4.0.v20090202-0900.jar
   org.apache.log4j_1.2.13.v200903072027.jar            -- from http://download.eclipse.org/tools/orbit/downloads/drops/R20090529135407/bundles/org.apache.log4j_1.2.13.v200903072027.jar
   org.apache.commons.beanutils_1.7.0.v200902170505.jar -- from http://download.eclipse.org/tools/orbit/downloads/drops/R20090529135407/bundles/org.apache.commons.beanutils_1.7.0.v200902170505.jar

Build
-----
   
cd \build3
build build

Test
----

(must run build first)
cd \build3
build runtests
