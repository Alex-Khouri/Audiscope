find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_mac_all.sh"
chmod a+x "push_mac_css.sh"
chmod a+x "push_mac_jar.sh"
chmod a+x "push_mac_java.sh"
chmod a+x "push_mac_jre.sh"
chmod a+x "push_mac_main.sh"
chmod a+x "push_mac_releases.sh"
chmod a+x "push_mac_scripts.sh"

zip -r "CSS.zip" CSS
zip -r "JAR.zip" JAR
zip -r "Java.zip" Java
zip -r "Scripts.zip" Scripts
rsync -e ssh "./CSS.zip" "./JAR.zip" "./Java.zip" "./Scripts.zip" "./readme.txt" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope
rm -f "CSS.zip"
rm -f "JAR.zip"
rm -f "Java.zip"
rm -f "Scripts.zip"

rsync -e ssh "./JRE/Linux ARM.zip" "./JRE/Linux x86.zip" "./JRE/Mac ARM.zip" "./JRE/Mac x86.zip" "./JRE/Windows ARM.zip" "./JRE/Windows x86.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/JRE

rsync -e ssh "./Releases/Audiscope (Linux ARM).zip" "./Releases/Audiscope (Linux x86).zip" "./Releases/Audiscope (Mac ARM).zip" "./Releases/Audiscope (Mac x86).zip" "./Releases/Audiscope (Windows ARM).zip" "./Releases/Audiscope (Windows x86).zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope/Releases