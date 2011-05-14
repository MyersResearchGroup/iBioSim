setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%LEMA%\gui\dist\classes"
for /f %%a IN ('dir /b "%LEMA%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%LEMA%\gui\lib\%%a"
java -Xmx512M -classpath %CLASSPATH% main.Gui -lema


