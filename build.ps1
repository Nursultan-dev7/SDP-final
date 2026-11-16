$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

# Remove only the generated classes directory within this project's build directory.
$projectRoot = [IO.Path]::GetFullPath($PSScriptRoot)
$buildRoot = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build'))
$classesRoot = [IO.Path]::GetFullPath((Join-Path $buildRoot 'classes'))
if (-not $classesRoot.StartsWith($buildRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'The generated classes directory must be inside this project build directory.'
}
if (Test-Path -LiteralPath $classesRoot) {
    Remove-Item -LiteralPath $classesRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $classesRoot -Force | Out-Null
$javaSources = @(Get-ChildItem -LiteralPath 'src', 'tests' -Filter '*.java' -Recurse |
    Sort-Object FullName | ForEach-Object { $_.FullName })
& javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d $classesRoot @javaSources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& jar --create --file (Join-Path $buildRoot 'BlockchainWallet.jar') --main-class wallet.Main -C $classesRoot .
exit $LASTEXITCODE
