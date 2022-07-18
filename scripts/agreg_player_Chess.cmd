set OUTPUT_FILE=C:\Temp\Player.java 
set FILES_DIR=E:\eclipse\BattleDev\src\ordije

:debut
del %OUTPUT_FILE%
copy %FILES_DIR%\Player.java %OUTPUT_FILE%
java -classpath ..\bin commons.AgregationFichier %FILES_DIR%\BibliothequeOuverturesChess360.java %OUTPUT_FILE%
java -classpath ..\bin commons.AgregationFichier %FILES_DIR%\Coup.java %OUTPUT_FILE%
java -classpath ..\bin commons.AgregationFichier %FILES_DIR%\Echiquier.java %OUTPUT_FILE%
java -classpath ..\bin commons.AgregationFichier %FILES_DIR%\Evaluateur.java %OUTPUT_FILE%
java -classpath ..\bin commons.AgregationFichier %FILES_DIR%\Moteur.java %OUTPUT_FILE%
java -classpath ..\bin commons.AgregationFichier %FILES_DIR%\Partie.java %OUTPUT_FILE%
pause
goto debut