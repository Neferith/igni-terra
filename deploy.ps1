$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Users\vivie\.jdks\temurin-24.0.2"



$srcKotlin = "composeApp\build\compileSync\wasmJs\main\developmentExecutable\kotlin"

$srcSkiko  = "composeApp\build\compose\skiko-for-web-runtime"

$srcRes    = "composeApp\build\dist\wasmJs\developmentExecutable\composeResources"

$srcHtml   = "composeApp\src\wasmJsMain\resources\index.html"

$out       = "docs"



Write-Host "Build..." -ForegroundColor Cyan

.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentDistribution --no-daemon



Write-Host "Assemblage..." -ForegroundColor Cyan

if (Test-Path $out) { Remove-Item $out -Recurse -Force }

New-Item -ItemType Directory -Path $out | Out-Null



Copy-Item -Path "$srcKotlin\*" -Destination $out -Force

Copy-Item -Path "$srcSkiko\*"  -Destination $out -Force

Copy-Item -Path $srcHtml        -Destination $out -Force



if (Test-Path $srcRes) {

    $resOut = Join-Path $out "composeResources"

    New-Item -ItemType Directory -Path $resOut -Force | Out-Null

    Copy-Item -Path "$srcRes\*" -Destination $resOut -Recurse -Force

}



Write-Host "Commit..." -ForegroundColor Cyan

git add docs/

$d = Get-Date -Format "yyyy-MM-dd HH:mm"

git commit -m "deploy $d"

git push origin master



Write-Host "Done !" -ForegroundColor Green