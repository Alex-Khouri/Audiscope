@echo off
REM ***** Excludes JRE for efficiency *****
REM
REM	Includes:
REM		* CSS
REM		* JAR
REM		* Java
REM		* Readme
REM		* Release
REM		* Scripts
@echo on

tar --exclude="*.DS_Store" -cf "CSS.zip" CSS
tar --exclude="*.DS_Store" -cf "JAR.zip" JAR
tar --exclude="*.DS_Store" -cf "Java.zip" Java
tar --exclude="*.DS_Store" -cf "Scripts.zip" Scripts
rsync -e ssh "./CSS.zip" "./JAR.zip" "./Java.zip" "./Scripts.zip" "./readme.txt" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope
del /q "CSS.zip"
del /q "JAR.zip"
del /q "Java.zip"
del /q "Scripts.zip"

rsync -e ssh "./Release/Audiscope-Linux-ARM.zip" "./Release/Audiscope-Linux-x86.zip" "./Release/Audiscope-Mac-ARM.zip" "./Release/Audiscope-Mac-x86.zip" "./Release/Audiscope-Windows-ARM.zip" "./Release/Audiscope-Windows-x86.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/Release