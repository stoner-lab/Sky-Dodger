# Finishes the emulator setup for Sky Dodger on this machine:
#   1. waits for the system image zip that curl is downloading in the background,
#   2. unpacks it into the Android SDK,
#   3. builds the debug APK,
#   4. boots the AVD, installs the APK, launches the game,
#   5. saves screenshots into docs/screenshots.
#
# Usage: powershell -ExecutionPolicy Bypass -File tools\install-and-screenshot.ps1

[CmdletBinding()]
param(
    [string]$SdkRoot = (Join-Path $env:LOCALAPPDATA 'Android\Sdk'),
    [string]$ZipPath = (Join-Path $env:TEMP 'sysimg-36-default-x86_64.zip'),
    [string]$AvdName = 'SkyDodger',
    [string]$ImageId = 'system-images\android-36\default\x86_64',
    [int64]$ExpectedBytes = 844217077,
    [string]$AppId = 'com.duy.skydodger',
    [string]$Activity = 'com.duy.skydodger/.MainActivity'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$shotDir = Join-Path $projectRoot 'docs\screenshots'

function Wait-ImageDownload {
    if (-not (Test-Path -LiteralPath $ZipPath)) { throw "system image zip not found: $ZipPath" }
    $deadline = (Get-Date).AddMinutes(40)
    while ((Get-Item -LiteralPath $ZipPath).Length -lt $ExpectedBytes) {
        if ((Get-Date) -gt $deadline) { throw 'timed out waiting for the system image download' }
        Write-Host ('waiting for image: {0:N0} / {1:N0} bytes' -f (Get-Item -LiteralPath $ZipPath).Length, $ExpectedBytes)
        Start-Sleep -Seconds 10
    }
    Write-Host 'image download complete'
}

function Install-SystemImage {
    $extract = Join-Path $env:TEMP ('sysimg-extract-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Force -Path $extract | Out-Null
    Write-Host "extracting image to $extract"
    tar.exe -xf $ZipPath -C $extract
    if ($LASTEXITCODE -ne 0) { throw 'tar failed to extract the system image' }

    $systemImg = Get-ChildItem -LiteralPath $extract -Recurse -Filter 'system.img' |
        Select-Object -First 1
    if (-not $systemImg) { throw 'system.img not found inside the archive' }

    $targetDir = Join-Path $SdkRoot $ImageId
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
    if (-not (Test-Path -LiteralPath $targetDir -PathType Container)) {
        throw "target image dir is not a directory: $targetDir"
    }
    Copy-Item -Path (Join-Path $systemImg.Directory.FullName '*') -Destination $targetDir -Recurse -Force
    Write-Host "system image installed into $targetDir"
}

function Ensure-Avd {
    $avdRoot = Join-Path $env:USERPROFILE '.android\avd'
    New-Item -ItemType Directory -Force -Path $avdRoot | Out-Null
    $avdDir = Join-Path $avdRoot "$AvdName.avd"
    New-Item -ItemType Directory -Force -Path $avdDir | Out-Null

    $iniPath = Join-Path $avdRoot "$AvdName.ini"
    if (-not (Test-Path -LiteralPath $iniPath)) {
        Set-Content -LiteralPath $iniPath -Encoding ASCII -Value @(
            'avd.ini.encoding=UTF-8',
            "path=$avdDir",
            "path.rel=avd\$AvdName.avd",
            'target=android-36'
        )
    }

    $configPath = Join-Path $avdDir 'config.ini'
    if (-not (Test-Path -LiteralPath $configPath)) {
        Set-Content -LiteralPath $configPath -Encoding ASCII -Value @(
            "AvdId=$AvdName",
            "avd.ini.displayname=Sky Dodger API 36",
            'avd.ini.encoding=UTF-8',
            'abi.type=x86_64',
            'hw.cpu.arch=x86_64',
            'hw.cpu.ncore=4',
            'image.sysdir.1=system-images\android-36\default\x86_64\',
            'tag.id=default',
            'tag.display=Default',
            'hw.ramSize=3072',
            'vm.heapSize=256',
            'disk.dataPartition.size=6G',
            'hw.lcd.width=1080',
            'hw.lcd.height=2340',
            'hw.lcd.density=440',
            'skin.dynamic=yes',
            'skin.name=1080x2340',
            'showDeviceFrame=no',
            'hw.keyboard=yes',
            'hw.gpu.enabled=yes',
            'hw.gpu.mode=auto',
            'hw.audioInput=yes',
            'hw.audioOutput=yes',
            'hw.camera.back=none',
            'hw.camera.front=none',
            'hw.sdCard=no',
            'PlayStore.enabled=false',
            'fastboot.forceColdBoot=yes'
        )
    }
    Write-Host "AVD ready: $AvdName"
}

function Build-Apk {
    Push-Location $projectRoot
    try {
        & (Join-Path $projectRoot 'gradlew.bat') ':app:assembleDebug' '--console=plain'
        if ($LASTEXITCODE -ne 0) { throw 'gradle build failed' }
    }
    finally {
        Pop-Location
    }
    $apk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
    if (-not (Test-Path -LiteralPath $apk)) { throw "APK not found: $apk" }
    return $apk
}

function Start-Emulator {
    $emulator = Join-Path $SdkRoot 'emulator\emulator.exe'
    $running = & $emulator -list-avds | Where-Object { $_ -eq $AvdName }
    if (-not $running) { throw "emulator cannot see the AVD $AvdName" }
    Start-Process -FilePath $emulator -ArgumentList @(
        '-avd', $AvdName, '-no-snapshot-save', '-no-boot-anim', '-gpu', 'auto'
    ) | Out-Null
}

function Wait-Boot {
    $adb = Join-Path $SdkRoot 'platform-tools\adb.exe'
    & $adb wait-for-device | Out-Null
    $deadline = (Get-Date).AddMinutes(8)
    while ((Get-Date) -lt $deadline) {
        $state = (& $adb shell getprop sys.boot_completed 2>$null) -join ''
        if ($state.Trim() -eq '1') {
            Write-Host 'emulator booted'
            return $adb
        }
        Start-Sleep -Seconds 3
    }
    throw 'emulator did not finish booting'
}

function Save-Screenshot([string]$adb, [string]$name) {
    New-Item -ItemType Directory -Force -Path $shotDir | Out-Null
    $remote = "/sdcard/$name.png"
    & $adb shell screencap -p $remote | Out-Null
    & $adb pull $remote (Join-Path $shotDir "$name.png") | Out-Null
    Write-Host "screenshot saved: docs/screenshots/$name.png"
}

# --- main -------------------------------------------------------------------------

Wait-ImageDownload
Install-SystemImage
Ensure-Avd
$apk = Build-Apk
Start-Emulator
$adb = Wait-Boot

& $adb install -r $apk
& $adb shell am start -n $Activity
Start-Sleep -Seconds 5
Save-Screenshot $adb 'ready'

# Tap to start, tap a few times to fly, then capture the running game.
& $adb shell input tap 540 1400
Start-Sleep -Milliseconds 500
1..6 | ForEach-Object {
    & $adb shell input tap 540 1400
    Start-Sleep -Milliseconds 700
}
Save-Screenshot $adb 'gameplay'

# Stop flapping and wait for the crash screen.
Start-Sleep -Seconds 4
Save-Screenshot $adb 'game-over'

Write-Host 'done - screenshots are in docs/screenshots'
