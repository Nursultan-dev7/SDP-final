"""Check the source hierarchy and the packaged Main entry point without external dependencies."""
from pathlib import Path
import re
import zipfile


root = Path(__file__).resolve().parent.parent
patterns = {"builder", "strategy", "decorator", "adapter", "observer", "facade"}
source_root = root / "src" / "wallet"
assert {path.name for path in source_root.iterdir() if path.is_dir()} == patterns
assert {path.name for path in source_root.glob("*.java")} == {"Main.java"}
assert {path.name for path in (root / "src").iterdir()} == {"wallet"}
packages = set()
for tree in (root / "src", root / "tests"):
    for path in tree.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        declaration = re.search(r"^package ([\w.]+);", text, re.MULTILINE)
        assert declaration, path
        package = declaration[1]
        assert package == "wallet" or package in {"wallet." + name for name in patterns}, path
        assert path.parent.relative_to(tree).as_posix() == package.replace(".", "/"), path
        top_level = re.findall(r"^(?:public )?(?:final )?(?:class|record|interface|enum) (\w+)", text, re.MULTILINE)
        assert top_level == [path.stem], (path, top_level)
        if package == "wallet":
            assert path == source_root / "Main.java", path
        packages.add(package)
assert packages == {"wallet", *["wallet." + name for name in patterns]}
with zipfile.ZipFile(root / "build" / "BlockchainWallet.jar") as jar:
    assert "Main-Class: wallet.Main" in jar.read("META-INF/MANIFEST.MF").decode()
    assert {name for name in jar.namelist() if name.endswith(".class") and name.count("/") == 1} == {"wallet/Main.class"}
    assert {name.split("/")[1] for name in jar.namelist() if name.endswith(".class") and name.count("/") > 1} == patterns
print("PASS: exactly six pattern packages, one top-level class per file, only Main outside them, matching JAR layout.")

# The submitted ZIP must contain the same implementation and report as this checkout.
with zipfile.ZipFile(root / "Blockchain Wallet - Source Code.zip") as archive:
    assert archive.testzip() is None
    prefix = "Blockchain Wallet/"
    expected_java = {prefix + path.relative_to(root).as_posix()
                     for tree in (root / "src", root / "tests") for path in tree.rglob("*.java")}
    assert {name for name in archive.namelist() if name.endswith(".java")} == expected_java
    text_types = {".java", ".md", ".py", ".puml", ".yml", ".sh", ".ps1", ".cmd", ".txt"}
    for entry in archive.infolist():
        assert entry.filename.startswith(prefix), entry.filename
        relative = Path(entry.filename[len(prefix):])
        assert not relative.is_absolute() and ".." not in relative.parts, entry.filename
        assert not {".git", "build", "work", "output"}.intersection(relative.parts), entry.filename
        expected = (root / relative).read_bytes()
        actual = archive.read(entry)
        if relative.suffix in text_types or relative.name in {".gitignore", ".gitattributes"}:
            actual = actual.replace(b"\r\n", b"\n")
            expected = expected.replace(b"\r\n", b"\n")
        assert actual == expected, f"Stale source archive entry: {relative}"
print("PASS: submitted ZIP matches current source, documentation and PDF; no stale wallet classes.")
