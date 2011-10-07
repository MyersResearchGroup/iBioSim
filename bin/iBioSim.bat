setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%BIOSIM%\gui\dist\classes"
for /f %%a IN ('dir /b "%BIOSIM%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%BIOSIM%\gui\lib\%%a"
java -Xmx2048M -Xms2048M -XX:+UseSerialGC -classpath %CLASSPATH% main.Gui

