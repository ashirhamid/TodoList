$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'src\main\java'
$docsDir = Join-Path $projectRoot 'docs\javadoc'
New-Item -ItemType Directory -Force -Path $docsDir | Out-Null
$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
& javadoc -encoding UTF-8 -author -d $docsDir $sources
if ($LASTEXITCODE -ne 0) { throw 'Javadoc generation failed.' }
Write-Host "Javadoc created at $docsDir"
