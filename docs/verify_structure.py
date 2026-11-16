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
