setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%BIOSIM%\gui\dist\classes"
for /f %%a IN ('dir /b "%BIOSIM%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%BIOSIM%\gui\lib\%%a"
java -Xmx512M -classpath %CLASSPATH% main.Gui

