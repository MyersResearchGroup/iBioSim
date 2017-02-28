@rem ***************************************************************************
@rem  
@rem This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
@rem for the latest version of iBioSim.
@rem
@rem Copyright (C) 2017 University of Utah
@rem
@rem This library is free software; you can redistribute it and/or modify it
@rem under the terms of the Apache License. A copy of the license agreement is provided
@rem in the file named "LICENSE.txt" included with this software distribution
@rem and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
@rem  
@rem ***************************************************************************
setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH="%ATACSGUI%\gui\dist\classes"
for /f %%a IN ('dir /b "%ATACSGUI%\gui\lib\*.jar"') do call set CLASSPATH=!CLASSPATH!;"%ATACSGUI%\gui\lib\%%a"
java -Xmx2048M -Xms2048M -XX:+UseSerialGC -classpath %CLASSPATH% main.Gui -atacs

