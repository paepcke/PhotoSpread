@echo off
set CURRENT_DIR=%cd%
cd "E:\Users\Paepcke\dldev\src\PhotoSpread\classes"
rem ************************************************************************* 
 rem This script is used to start a Java Application with JProbe agent. 
 rem Please see the details of the script below: 
 rem 		1. It is using JPROBE_HOME\bin\jplauncher.exe executable instead of actual java.exe.
 rem 		2. Using a new java option -jp_input=<path to jpl file>(which contains all java application settings) 
 rem 
 rem 
 rem For more information, please see the JProbe Getting Started Guide or http://www.quest.com/jprobe/. 
 rem ************************************************************************* 

set JPROBE_CMD="C:\\Program Files\\JProbe 8.0\bin\jplauncher.exe"
set JPROBE_JPL=-jp_input="E:\Users\Paepcke\dldev\src\PhotoSpread\Releases\JProbeStartupScripts\PSMem-500-500-100-1_Mem_Settings.jpl" 

%JPROBE_CMD% %JPROBE_JPL%

cd %CURRENT_DIR%

