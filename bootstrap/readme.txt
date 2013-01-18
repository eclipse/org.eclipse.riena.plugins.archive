Set-up and run a build on windows
---------------------------------

0. Create c:\build3

1. Copy these files into it. They go here:

   c:\build3\build.bat
   c:\build3\build.xml

2. Create the tools structure

   \build3\tools\apache-ant-1.7.1   -- ant lives in .\bin
   \build3\tools\cygwin             -- install cvs package, cvs lives in .\bin
   \build3\tools\eclipse.3x.sdk\eclipse-SDK-3.6-win32.zip
   \build3\tools\jdk1.5.0_18        -- javac lives in .\bin
   \build3\tools\testutils\pde.test.utils_3.5.0.jar 
     -- from the dev.eclipse.org CVS at the following location
        /cvsroot/rt/org.eclipse.riena/org.eclipse.riena.build3/testutils
                                      
   (Adjust c:\build3\build.{bat,xml} if you use other version numbers)
   (Adjust J2SE-1.5 in org.eclipse.riena.build3/build.properties if you 
    move java elsewhere. Need a 1.5 JDK so we compile against the 'proper' 
    version)
    
3. Adjust access credentials for source checkout

   The build is configured for passwordless ssh login to eclipse.org servers.
   Depending on your status, you may need to change how the build accesses the 
   eclipse.org source code repositories:  
   
   (a) if you are a Riena committer contact the rest of the group
   (b) if you are an Eclipse committer edit the .map file and build.xml to
       use your committer id. You will need to set up passwordless login
       with the eclipse.org servers. This will use the real-time CVS repository.
   (c) otherwise edit the .map file and build.xml to use anonymous pserver
       access. This will use the public delayed CVS repository (synced every  
       few minutes).
       
   Map file syntax:
   http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.pde.doc.user/tasks/pde_fetch_phase.htm

4. Create the target directory with these files:

   c:\build3\target\equinox-SDK-3.6.zip                    -- from http://download.eclipse.org/equinox/drops/R-3.6-201006080911/download.php?dropFile=equinox-SDK-3.6.zip
   c:\build3\target\org.eclipse.rcp.source-3.6.zip         -- from http://download.eclipse.org/eclipse/downloads/drops/R-3.6-201006080911/download.php?dropFile=org.eclipse.rcp.source-3.6.zip
   c:\build3\target\rap-runtime-1.4.0-N-20100812-2322.zip  -- from http://eclipse.org/rap/downloads/  (*)
   
   (*) RAP 1.4M2 or later is required. As of 8/16/2010 we use a prerelease  
       nightly build of RAP. Only needed if building Riena-on-RAP.

5. Optional -- Run 'build update' to update the prebuild directory

   If you are updating an existing build run 'cd \build3' and 'build update' 
   to refresh the prebuild directory. This will download all pre-build bundles 
   that are required for the build. 
   
   If you are running a build the very frist time, this is action is performed 
   automatically for you.
   

Build Riena on RCP
------------------
   
cd \build3
build build

Test
----

(must run build first)
cd \build3
build runtests

Build Riena on RAP
------------------

cd \build3
build builrap
