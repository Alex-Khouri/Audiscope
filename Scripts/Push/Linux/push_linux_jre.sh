find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_linux_all.sh"
chmod a+x "push_linux_css.sh"
chmod a+x "push_linux_jar.sh"
chmod a+x "push_linux_java.sh"
chmod a+x "push_linux_jre.sh"
chmod a+x "push_linux_main.sh"
chmod a+x "push_linux_releases.sh"
chmod a+x "push_linux_scripts.sh"

rsync -e ssh "./JRE/Linux ARM.zip" "./JRE/Linux x86.zip" "./JRE/Mac ARM.zip" "./JRE/Mac x86.zip" "./JRE/Windows ARM.zip" "./JRE/Windows x86.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/JRE