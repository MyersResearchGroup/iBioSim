setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%ATACSGUI%\gui\dist\classes"
for /f %%a IN ('dir /b "%ATACSGUI%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%ATACSGUI%\gui\lib\%%a"
java -Xmx2048M -Xms2048M -XX:+UseSerialGC -classpath %CLASSPATH% main.Gui -atacs

