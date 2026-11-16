"""Package the current source tree and report; exclude generated classes and the ZIP itself."""
from pathlib import Path
import zipfile


root = Path(__file__).resolve().parent.parent
output = root / "Blockchain Wallet - Source Code.zip"
files = []
for directory in ("src", "tests", "docs", ".github"):
    files.extend(path for path in (root / directory).rglob("*")
                 if path.is_file() and "__pycache__" not in path.parts and path.suffix != ".pyc")
for name in ("README.md", ".gitignore", ".gitattributes", "build.ps1", "build.sh",
             "run.cmd", "test.cmd", "test.sh", "Blockchain Wallet - Final Project Report.pdf"):
    files.append(root / name)
with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(files):
        archive.write(path, "Blockchain Wallet/" + path.relative_to(root).as_posix())
with zipfile.ZipFile(output) as archive:
    assert archive.testzip() is None
print(f"PASS: packaged {len(files)} current files without generated output or recursive ZIPs.")
