import sys
from pathlib import Path

from reportlab.lib.colors import HexColor, white
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


ROOT = Path(__file__).resolve().parent.parent
FONTS = Path("C:/Windows/Fonts")
pdfmetrics.registerFont(TTFont("Text", str(FONTS / "segoeui.ttf")))
pdfmetrics.registerFont(TTFont("Strong", str(FONTS / "segoeuib.ttf")))

W, H = A4
M = 48
WIDTH = W - M * 2
NAVY = HexColor("#152B3C")
TEAL = HexColor("#076C63")
MUTED = HexColor("#455C6D")
LINE = HexColor("#C9D7DF")
PALE = HexColor("#EFF4F7")
MINT = HexColor("#E0F1EC")
output = Path(sys.argv[1])
output.parent.mkdir(parents=True, exist_ok=True)
c = canvas.Canvas(str(output), pagesize=A4, pageCompression=1)
c.setTitle("Blockchain Wallet | Final Project Report")
c.setSubject("Six design patterns in a Java wallet simulator")


def text(x, top, value, size=11, font="Text", color=NAVY, align="left"):
    c.setFillColor(color)
    c.setFont(font, size)
    draw = {"left": c.drawString, "center": c.drawCentredString, "right": c.drawRightString}
    draw[align](x, H - top, value)


def paragraph(top, value, x=M, width=WIDTH, size=11, leading=16, color=NAVY):
    style = ParagraphStyle("body", fontName="Text", fontSize=size, leading=leading, textColor=color)
    block = Paragraph(value, style)
    _, height = block.wrap(width, H)
    block.drawOn(c, x, H - top - height)
    return top + height


def box(x, top, width, height, fill, stroke=None, radius=0):
    c.setFillColor(fill)
    if stroke:
        c.setStrokeColor(stroke)
        c.setLineWidth(0.7)
    c.roundRect(x, H - top - height, width, height, radius, fill=1, stroke=int(stroke is not None))


def line(x1, t1, x2, t2, color=LINE, width=0.8, dashed=False):
    c.setStrokeColor(color)
    c.setLineWidth(width)
    c.setDash(3, 3) if dashed else c.setDash()
    c.line(x1, H - t1, x2, H - t2)
    c.setDash()


def arrow(points, *, dashed=False, triangle=False, color=MUTED):
    from math import hypot

    for (x1, t1), (x2, t2) in zip(points, points[1:]):
        line(x1, t1, x2, t2, color, dashed=dashed)
    (x1, t1), (x2, t2) = points[-2:]
    length = hypot(x2 - x1, t2 - t1)
    dx, dt = (x2 - x1) / length, (t2 - t1) / length
    back, side = (8, 4) if triangle else (5, 2.5)
    left = (x2 - back * dx - side * dt, t2 - back * dt + side * dx)
    right = (x2 - back * dx + side * dt, t2 - back * dt - side * dx)
    if triangle:
        c.setStrokeColor(color)
        c.setFillColor(white)
        path = c.beginPath()
        path.moveTo(x2, H - t2)
        path.lineTo(left[0], H - left[1])
        path.lineTo(right[0], H - right[1])
        path.close()
        c.drawPath(path, fill=1, stroke=1)
    else:
        line(*left, x2, t2, color)
        line(*right, x2, t2, color)


def section(index, name, title, subtitle=None):
    text(M, 47, f"{index:02d}  /  {name.upper()}", 10, "Strong", TEAL)
    text(M, 83, title, 29, "Strong")
    if subtitle:
        paragraph(97, subtitle, size=10.5, leading=14, color=MUTED)


def end_page(index):
    line(M, H - 43, W - M, H - 43)
    text(M, H - 25, "BLOCKCHAIN WALLET   /   FINAL PROJECT REPORT", 8, "Strong", MUTED)
    text(W - M, H - 25, f"{index:02d} / 10", 8, "Strong", MUTED, "right")
    c.showPage()


def heading(top, label):
    text(M, top, label, 17, "Strong")


def screenshot(top, name, width=WIDTH):
    height = width * 2 / 3
    x = (W - width) / 2
    c.drawImage(str(ROOT / "docs" / "screens" / name), x, H - top - height, width, height)
    return top + height


box(0, 0, W, 275, NAVY)
box(M, 48, 40, 4, HexColor("#72C9B6"))
text(M, 75, "SOFTWARE DESIGN PATTERNS   /   FINAL PROJECT", 11, "Strong", HexColor("#B9DFD9"))
text(M, 138, "Blockchain", 52, "Strong", white)
text(M, 192, "Wallet", 52, "Strong", white)
paragraph(216, "A Java desktop application that follows a transfer from fee selection to block confirmation.",
          width=390, size=14, leading=20, color=white)
text(M, 310, "PROJECT AUTHORS", 9, "Strong", TEAL)
paragraph(321, "Ташкенбаев Нурсултан · Султанкул Шынгыс · Лопаткин Артем", size=11, leading=15)
c.acroForm.textfield(name="author_names", tooltip="GitHub author identifiers; full names are printed above",
                     value="Nursultan-dev7 / chinusoid / ArtemLopatkin",
                     x=M, y=H - 372, width=WIDTH, height=22, fontSize=10,
                     borderStyle="underlined", borderWidth=1, borderColor=LINE,
                     fillColor=PALE, textColor=NAVY, fieldFlags="", forceBorder=True)

heading(393, "Introduction")
paragraph(410, "A wallet coordinates activities with different responsibilities: fee calculation, transaction construction, authorization, network submission and status updates. This project models that workflow in one Java desktop application and demonstrates how six design patterns divide the work between components.")
heading(496, "Project scope")
paragraph(513, "Alice begins with 100.00 Demo Coin (DCO) and Bob begins with 0.00 DCO. The wallet reviews a transfer to Bob, reserves its amount and fee when submitted, then settles it when a local block is created. The simulator stores its ledger in memory and links blocks with SHA-256 hashes.")
box(M, 599, WIDTH, 107, MINT, radius=8)
text(M + 16, 624, "DEMONSTRATION CASE", 9, "Strong", TEAL)
paragraph(635, "Send 30.00 DCO using the Priority fee of 3.00 DCO. While the transfer is pending, Alice can spend 67.00 DCO. After confirmation, Alice has 67.00 DCO and Bob has 30.00 DCO.",
          x=M + 16, width=WIDTH - 32, size=11, leading=16)
end_page(1)


section(2, "Body", "Six patterns in one transfer", "The class names below match the Java source included with the project.")
patterns = [
    ("Strategy / Behavioral", "StandardFeeStrategy, PriorityFeeStrategy",
     "The UI selects a fee algorithm. Standard charges 1.00 DCO. Priority charges 10% of the amount, rounded to two decimals, with a minimum of 2.00 DCO."),
    ("Builder / Creational", "TransactionBuilder",
     "Collects sender, recipient, amount and fee, validates required fields and creates an immutable Transaction for review."),
    ("Decorator / Structural", "AuthenticatedSender, NetworkSender",
     "Both implement TransferSender. The decorator verifies the demo PIN and the transfer-bound one-time code before delegating to the network sender."),
    ("Adapter / Structural", "WalletNetwork, LocalChainAdapter, LocalChainNode",
     "Translates wallet decimals and UUIDs to the node's integer minor units and string references, and translates numeric receipts back to wallet statuses."),
    ("Observer / Behavioral", "LocalChainNode.onIncluded, TransactionHistory.onConfirmation",
     "The node publishes a receipt when a transfer is included in a block. The adapter relays the receipt to the subscribed history, which updates the matching transfer."),
    ("Facade / Structural", "WalletFacade",
     "Gives the Swing interface one entry point for preparing and submitting transfers, querying balances and history, and creating blocks."),
]
for index, (name, symbols, detail) in enumerate(patterns, 1):
    top = 133 + (index - 1) * 102
    box(M, top, WIDTH, 91, PALE, radius=6)
    text(M + 15, top + 25, f"{index:02d}", 10, "Strong", TEAL)
    text(M + 47, top + 26, name, 16, "Strong")
    text(M + 47, top + 43, symbols, 9, "Strong", TEAL)
    paragraph(top + 50, detail, x=M + 47, width=WIDTH - 64, size=10.25, leading=13.3)
end_page(2)


section(3, "Body / UML", "Class diagram", "Selected classes and interfaces in the transfer and confirmation workflow.")


def uml_class(x, top, name, member, stereotype=None, width=147):
    box(x, top, width, 60, white, LINE, 4)
    if stereotype:
        text(x + width / 2, top + 14, stereotype, 8, "Text", TEAL, "center")
    text(x + width / 2, top + 30, name, 11, "Strong", NAVY, "center")
    line(x, top + 37, x + width, top + 37)
    text(x + 8, top + 51, member, 8.2, "Text", MUTED)


left, middle, right = 48, 224, 400
uml_class(left, 155, "WalletPanel", "review() / submit()")
uml_class(middle, 155, "WalletFacade", "prepare() / createBlock()")
uml_class(right, 155, "FeeStrategy", "calculate(amount)", "«interface»")
uml_class(left, 278, "TransactionHistory", "onConfirmation(receipt)")
uml_class(middle, 278, "TransferSender", "send(transaction, credentials)", "«interface»")
uml_class(right, 278, "TransactionBuilder", "build(): Transaction")
uml_class(left, 411, "AuthenticatedSender", "verifies and delegates")
uml_class(middle, 411, "NetworkSender", "broadcasts to the network")
uml_class(right, 411, "WalletNetwork", "broadcast() / onConfirmation()", "«interface»")
uml_class(middle, 544, "LocalChainNode", "broadcast() / onIncluded() / mine()")
uml_class(right, 544, "LocalChainAdapter", "translates payments and receipts")

arrow([(195, 185), (224, 185)])
arrow([(371, 185), (400, 185)], dashed=True)
arrow([(244, 215), (244, 244), (121, 244), (121, 278)])
arrow([(298, 215), (298, 278)])
arrow([(351, 215), (351, 244), (474, 244), (474, 278)], dashed=True)
arrow([(121, 411), (121, 379), (245, 379), (245, 338)], dashed=True, triangle=True)
arrow([(298, 411), (298, 338)], dashed=True, triangle=True)
arrow([(195, 441), (224, 441)])
arrow([(371, 441), (400, 441)])
arrow([(474, 544), (474, 471)], dashed=True, triangle=True)
arrow([(400, 574), (371, 574)])

line(M, 636, W - M, 636)
arrow([(M, 659), (M + 27, 659)])
text(M + 34, 662, "uses or holds", 9, "Text", MUTED)
arrow([(M + 134, 659), (M + 161, 659)], dashed=True)
text(M + 168, 662, "depends on", 9, "Text", MUTED)
arrow([(M + 263, 659), (M + 290, 659)], dashed=True, triangle=True)
text(M + 297, 662, "implements", 9, "Text", MUTED)
paragraph(684, "Selected classes are shown above; the editable UML includes TransferAuthenticator and Main. The facade and decorator depend on the authentication interface. The composition root assembles concrete demo components.", size=10.5, leading=15)
end_page(3)


section(4, "Body / UML", "Sequence diagram", "A successful transfer moves through review, submission and block confirmation.")
lanes = [86, 171, 256, 341, 426, 511]
names = ["WalletPanel", "WalletFacade", "Authenticated", "LocalChain", "LocalChainNode", "Transaction"]
subnames = ["UI", "Facade", "Sender", "Adapter", "Local ledger", "History"]
for x, name, subname in zip(lanes, names, subnames):
    box(x - 38, 146, 76, 38, PALE, LINE, 4)
    text(x, 162, name, 8.4, "Strong", NAVY, "center")
    text(x, 174, subname, 8, "Text", MUTED, "center")
    line(x, 184, x, 725, LINE, dashed=True)


def message(y, sender, receiver, label, *, reply=False, event=False):
    a, b = lanes[sender], lanes[receiver]
    color = TEAL if event else NAVY
    text((a + b) / 2, y - 5, label, 8.2, "Strong" if event else "Text", color, "center")
    arrow([(a, y), (b, y)], dashed=reply, color=color)


def phase(top, label):
    box(M, top, WIDTH, 19, MINT, radius=3)
    text(M + 7, top + 13, label, 8.5, "Strong", TEAL)


phase(201, "01   REVIEW")
message(242, 0, 1, "prepare()")
box(176, 252, 156, 29, PALE, LINE, 3)
text(184, 264, "Calculate fee and build transaction", 8.2)
text(184, 275, "Validate via adapter and issue code", 8.2)
message(304, 1, 0, "quote + code", reply=True)

phase(318, "02   AUTHORIZE AND SUBMIT")
message(359, 0, 1, "submit()")
message(382, 1, 2, "send()")
box(261, 392, 150, 20, PALE, LINE, 3)
text(268, 405, "Verify PIN + code, then delegate", 8.2)
message(432, 2, 3, "via NetworkSender")
message(455, 3, 4, "broadcast()")
message(478, 4, 3, "IN_POOL", reply=True)
message(501, 3, 1, "Pending receipt via sender", reply=True)
message(524, 1, 5, "recordSubmission()")
message(547, 1, 0, "Pending", reply=True)

phase(563, "03   CONFIRM")
message(604, 0, 1, "createBlock()")
message(627, 1, 3, "createBlock()")
message(650, 3, 4, "mine()")
message(673, 4, 3, "included event", event=True)
message(696, 3, 5, "onConfirmation()", event=True)
message(719, 3, 0, "Block summary via facade", reply=True)
paragraph(741, "After block creation, the UI reads the history again. The Observer callback has already updated the existing entry to Confirmed.", size=10.5, leading=14)
end_page(4)


section(5, "Body / implementation", "Java source excerpts", "These excerpts show how the implemented classes connect along the transfer path.")


def snippet(top, title, filename, first, last):
    source = next((ROOT / "src").rglob(filename))
    lines = source.read_text(encoding="utf-8").splitlines()
    start = next(i for i, value in enumerate(lines) if first in value)
    end = next(i for i, value in enumerate(lines[start:], start) if last in value)
    excerpt = lines[start:end + 1]
    indent = min(len(value) - len(value.lstrip()) for value in excerpt if value.strip())
    text(M, top, title, 13, "Strong")
    text(W - M, top, filename, 9, "Strong", TEAL, "right")
    height = 25 + len(excerpt) * 12.5
    box(M, top + 12, WIDTH, height, PALE, radius=6)
    for index, value in enumerate(excerpt):
        text(M + 12, top + 33 + index * 12.5, value[indent:], min(8.1, (WIDTH - 24) / (max(len(v[indent:]) for v in excerpt) * 0.6)), "Courier")
    return top + height + 12


snippet(148, "Select a fee and build the transaction", "WalletFacade.java",
        "BigDecimal amount =", "return new PreparedTransfer")
snippet(275, "Wrap the sender with authorization", "AuthenticatedSender.java",
        "record AuthenticatedSender", "return delegate.send")
snippet(429, "Translate an inclusion event", "LocalChainAdapter.java",
        "public void onConfirmation", "node.onIncluded")
snippet(524, "Update the subscribed transaction history", "TransactionHistory.java",
        "void onConfirmation", "new Entry(entry.transaction()")
box(M, 636, WIDTH, 86, MINT, radius=6)
text(M + 14, 659, "WIRING", 9, "Strong", TEAL)
paragraph(671, "The Facade constructor subscribes the history through <font name='Courier'>network.onConfirmation(history::onConfirmation)</font>. After confirmation, the UI reads the updated entry from history.",
          x=M + 14, width=WIDTH - 28, size=10.5, leading=14)
end_page(5)


section(6, "Body / application screenshots", "Review and submit")
text(M, 116, "01   REVIEW", 9, "Strong", TEAL)
first_end = screenshot(126, "01-review.png", 447)
paragraph(first_end + 8, "The Priority strategy quotes 3.00 DCO. The review shows the total and a simulated device code.", size=9.7, leading=12)
text(M, 462, "02   PENDING", 9, "Strong", TEAL)
second_end = screenshot(472, "02-pending.png", 447)
paragraph(second_end + 5, "The pending transfer reserves 33.00 DCO. Alice has 67.00 DCO available and 100.00 DCO confirmed.", size=9.7, leading=12)
end_page(6)


section(7, "Results", "Confirm the transfer")
image_end = screenshot(112, "03-confirmed.png")
paragraph(image_end + 7, "03   CONFIRMED. The history displays block #1. Alice has 67.00 DCO and Bob has 30.00 DCO.", size=9.7, leading=13)
heading(503, "Verification")
paragraph(515, "Both test suites pass: fees, invalid input, authorization and expiry, replay, balance reservation, linked blocks, transaction invariants, observer failure isolation, and Swing buttons. Compilation uses Java 17 compatibility with all warnings treated as errors.")
heading(584, "Conclusion")
paragraph(596, "All six patterns participate in one transfer. A failed authorization leaves the ledger untouched. A valid transfer reserves funds, and a node event updates the corresponding history entry after inclusion in a block.")
heading(676, "Further work")
paragraph(688, "Persist wallet history between sessions, add digital signatures and secure key storage, and replace the simulated node and device code with a test network and an independent authentication channel.")
end_page(7)
section(8, "Body / quality", "Design quality and validation", "Concrete responsibilities, extension points and the limits of the local simulator.")
quality = [
    ("Single responsibility", "UI, use cases, authorization, history and ledger live in separate classes. Main assembles concrete components and provides the all-pattern test entry point."),
    ("Open/closed and substitution", "New fee algorithms implement FeeStrategy. Both sender implementations preserve the TransferSender contract; authorization may reject invalid credentials."),
    ("Interface segregation and DIP", "FeeStrategy and TransferAuthenticator are focused contracts. WalletFacade depends on WalletNetwork and TransferAuthenticator; the decorator depends on TransferSender and TransferAuthenticator."),
    ("DRY and KISS", "The canonical Transaction constructor validates every creation path, including Builder. The app uses the Java standard library, immutable values and a small in-memory ledger."),
    ("Correctness boundaries", "Money has exact minor-unit precision and a bounded range. Codes expire at 120 seconds and cannot be replayed. Observer failures are logged without stopping other confirmation deliveries."),
    ("Verified behavior", "Main --test runs both suites with Java 17 compatibility and warnings treated as errors. CI run 43806411176 passed all four Java 17/21 Windows/Linux jobs for d50c169, including exact source and JAR hierarchy checks."),
]
for index, (title, detail) in enumerate(quality):
    top = 135 + index * 87
    text(M, top, title, 13, "Strong", TEAL)
    paragraph(top + 10, detail, size=10.5, leading=14)
box(M, 677, WIDTH, 91, PALE, radius=6)
text(M + 14, 701, "SCOPE AND PERFORMANCE", 9, "Strong", TEAL)
paragraph(713, "Single-threaded Swing EDT use; no persistence, signing or independent second factor. With P pending payments and L observers: reservation O(P), block ordering O(P log P), event delivery O(P * L). Failed observers have no retry delivery.", x=M + 14, width=WIDTH - 28, size=10.2, leading=14)
end_page(8)

section(9, "Team / submission", "Contribution evidence", "The team roster is confirmed. Individual implementation evidence must be recorded honestly.")
text(M, 143, "PROJECT REPOSITORY", 9, "Strong", TEAL)
repo_url = "https://github.com/Nursultan-dev7/SDP-final"
c.acroForm.textfield(name="repository_url", tooltip="Repository URL; ensure the lecturer has access",
                     value=repo_url, x=M, y=H - 177, width=WIDTH, height=22, fontSize=9,
                     borderStyle="underlined", borderWidth=1, borderColor=LINE,
                     fillColor=PALE, textColor=NAVY, forceBorder=True)
team = [
    ("Ташкенбаев Нурсултан", "Nursultan-dev7"),
    ("Султанкул Шынгыс", "chinusoid"),
    ("Лопаткин Артем", "ArtemLopatkin"),
]
for index, (name, login) in enumerate(team):
    top = 208 + index * 91
    text(M, top, name, 12, "Strong")
    text(W - M, top, login, 10, "Strong", TEAL, "right")
    text(M, top + 19, "Implemented patterns (at least two) + commit / PR evidence:", 9, "Text", MUTED)
    c.acroForm.textfield(name="contribution_" + login, tooltip="Two implemented patterns and real commit or PR links for " + login,
                         value="", x=M, y=H - top - 49, width=WIDTH, height=22, fontSize=9,
                         borderStyle="underlined", borderWidth=1, borderColor=LINE,
                         fillColor=PALE, textColor=NAVY, forceBorder=True)
heading(503, "Evidence still to complete")
paragraph(519, "The repository preserves its original import commit and this review branch. Available history does not establish that each of the three students implemented two patterns. Fill the fields above with real contributions before submission; the roster alone is not sufficient.", size=10.5, leading=15)
heading(600, "Submission and defense")
paragraph(616, "Each student must submit the PDF report and source ZIP in Moodle by the announced deadline. Verify the pushed history, all three students' contributions and lecturer access to the repository. The supplied PDF did not specify a calendar deadline.", size=10.5, leading=15)
paragraph(689, "Assessment: code 50%, report 10%, defense 40% (OOP and patterns). The additional instructions specify a 5% deduction per violation. The repository includes a Russian defense guide, requirements checklist and editable UML sources.", size=10.5, leading=15)
end_page(9)
section(10, "Body / structure", "Package hierarchy", "Exactly six pattern packages; Main is the only class outside them.")
layout = [
    "src/wallet/",
    "  Main.java                    composition + --test entry point",
    "  adapter/",
    "    WalletNetwork.java         target interface",
    "    LocalChainAdapter.java     adapter",
    "    LocalChainNode.java        adaptee / confirmation publisher",
    "  builder/",
    "    TransactionBuilder.java    builder",
    "    Transaction.java           immutable product",
    "  decorator/",
    "    TransferSender.java        component interface",
    "    NetworkSender.java         concrete component",
    "    AuthenticatedSender.java   decorator",
    "    TransferAuthenticator.java authorization contract",
    "    DemoAuthenticator.java     demo authorization implementation",
    "  facade/",
    "    WalletFacade.java          facade / use cases",
    "    WalletPanel.java           UI client of the facade",
    "  observer/",
    "    TransactionHistory.java    subscribed concrete observer",
    "  strategy/",
    "    FeeStrategy.java           strategy interface",
    "    StandardFeeStrategy.java   concrete strategy",
    "    PriorityFeeStrategy.java   concrete strategy",
]
box(M, 132, WIDTH, 413, PALE, radius=6)
for index, value in enumerate(layout):
    text(M + 12, 156 + index * 15, value, 8.4, "Courier")
heading(580, "One entry point verifies all patterns")
paragraph(596, "Main --test executes the lifecycle, regression and Swing checks. Test classes live in tests/wallet/facade and share the existing facade package. They are included in the executable JAR so the verification also works without the source tree.", size=10.5, leading=15)
box(M, 667, WIDTH, 91, MINT, radius=6)
text(M + 14, 691, "REPRODUCIBLE CHECKS", 9, "Strong", TEAL)
paragraph(703, "test.cmd (Windows) or sh test.sh (Linux); python docs/verify_structure.py verifies package names, source paths, one top-level class per file, Main as the sole outside class, and the actual JAR layout.", x=M + 14, width=WIDTH - 28, size=10.5, leading=15)
end_page(10)
c.save()
