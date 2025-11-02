find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_mac_all.sh"
chmod a+x "push_mac_css.sh"
chmod a+x "push_mac_jar.sh"
chmod a+x "push_mac_java.sh"
chmod a+x "push_mac_jre.sh"
chmod a+x "push_mac_main.sh"
chmod a+x "push_mac_releases.sh"
chmod a+x "push_mac_scripts.sh"

zip -r "JAR.zip" JAR

rsync -e ssh "./JAR.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope

rm -f "JAR.zip"