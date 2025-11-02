rm -rf "Classes"
mkdir "Classes"
javac -classpath "./JAR/*" -d "Classes/" "Java/AppGUI.java" "Java/AudioData.java" "Java/ExecutionBatch.java" "Java/FileProcessingTask.java" "Java/Config.java"

cd Classes
rm -f "../JAR/Audiscope.jar"
jar cfe "../JAR/Audiscope.jar" AppGUI "./"
cd ..
rm -rf Classes

find . -depth -name ".DS_Store" -exec rm {} \;