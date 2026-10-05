$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$rendererPath = Join-Path $projectRoot 'src/main/java/com/colossalfurnaces/client/render/ColossalFurnaceRenderer.java'
$renderer = Get-Content -LiteralPath $rendererPath -Raw
$properties = Get-Content -LiteralPath (Join-Path $projectRoot 'gradle.properties') -Raw
$version = [regex]::Match($properties, '(?m)^neo_version=(.+)$').Groups[1].Value.Trim()
$sourcesPath = Join-Path $projectRoot "build/moddev/artifacts/minecraft-patched-$version-sources.jar"

if (-not (Test-Path -LiteralPath $sourcesPath)) {
    throw 'Minecraft sources are missing. Run gradlew.bat createMinecraftArtifacts first.'
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($sourcesPath)

function Read-Source([string]$path) {
    $entry = $archive.GetEntry($path)
    if ($null -eq $entry) {
        throw "Minecraft source not found: $path"
    }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try {
        return $reader.ReadToEnd()
    } finally {
        $reader.Dispose()
    }
}

try {
    $manager = Read-Source 'net/minecraft/client/resources/model/sprite/AtlasManager.java'
    $ids = Read-Source 'net/minecraft/data/AtlasIds.java'
    if ($manager -notmatch 'atlasById\.get\(atlasId\)' -or
            $manager -notmatch 'AtlasConfig\(TextureAtlas.LOCATION_BLOCKS, AtlasIds.BLOCKS,' -or
            $ids -notmatch 'BLOCKS\s*=\s*Identifier.withDefaultNamespace\("blocks"\)') {
        throw 'The upstream atlas contract changed; review the renderer against the new Minecraft API.'
    }

    # Source-contract regression only: it does not execute a client or render any geometry.
    $lookup = [regex]::Match($renderer, 'getAtlasOrThrow\(([^)]+)\)')
    if (-not $lookup.Success -or $lookup.Groups[1].Value.Trim() -ne 'AtlasIds.BLOCKS') {
        throw 'Atlas lookup must use AtlasIds.BLOCKS (minecraft:blocks), not the atlas texture path.'
    }
    if ($renderer -notmatch 'sprite == null \? texture : TextureAtlas.LOCATION_BLOCKS') {
        throw 'Atlas-backed geometry must still bind the blocks atlas texture, not its definition ID.'
    }
    if ($renderer -match 'sprite\.get[UV]\([^)]*\*\s*16' -or
            ([regex]::Matches($renderer, 'sprite\.get[UV]\(\w+\)')).Count -ne 4) {
        throw 'Atlas sprite UV interpolation must use normalized 0-1 coordinates.'
    }
    Write-Output 'PASS: atlas definition lookup, texture binding, and normalized sprite UV contracts.'
} finally {
    $archive.Dispose()
}
