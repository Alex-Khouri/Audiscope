tar --exclude="*.DS_Store" -cf "CSS.zip" CSS

rsync -e ssh "./CSS.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope

rmdir /s /q "CSS.zip"