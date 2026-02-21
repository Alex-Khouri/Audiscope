find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_linux_all.sh"
chmod a+x "push_linux_css.sh"
chmod a+x "push_linux_jar.sh"
chmod a+x "push_linux_java.sh"
chmod a+x "push_linux_jre.sh"
chmod a+x "push_linux_main.sh"
chmod a+x "push_linux_releases.sh"
chmod a+x "push_linux_scripts.sh"

rsync -e ssh "./Release/Audiscope-Linux-ARM.zip" "./Release/Audiscope-Linux-x86.zip" "./Release/Audiscope-Mac-ARM.zip" "./Release/Audiscope-Mac-x86.zip" "./Release/Audiscope-Windows-ARM.zip" "./Release/Audiscope-Windows-x86.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/Release