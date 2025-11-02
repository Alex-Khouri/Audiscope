tar --exclude="*.DS_Store" -cf "Scripts.zip" Scripts

rsync -e ssh "./Scripts.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope

rmdir /s /q "Scripts.zip"