rmdir /s /q "Classes"
mkdir "Classes"
javac -classpath "./JAR/*" -d "Classes/" "Java/AppGUI.java" "Java/AudioData.java" "Java/ExecutionBatch.java" "Java/FileProcessingTask.java" "Java/Config.java"

cd Classes
del /F "../JAR/Audiscope.jar"
jar cfe "../JAR/Audiscope.jar" AppGUI "./"
cd ..
rmdir /s /q "Classes"