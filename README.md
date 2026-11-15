# Blockchain Wallet

Java desktop application for demonstrating six design patterns in one transfer lifecycle. The local simulator starts with 100.00 Demo Coin (DCO) assigned to `demo-alice` and 0.00 DCO to `demo-bob`. Each application launch starts a fresh session.

## Run on Windows

Install a JDK version 17 or newer, extract the ZIP and open `run.cmd`. The script builds `build/BlockchainWallet.jar` and opens the app. From PowerShell in this folder, use `.\run.cmd`.

For other desktop platforms with a JDK and a graphical session, run these commands from the project directory:

```sh
mkdir -p build/classes
javac --release 17 -encoding UTF-8 -d build/classes src/wallet/*.java
java -cp build/classes wallet.WalletApp
```

## Live defense walkthrough

1. Keep the address `demo-bob`, enter `30.00` and choose **Standard**. Click **Review transfer** to show the fee of `1.00 DCO`. Cancel the review, choose **Priority**, and review again. The fee is now `3.00 DCO` and the total is `33.00 DCO`.
2. Enter PIN `2468` and an incorrect device code. **Authorize & send** rejects the transfer, leaves history empty and keeps the available balance at `100.00 DCO`.
3. Enter the six-digit code displayed under **Simulated device code**. The transaction appears as **Pending**. Alice's available balance is `67.00 DCO`, while her confirmed balance remains `100.00 DCO`.
4. Click **Create block**. The transfer moves to **Confirmed** in block `#1`, Alice's confirmed balance becomes `67.00 DCO` and Bob receives `30.00 DCO`. The right-hand panel shows the block hash prefix. Hover over it to view the full hash.

The code is scoped to a reviewed transfer and expires after two minutes. After three failed attempts, cancel the review and create a new one.

## Where the six patterns run

| Pattern | Source | Role in the demo |
| --- | --- | --- |
| Strategy | `FeeStrategy.java` | Fixed Standard fee or percentage-based Priority fee selected by the UI. The simulator sorts pending transfers by fee within each block. |
| Builder | `Transaction.java` | Builds and validates an immutable transfer with a sender, recipient, amount and fee. |
| Decorator | `TransferSender.java` | `AuthenticatedSender` wraps `NetworkSender`, checking the PIN and one-time code before broadcasting. |
| Adapter | `WalletNetwork.java`, `LocalChainAdapter.java` | Bridges the wallet model and the node's integer minor units, string references and numeric status codes. |
| Observer | `LocalChainNode.java`, `TransactionHistory.java` | The node publishes confirmations through the adapter. The subscribed history updates the matching row's status. |
| Facade | `WalletFacade.java` | Coordinates review, authorization, submission, balance queries, block creation and history for the Swing interface. |

## Verify the project

On Windows, run `.\test.cmd` from PowerShell. The test compiles against Java 17 and exercises fees, invalid input, insufficient funds, incorrect codes, lockout, cancellation, replay, reservations, block linkage and the Swing buttons. To reproduce the screenshots, run `.\test.cmd work/screens`.

On other platforms:

```sh
mkdir -p build/test-classes
javac --release 17 -encoding UTF-8 -Xlint:all -d build/test-classes src/wallet/*.java tests/wallet/*.java
java -Djava.awt.headless=true -cp build/test-classes wallet.WalletSmokeTest
```

## Simulation scope

The demo runs in memory and includes a genesis block, a pool of pending transfers and SHA-256-linked blocks. Block creation is manual and fees are removed from the demo supply. The app models the second-factor flow by showing a simulated device code inside the same window. A production wallet would require independent authentication, transaction signing, secure key storage and an external network.

## Final deliverables

The submitted report and source archive are included in this repository as [Blockchain Wallet - Final Project Report.pdf](Blockchain%20Wallet%20-%20Final%20Project%20Report.pdf) and [Blockchain Wallet - Source Code.zip](Blockchain%20Wallet%20-%20Source%20Code.zip).
