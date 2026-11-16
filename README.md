# Blockchain Wallet

Java 17+ Swing application demonstrating **six integrated design patterns** in one transfer lifecycle. Alice starts with 100.00 Demo Coin (DCO), Bob with 0.00 DCO. State resets on each launch.

## Run and test

On Windows, install a JDK 17 or newer and run `run.cmd` (from PowerShell: `.\run.cmd`). It builds `build/BlockchainWallet.jar` and starts the application. Run `.\test.cmd` to build and execute all checks, or `.\test.cmd work/screens` to reproduce the screenshots.

On Linux/macOS:

```sh
sh build.sh
java -jar build/BlockchainWallet.jar
sh test.sh
```

The standalone JAR can also verify every pattern without a graphical desktop:

```sh
java -jar build/BlockchainWallet.jar --test
```

`wallet.Main` is the only application entry point. Without arguments it assembles the demo and opens Swing; `--test` invokes both test suites. Test sources share `wallet.facade` and are included in the JAR so the same entry point works for the defense. Both build scripts compile `src` and `tests` with `--release 17 -Xlint:all -Werror` into a clean generated directory.

## Required package hierarchy

```text
src/wallet/
  Main.java
  adapter/
    WalletNetwork.java
    LocalChainAdapter.java
    LocalChainNode.java
  builder/
    Transaction.java
    TransactionBuilder.java
  decorator/
    TransferSender.java
    NetworkSender.java
    AuthenticatedSender.java
    TransferAuthenticator.java
    DemoAuthenticator.java
  facade/
    WalletFacade.java
    WalletPanel.java
  observer/
    TransactionHistory.java
  strategy/
    FeeStrategy.java
    StandardFeeStrategy.java
    PriorityFeeStrategy.java
```

There are **six pattern packages**, with `Main` as the sole class outside them in the parent `wallet` package. Every top-level type has its own matching source file. Supporting UI classes belong to `wallet.facade`; authorization helpers belong to `wallet.decorator`; the local adaptee belongs to `wallet.adapter`. The test sources are in `tests/wallet/facade`, using the existing package.

Run `python docs/verify_structure.py` after a build. It verifies package names and source paths, one top-level type per file, Main as the only outside class, and the actual executable JAR hierarchy. GitHub CI runs these checks with both test suites on Java 17 and 21 under Windows and Linux.

## Six working patterns

| Pattern | Category | Roles and behavior |
| --- | --- | --- |
| Builder | Creational | TransactionBuilder assembles the immutable Transaction product. The canonical constructor validates every creation path. |
| Strategy | Behavioral | FeeStrategy is implemented by StandardFeeStrategy (1.00 DCO) and PriorityFeeStrategy (10%, minimum 2.00 DCO, HALF_UP rounding). |
| Decorator | Structural | TransferSender is the component contract, NetworkSender is the concrete component, and AuthenticatedSender wraps it with PIN/code verification. |
| Adapter | Structural | WalletNetwork is the target, LocalChainAdapter translates units/IDs/statuses, and LocalChainNode is the adaptee. |
| Observer | Behavioral | LocalChainNode publishes inclusion callbacks through the adapter; the subscribed TransactionHistory updates the matching entry. Consumer is the callback contract. |
| Facade | Structural | WalletFacade coordinates review, submission, balances, history and block creation for WalletPanel. |

The project contains at least one pattern of each required category. All six participate in the same lifecycle rather than independent example programs.

## Defense walkthrough

1. Enter recipient `demo-bob`, amount `30.00`, Standard, then **Review transfer**: fee = 1.00 DCO. Cancel, select Priority and review: fee = 3.00, total = 33.00 DCO.
2. Enter PIN `2468` and a wrong code. **Authorize & send** rejects the transfer; history is empty and available balance is 100.00.
3. Enter the displayed simulated device code. The transfer becomes Pending; available = 67.00 and confirmed = 100.00 DCO.
4. Click **Create block**. Observer updates the history to Confirmed in block #1; Alice's confirmed balance is 67.00 and Bob receives 30.00. Hover over the hash to see all 64 hexadecimal characters.
5. Run `java -jar build/BlockchainWallet.jar --test` and show `docs/uml`, the report's hierarchy page, and each student's actual contribution history.

Codes are bound to the reviewed transfer, expire at 120 seconds, and lock after three failed attempts. A new successful review replaces the previous challenge.

## Clean code, SOLID, KISS and DRY

- **SRP:** UI, use cases, authorization, transaction construction, history and ledger are separate classes. Main coordinates application startup and test execution.
- **OCP:** fee algorithms implement FeeStrategy. Adding a visible option also requires registering its label in the UI selector.
- **LSP:** both senders implement the same send/Receipt contract, which permits validation and authorization failures.
- **ISP:** fee, sending and authorization have focused interfaces independent of Swing.
- **DIP:** Facade consumes WalletNetwork and TransferAuthenticator contracts; the decorator consumes TransferSender and TransferAuthenticator. Main selects concrete demo implementations.
- **DRY:** Builder delegates validation to Transaction; money rules and ledger mutations are not duplicated in the UI. Run/test scripts share the same build path.
- **KISS:** Java standard library, immutable values, an in-memory ledger, and six patterns used for actual behavior.

Transaction construction rejects missing IDs/addresses, self-transfers, non-positive amounts, negative fees, unsupported minor-unit precision and overflow. Observer failures are logged and isolated after committing a block so other subscribers still receive confirmations.

The application confines ledger calls to the Swing event dispatch thread. It is not a concurrent server. With P pending payments and L listeners, reservation scans are O(P), block ordering O(P log P), and event delivery O(P * L).

## Verification and deliverables

`WalletSmokeTest` covers the transfer lifecycle, fee selection, invalid input, insufficient balance, authentication failures, lockout, cancellation, replay, reservations, linked blocks and Swing controls. `WalletRegressionTest` covers record invariants, precision/overflow, exact code expiry, superseded reviews, duplicate submissions, fee ordering and observer failures.

[CI run 43806411176](https://github.com/Nursultan-dev7/SDP-final/actions/runs/43806411176) passed all four Java 17/21 Windows/Linux jobs for code commit `d50c169`, including hierarchy verification.

- [Final report](Blockchain%20Wallet%20-%20Final%20Project%20Report.pdf): 10 pages with introduction, body, conclusion, further work, Java excerpts, UML, screenshots, team and package hierarchy.
- [Source ZIP](Blockchain%20Wallet%20-%20Source%20Code.zip): current source tree, test suites, build scripts and documentation.
- `docs/DEFENSE_RU.md`: defense walkthrough and OOP/pattern questions in Russian.
- `docs/REQUIREMENTS_RU.md`: requirement checks and remaining submission work.
- `docs/TEAM.md`: named team and honest contribution evidence.
- `docs/uml/`: editable PlantUML class and sequence diagrams.

To rebuild the report on Windows, install `docs/report-requirements.txt`, run `.\test.cmd work/screens`, copy the three PNGs to `docs/screens`, then run:

```sh
python docs/build_report.py "Blockchain Wallet - Final Project Report.pdf"
python docs/verify_report.py "Blockchain Wallet - Final Project Report.pdf" work/report-preview
python docs/package_source.py
```

The report builder uses the Segoe UI fonts supplied with Windows. The Java app and its tests have no third-party dependencies.

## Team and submission status

Team: Ташкенбаев Нурсултан (`Nursultan-dev7`), Султанкул Шынгыс (`chinusoid`), Лопаткин Артем (`ArtemLopatkin`). The repository's original import history is preserved. The refactor does **not** establish that each student implemented two patterns: record each person's actual implementation commits/PRs in `docs/TEAM.md` and the report. Each student must separately submit the PDF and source ZIP in Moodle by the announced deadline; the supplied requirements PDF did not contain a calendar date.

## Simulation scope

The in-memory demo includes a genesis block, a pending pool and SHA-256-linked blocks. Block creation is manual; fees are removed from the demo supply. All pending transfers are included in the next block, with fees determining their order. There are no real private keys, transaction signatures, distributed consensus or independent second-factor channel. Production use would require these features, persistent storage and a real network.
