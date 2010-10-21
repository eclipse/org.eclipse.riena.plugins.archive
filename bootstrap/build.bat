set TOOLSROOT=c:\build3\tools
set JAVA_HOME=%TOOLSROOT%\jdk1.5.0_18
set ANT_HOME=%TOOLSROOT%\apache-ant-1.7.1
// must use the installed cygwin version, otherwise it fails......
set CVS_HOME_BIN=c:\cygwin\bin
set CVS_SSH=ssh -l rienaBuild
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%CVS_HOME_BIN%
set FETCHTAG_PARM=HEAD

c:
cd \build3
@echo off

REM ### CHECKS
cvs -version
echo.
java -version
echo.

if '%2' EQU '' GOTO :CONT

set FETCHTAG_PARM=%2

:CONT

if '%1' EQU 'build' GOTO :BUILD
if '%1' EQU 'buildindigo' GOTO :BUILDINDIGO
if '%1' EQU 'buildrap' GOTO :BUILDRAP
if '%1' EQU 'runtests' GOTO :RUNTESTS
if '%1' EQU 'beforesign' GOTO :BEFORESIGN
if '%1' EQU 'aftersign' GOTO :AFTERSIGN
if '%1' EQU 'update' GOTO :UPDATE

echo Usage:
echo build build      - Build Riena against RCP
echo build buildrap   - Build Riena against RAP
echo build runtests   - Run tests (must build against RCP first)
echo build beforesign - Steps before sign
echo build aftersign  - Steps after sign
echo build update     - Update ./prebuild dir from server (run when needed) 
GOTO :EOF

:BUILD
echo Building version %FETCHTAG_PARM%
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% clean build
GOTO :EOF

:BUILDINDIGO
echo Building version %FETCHTAG_PARM%
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% clean buildindigo
GOTO :EOF

:BUILDRAP
echo Building version %FETCHTAG_PARM% against RAP
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% clean buildrap
GOTO :EOF

:RUNTESTS
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% clean runtests
GOTO :EOF

:BEFORESIGN
ant -f build.xml beforesign
GOTO :EOF

:AFTERSIGN
ant -f build.xml aftersign
GOTO :EOF

:UPDATE
ant -f build.xml -DFETCHTAG_PARM=%FETCHTAG_PARM% update
