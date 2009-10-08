Set-up and run a build on windows
---------------------------------

0. Create c:\build3

1. Copy these files into it. They go here:

   c:\build3\build.bat
   c:\build3\build.xml

2. Create the tools structure:

   \build3\tools\apache-ant-1.7.1   -- ant lives in .\bin
   \build3\tools\cygwin             -- install cvs package, cvs lives in .\bin
   \build3\tools\eclipse.sdk        -- eclipse.exe lives in .\eclipse
   \build3\tools\jdk1.5.0_18        -- javac lives in .\bin
   
   (Adjust build.bat if you use other version numbers)
   (Adjust J2SE-1.5 in org.eclipse.riena.build3/build.properties if you 
    move java elsewehere. Need a 1.5 JDK so we compile against the 'proper' 
    version)

3. Create the target directory:

   c:\build3\target\eclipse
   c:\build3\target\eclipse\plugins
   c:\build3\target\eclipse\features

   This should contain the Eclipse RCP SDK and the Equinox SDK features and 
   plugins. Equinox does not come as plugins / features and must currently 
   be repackaged.
   
# TODO [ev]- automate or find other solution? p2.repo2runnable

4. Create the  c:\build3\prebuild\plugins  directory  and copy 
   these files into it:

   org.eclipse.core.net_1.2.1.r35x_20090812-1200.jar
   org.eclipse.core.variables_3.2.200.v20090521.jar
   
   (for example from 3.5 a SDK - we ship those, but don't build them)

Build
-----
   
- Invoke C:\build3\build.bat
