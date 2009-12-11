Set-up and run a build on windows
---------------------------------

0. Create c:\build3

1. Copy these files into it. They go here:

   c:\build3\build.bat
   c:\build3\build.xml

2. Create the tools structure:

   \build3\tools\apache-ant-1.7.1   -- ant lives in .\bin
   \build3\tools\cygwin             -- install cvs package, cvs lives in .\bin
   \build3\tools\eclipse.sdk\eclipse-SDK-3.5.1-win32.zip 
   \build3\tools\jdk1.5.0_18        -- javac lives in .\bin
   \build3\tools\testutils\pde.test.utils_3.5.0.jar -- from riena CVS
                                      
   (Adjust c:\build3\build.{bat,xml} if you use other version numbers)
   (Adjust J2SE-1.5 in org.eclipse.riena.build3/build.properties if you 
    move java elsewhere. Need a 1.5 JDK so we compile against the 'proper' 
    version)

3. Create the target directory with these files:

   c:\build3\target\eclipse-RCP-SDK-3.5.1-win32.zip
   c:\build3\target\equinox-SDK-3.5.1.zip

4. Create the  c:\build3\prebuild\plugins  directory  and copy 
   these files into it:

   org.eclipse.core.net_1.2.1.r35x_20090812-1200.jar
   org.eclipse.core.variables_3.2.200.v20090521.jar
   org.junit_3.8.2.v20090203-1005.zip (http://download.eclipse.org/tools/orbit/downloads/drops/R20090529135407/bundles/org.junit_3.8.2.v20090203-1005.zip)
   
   (for example from 3.5 a SDK - we ship those, but don't build them)

Build
-----
   
cd \build3
build build

Test
----

(must run build first)
cd \build3
build runtests

