setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%ATACSGUI%\gui\dist\classes"
for /f %%a IN ('dir /b "%ATACSGUI%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%ATACSGUI%\gui\lib\%%a"
java -Xmx512M -classpath %CLASSPATH% main.Gui -atacs

