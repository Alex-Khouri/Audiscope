find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_linux_all.sh"
chmod a+x "push_linux_css.sh"
chmod a+x "push_linux_jar.sh"
chmod a+x "push_linux_java.sh"
chmod a+x "push_linux_jre.sh"
chmod a+x "push_linux_main.sh"
chmod a+x "push_linux_releases.sh"
chmod a+x "push_linux_scripts.sh"

rsync -e ssh "./Releases/Audiscope (Linux ARM).zip" "./Releases/Audiscope (Linux x86).zip" "./Releases/Audiscope (Mac ARM).zip" "./Releases/Audiscope (Mac x86).zip" "./Releases/Audiscope (Windows ARM).zip" "./Releases/Audiscope (Windows x86).zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/Releases