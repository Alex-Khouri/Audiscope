tar --exclude="*.DS_Store" -cf "Java.zip" Java

rsync -e ssh "./Java.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope

rmdir /s /q "Java.zip"