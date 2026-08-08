Param(
    [Parameter(Mandatory=$true)][string]$Path
)

if (-not (Test-Path $Path)) {
    Write-Error "File not found: $Path"
    exit 1
}

$dir = Split-Path $Path -Parent
$temp = Join-Path $dir "000000.tmp"

# If a 000000.tmp exists already, remove it so the rename can succeed
if (Test-Path $temp) { Remove-Item $temp -Force }

# Rename the target file to 000000.tmp
Move-Item -Path $Path -Destination $temp -Force

# Set timestamps to Unix epoch (1 Jan 1970 UTC)
$epoch = [datetime]::ParseExact('1970-01-01T00:00:00Z','yyyy-MM-ddTHH:mm:ssZ',[System.Globalization.CultureInfo]::InvariantCulture)
try {
    [System.IO.File]::SetCreationTimeUtc($temp, $epoch.ToUniversalTime())
    [System.IO.File]::SetLastWriteTimeUtc($temp, $epoch.ToUniversalTime())
    [System.IO.File]::SetLastAccessTimeUtc($temp, $epoch.ToUniversalTime())
} catch {
    # best-effort; continue even if timestamps can't be set
}

# Overwrite with zeros in 64KB blocks, 2 passes
$bufferSize = 65536
$zeroBuffer = New-Object byte[] $bufferSize

# Open file for exclusive write
$fs = [System.IO.File]::Open($temp, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None)
try {
    $length = $fs.Length
    for ($pass = 1; $pass -le 2; $pass++) {
        $fs.Position = 0
        $remaining = $length
        while ($remaining -gt 0) {
            $toWrite = [Math]::Min($bufferSize, $remaining)
            $fs.Write($zeroBuffer, 0, $toWrite)
            $remaining -= $toWrite
        }
        # Try to flush to disk; fallback to Flush() if overload not available
        try { $fs.Flush($true) } catch { $fs.Flush() }
    }

    # Truncate file to 0 bytes and force flush
    $fs.SetLength(0)
    try { $fs.Flush($true) } catch { $fs.Flush() }
} finally {
    $fs.Close()
}

# Finally delete normally
Remove-Item $temp -Force

Write-Output "BNilios-style secure delete completed for: $Path"