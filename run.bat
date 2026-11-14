@echo off
set "JDK21=C:\Program Files\BellSoft\LibericaJDK-21-Full"
if exist "%JDK21%\bin\javaw.exe" (
    start "" "%JDK21%\bin\javaw.exe" -jar "%~dp0out\artifacts\TextAnalysis_jar\TextAnalysis.jar"
) else (
    start "" javaw -jar "%~dp0out\artifacts\TextAnalysis_jar\TextAnalysis.jar"
)
