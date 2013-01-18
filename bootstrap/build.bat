set TOOLSROOT=c:\build3\tools
set JAVA_HOME=%TOOLSROOT%\jdk1.6.0_21
set ANT_HOME=%TOOLSROOT%\apache-ant-1.7.1
REM must use the installed cygwin version, otherwise it fails......
set CVS_HOME_BIN=c:\cygwin\bin
set CVS_SSH=ssh -l rienaBuild
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%CVS_HOME_BIN%;%GITHOME%
set FETCHTAG_PARM=HEAD
set FETCHTAG_GIT_PARM=origin/master
set BUILD_QUALIFIER=HEAD

c:
cd \build3
@echo off

REM ### CHECKS
cvs -version
echo.
git --version
echo.
java -version
echo.

if '%2' EQU '' GOTO :CONT

@echo on
set FETCHTAG_PARM=%2
set FETCHTAG_GIT_PARM=%2
set BUILD_QUALIFIER=%2
@echo off

if '%3' EQU '' GOTO :CONT

@echo on
set FETCHTAG_GIT_PARM=%3
@echo off

:CONT

if '%1' EQU 'build' GOTO :BUILD
if '%1' EQU 'buildgit' GOTO :BUILDGIT
if '%1' EQU 'buildgite4' GOTO :BUILDGITE4
if '%1' EQU 'buildgit3xe4' GOTO :BUILDGIT3XE4
if '%1' EQU 'buildrap' GOTO :BUILDRAP
if '%1' EQU 'runtests' GOTO :RUNTESTS
if '%1' EQU 'runtestse4' GOTO :RUNTESTSE4
if '%1' EQU 'beforesign' GOTO :BEFORESIGN
if '%1' EQU 'aftersign' GOTO :AFTERSIGN
if '%1' EQU 'update' GOTO :UPDATE

echo Usage:
echo build build        - Build Riena against RCP
echo build buildgit     - Build Riena against Git repo
echo build buildgite4   - Build Riena on E4 against Git repo, branch rienaOnE4
echo build buildgit3xe4 - Build Riena against Git repo, branch rienaOnE4
echo build buildrap     - Build Riena against RAP
echo build runtests     - Run tests (must build against RCP first)
echo build runtestse4   - Run tests with E4 (must build against RCP-E4 first)
echo build beforesign   - Steps before sign
echo build aftersign    - Steps after sign
echo build update       - Update ./prebuild dir from server (run when needed) 
GOTO :EOF

:BUILD
echo Building version CVS=%FETCHTAG_PARM% GIT=%FETCHTAG_GIT_PARM% BUILD=%BUILD_QUALIFIER%
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% -DBUILD_QUALIFIER=%BUILD_QUALIFIER% clean build
GOTO :EOF

:BUILDGIT
echo Building version CVS=%FETCHTAG_PARM% GIT=%FETCHTAG_GIT_PARM% BUILD=%BUILD_QUALIFIER%
ant -f build.xml -DECLIPSE_STREAM=3x -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% -DBUILD_QUALIFIER=%BUILD_QUALIFIER% clean buildgit
GOTO :EOF

:BUILDGITE4
echo Building version CVS=%FETCHTAG_PARM% GIT=%FETCHTAG_GIT_PARM% BUILD=%BUILD_QUALIFIER%
ant -f build.xml -DECLIPSE_STREAM=e4 -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% -DBUILD_QUALIFIER=%BUILD_QUALIFIER% clean buildgite4
GOTO :EOF

:BUILDGIT3XE4
echo Building version CVS=%FETCHTAG_PARM% GIT=%FETCHTAG_GIT_PARM% BUILD=%BUILD_QUALIFIER%
ant -f build.xml -DECLIPSE_STREAM=e4 -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% -DBUILD_QUALIFIER=%BUILD_QUALIFIER% clean buildgit3xe4
GOTO :EOF

:BUILDRAP
echo Building version CVS=%FETCHTAG_PARM% GIT=%FETCHTAG_GIT_PARM% BUILD=%BUILD_QUALIFIER% against RAP
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% -DBUILD_QUALIFIER=%BUILD_QUALIFIER% clean buildrap
GOTO :EOF

:RUNTESTS
ant -f build.xml -DECLIPSE_STREAM=3x -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% clean runtests
GOTO :EOF

:RUNTESTSE4
ant -f build.xml -DECLIPSE_STREAM=e4 -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% clean runtestse4
GOTO :EOF

:BEFORESIGN
ant -f build.xml -DECLIPSE_STREAM=3x beforesign
GOTO :EOF

:AFTERSIGN
ant -f build.xml -DECLIPSE_STREAM=3x aftersign
GOTO :EOF

:UPDATE
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% -DFETCHTAG_GIT_PARM=%FETCHTAG_GIT_PARM% update
