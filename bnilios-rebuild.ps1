Param(
    [Parameter(Mandatory=$true)][string]$Path
)

# Validate input exists and is a file
if (-not (Test-Path $Path)) {
    Write-Error "File not found: $Path"
    exit 1
}
if ((Get-Item $Path).PSIsContainer) {
    Write-Error "Path is a directory, not a file: $Path"
    exit 1
}

# Work in the same directory as the target, using the worktree-safe path
$dir = Split-Path $Path -Parent
$temp = Join-Path $dir "000000.tmp"

# If a 000000.tmp exists already, attempt to remove it first (best-effort)
if (Test-Path $temp) {
    try { Remove-Item $temp -Force -ErrorAction Stop } catch { Write-Verbose "Could not remove existing 000000.tmp: $_" }
}

# Ensure attributes allow renaming (remove read-only temporarily)
try {
    $origAttr = (Get-Item $Path).Attributes
    if ($origAttr -band [System.IO.FileAttributes]::ReadOnly) {
        (Get-Item $Path).Attributes = $origAttr -bxor [System.IO.FileAttributes]::ReadOnly
    }
} catch { }

# Rename (move) the file to 000000.tmp; fail if not possible
try {
    Move-Item -Path $Path -Destination $temp -Force -ErrorAction Stop
} catch {
    Write-Error "Failed to rename file to 000000.tmp: $_"
    exit 1
}

# Set timestamps to Unix epoch (1 Jan 1970 UTC) - best-effort
$epoch = [datetime]::UtcNow.Date.AddYears(-1970 + 1970) # keep simple fallback# not used; use explicit date$epoch = [datetime]::ParseExact('1970-01-01T00:00:00Z','yyyy-MM-ddTHH:mm:ssZ',[System.Globalization.CultureInfo]::InvariantCulture)
try {
    [System.IO.File]::SetCreationTimeUtc($temp, $epoch)
    [System.IO.File]::SetLastWriteTimeUtc($temp, $epoch)
    [System.IO.File]::SetLastAccessTimeUtc($temp, $epoch)
} catch {
    Write-Verbose "Could not set timestamps: $_"
}

# Overwrite with zeros in 64KB blocks, exactly 2 passes, using write-through to reduce cache effects$bufferSize = 65536$zeroBuffer = New-Object byte[] $bufferSize
ntry {
    # Open a FileStream with WriteThrough to request non-cached writes where supported    $fs = New-Object System.IO.FileStream($temp, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None, $bufferSize, [System.IO.FileOptions]::WriteThrough)} catch {    # Fallback to Open without FileOptions if constructor not available on this runtime    try { $fs = New-Object System.IO.FileStream($temp, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None, $bufferSize) } catch { Write-Error "Failed to open file for writing: $_"; exit 1 }}
try {    $length = $fs.Length    for ($pass = 1; $pass -le 2; $pass++) {        $fs.Position = 0        $remaining = $length        while ($remaining -gt 0) {            $toWrite = [Math]::Min($bufferSize, $remaining)            $fs.Write($zeroBuffer, 0, $toWrite)            $remaining -= $toWrite        }        # Force write-through/flush to disk. Use Flush(true) when available.        try { $fs.Flush($true) } catch { try { $fs.Flush() } catch { Write-Verbose "Flush failed: $_" } }    }    # Truncate file to 0 bytes then flush again    $fs.SetLength(0)    try { $fs.Flush($true) } catch { try { $fs.Flush() } catch { } }} finally {    # Ensure the stream is closed and disposed    if ($null -ne $fs) { $fs.Close(); $fs.Dispose() }}
# Final best-effort reset of timestamps again (after truncation)try {    [System.IO.File]::SetCreationTimeUtc($temp, $epoch)    [System.IO.File]::SetLastWriteTimeUtc($temp, $epoch)    [System.IO.File]::SetLastAccessTimeUtc($temp, $epoch)} catch { }
# Finally delete normallytry { Remove-Item $temp -Force -ErrorAction Stop } catch { Write-Error "Failed to remove file: $_"; exit 1 }
Write-Output "BNilios-style secure delete completed for: $Path"