find . -depth -name ".DS_Store" -exec rm {} \;

chmod a+x "push_linux_all.sh"
chmod a+x "push_linux_css.sh"
chmod a+x "push_linux_jar.sh"
chmod a+x "push_linux_java.sh"
chmod a+x "push_linux_jre.sh"
chmod a+x "push_linux_main.sh"
chmod a+x "push_linux_releases.sh"
chmod a+x "push_linux_scripts.sh"

zip -r "CSS.zip" CSS

rsync -e ssh "./CSS.zip" alex-khouri@frs.sourceforge.net:/home/frs/project/audiscope

rm -f "CSS.zip"