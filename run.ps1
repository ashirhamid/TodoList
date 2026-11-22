$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'src\main\java'
$classesDir = Join-Path $projectRoot 'build\classes'
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
if (-not $sources) { throw 'No Java source files were found.' }
& javac -encoding UTF-8 -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
& java -cp $classesDir edu.oel.tasknest.TaskNestApp
