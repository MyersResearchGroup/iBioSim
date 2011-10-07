setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%LEMA%\gui\dist\classes"
for /f %%a IN ('dir /b "%LEMA%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%LEMA%\gui\lib\%%a"
java -Xmx2048M -Xms2048M -XX:+UseSerialGC -classpath %CLASSPATH% main.Gui -lema


