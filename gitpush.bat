@echo off
rem Publie la version courante : commit, push, puis tag (le tag declenche .github/workflows/release.yml).
rem Usage (PowerShell ou cmd, depuis n'importe ou) :
rem   .\gitpush                 version lue dans gradle.properties (mod_version)
rem   .\gitpush "message"       message de commit personnalise
rem   .\gitpush --dry-run       affiche les commandes sans rien executer
setlocal
cd /d "%~dp0"

set "RUN="
set "MSG=%~1"
if "%~1"=="--dry-run" (
    set "RUN=echo [dry-run]"
    set "MSG="
)

set "VERSION="
for /f "tokens=1,* delims==" %%a in ('findstr /b /l /c:"mod_version=" gradle.properties') do set "VERSION=%%b"
if not defined VERSION (
    echo mod_version introuvable dans gradle.properties 1>&2
    exit /b 1
)

findstr /b /l /c:"## [%VERSION%]" CHANGELOG.md >nul
if errorlevel 1 (
    echo Aucune section [%VERSION%] dans CHANGELOG.md : ajoutez les notes de version avant de publier. 1>&2
    exit /b 1
)

git rev-parse -q --verify "refs/tags/%VERSION%" >nul 2>&1
if not errorlevel 1 (
    echo Le tag %VERSION% existe deja : augmentez mod_version dans gradle.properties. 1>&2
    exit /b 1
)

if not defined MSG set "MSG=NetheryX %VERSION%"

%RUN% git add -A || exit /b 1
%RUN% git commit -m "%MSG%" || exit /b 1
%RUN% git push origin main || exit /b 1
%RUN% git tag %VERSION% || exit /b 1
%RUN% git push origin %VERSION% || exit /b 1
if defined RUN (echo Simulation terminee : rien n'a ete execute.) else (echo Version %VERSION% publiee.)
