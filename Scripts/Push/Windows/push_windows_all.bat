@echo on

tar --exclude="*.DS_Store" -cf "CSS.zip" CSS
tar --exclude="*.DS_Store" -cf "Java.zip" Java
tar --exclude="*.DS_Store" -cf "JAR.zip" JAR
tar --exclude="*.DS_Store" -cf "Scripts.zip" Scripts
rsync -e ssh "./CSS.zip" "./JAR.zip" "./Java.zip" "./Scripts.zip" "./readme.txt" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope
del /q "CSS.zip"
del /q "Java.zip"
del /q "JAR.zip"
del /q "Scripts.zip"

rsync -e ssh "./JRE/Linux ARM.zip" "./JRE/Linux x86.zip" "./JRE/Mac ARM.zip" "./JRE/Mac x86.zip" "./JRE/Windows ARM.zip" "./JRE/Windows x86.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/JRE

rsync -e ssh "./Releases/Audiscope (Linux ARM).zip" "./Releases/Audiscope (Linux x86).zip" "./Releases/Audiscope (Mac ARM).zip" "./Releases/Audiscope (Mac x86).zip" "./Releases/Audiscope (Windows ARM).zip" "./Releases/Audiscope (Windows x86).zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/Releases