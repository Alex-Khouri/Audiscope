tar --exclude="*.DS_Store" -cf "JAR.zip" JAR

rsync -e ssh "./JAR.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope

rmdir /s /q "JAR.zip"