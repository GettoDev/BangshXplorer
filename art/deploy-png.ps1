#!/usr/bin/env pwsh
# PowerShell version of deploy-png.sh for Windows
# Copies generated PNGs to their respective directories

$ErrorActionPreference = "Stop"

# Copy launcher icons to mipmap directories
$dpiLevels = @("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
foreach ($dpi in $dpiLevels) {
    Copy-Item "launcher_icon-${dpi}.png" "..\app\src\main\res\mipmap-${dpi}\launcher_icon.png" -Force
    Copy-Item "launcher_icon_foreground-${dpi}.png" "..\app\src\main\res\mipmap-${dpi}\launcher_icon_foreground.png" -Force
}

# Copy play store icon
Copy-Item "launcher_icon-play.png" "..\fastlane\metadata\android\en-US\images\icon.png" -Force

# Copy shortcut icons to mipmap directories
$shortcuts = @("directory", "downloads", "file", "ftp_server")
foreach ($shortcut in $shortcuts) {
    foreach ($dpi in $dpiLevels) {
        Copy-Item "${shortcut}_shortcut_icon-${dpi}.png" "..\app\src\main\res\mipmap-${dpi}\${shortcut}_shortcut_icon.png" -Force
    }
}

# Copy banners
Copy-Item "banner-xhdpi.png" "..\app\src\main\res\drawable-xhdpi\banner.png" -Force
Copy-Item "banner-play.png" "..\fastlane\metadata\android\en-US\images\tvBanner.png" -Force

Write-Host "PNG deployment completed successfully"
