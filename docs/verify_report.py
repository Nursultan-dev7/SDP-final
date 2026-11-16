"""Validate report structure, canonical form data and appearance streams; render for QA."""
import sys
from pathlib import Path

import pypdfium2 as pdfium
from pypdf import PdfReader


path = Path(sys.argv[1])
directory = Path(sys.argv[2])
directory.mkdir(parents=True, exist_ok=True)
reader = PdfReader(path)
assert len(reader.pages) == 10
fields = reader.get_fields()
assert set(fields) == {
    "author_names", "repository_url", "contribution_Nursultan-dev7",
    "contribution_chinusoid", "contribution_ArtemLopatkin",
}
assert fields["author_names"]["/V"] == "Nursultan-dev7 / chinusoid / ArtemLopatkin"
assert fields["repository_url"]["/V"] == "https://github.com/Nursultan-dev7/SDP-final"
widgets = []
for page in reader.pages:
    for reference in page.get("/Annots", []):
        widget = reference.get_object()
        if widget.get("/Subtype") == "/Widget":
            widgets.append(widget["/T"])
            assert widget.get("/AP", {}).get("/N")
            assert widget.get("/V", "") == fields[widget["/T"]].get("/V", "")
assert set(widgets) == set(fields)
text = "\n".join(page.extract_text() for page in reader.pages)
for heading in ("Introduction", "Conclusion", "Further work", "Class diagram", "Sequence diagram",
                "Java source excerpts", "Contribution evidence", "Package hierarchy", "Creational", "Structural", "Behavioral"):
    assert heading in text, heading
for name in ("Ташкенбаев Нурсултан", "Султанкул Шынгыс", "Лопаткин Артем"):
    assert name in text, name
pdf = pdfium.PdfDocument(path)
pdf.init_forms()
for index, page in enumerate(pdf):
    page.render(scale=1.5).to_pil().save(directory / f"page-{index + 1}.png")
print("PASS: 10 report pages; five interactive fields; canonical values and appearances; names and sections.")
