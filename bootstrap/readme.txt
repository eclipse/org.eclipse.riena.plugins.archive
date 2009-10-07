Set-up and run a build on windows
---------------------------------

0. Create c:\build3
1. Copy these files into it
2. Create the tool structure on c:

   \build3\tools\apache-ant-1.7.1   -- ant lives in .\bin
   \build3\tools\cygwin             -- install cvs package, cvs lives in .\bin
   \build3\tools\eclipse.sdk        -- eclipse.exe lives in .\eclipse
   \build3\tools\jdk1.5.0_18        -- javac lives in .\bin
   
   (Adjust build.bat if you use other version numbers)
   (Adjust J2SE-1.5 in org.eclipse.riena.build3/build.properties if you 
    move java elsewehere. Need a 1.5 JDK so we compile against the 'proper' 
    version)

3. Create the target on c:\build3\target

   This should contain the Eclipse RCP SDK and the Equinox SDK. Equinox does
   not come as plugins / features and must currently be repackaged.
# TODO [ev]- automate or find other solution?

Build
-----
   
- Invoke C:\build3\build.bat
