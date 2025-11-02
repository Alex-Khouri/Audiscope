find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_mac_all.sh"
chmod a+x "push_mac_css.sh"
chmod a+x "push_mac_jar.sh"
chmod a+x "push_mac_java.sh"
chmod a+x "push_mac_jre.sh"
chmod a+x "push_mac_main.sh"
chmod a+x "push_mac_releases.sh"
chmod a+x "push_mac_scripts.sh"

rsync -e ssh "./Releases/Audiscope (Linux ARM).zip" "./Releases/Audiscope (Linux x86).zip" "./Releases/Audiscope (Mac ARM).zip" "./Releases/Audiscope (Mac x86).zip" "./Releases/Audiscope (Windows ARM).zip" "./Releases/Audiscope (Windows x86).zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/Releases