package wallet;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
final class WalletPanel extends JPanel {
    private static final Color INK = new Color(0x152B3C);
    private static final Color MUTED = new Color(0x496174);
    private static final Color BACKGROUND = new Color(0xEFF4F7);
    private static final Color LINE = new Color(0xD4DFE6);
    private static final Color TEAL = new Color(0x076C63);
    private static final Color ERROR = new Color(0x9C2630);

    private enum FeeOption {
        STANDARD("Standard", new FeeStrategy.Standard()),
        PRIORITY("Priority", new FeeStrategy.Priority());

        private final String label;
        private final FeeStrategy strategy;

        FeeOption(String label, FeeStrategy strategy) {
            this.label = label;
            this.strategy = strategy;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final WalletFacade wallet;
    private final JLabel available = label("", 23, true, TEAL);
    private final JLabel confirmed = label("", 23, true, INK);
    private final JLabel recipientBalance = label("", 23, true, INK);
    private final JLabel notice = label("Ready to review a transfer.", 13, true, TEAL);
    private final JLabel blockInfo = label("Chain tip: genesis block #0", 13, true, INK);
    private final JLabel blockHash = label("Linked blocks use SHA-256.", 12, false, MUTED);
    private final JLabel quote = label("", 14, true, INK);
    private final JLabel demoCode = label("", 14, true, TEAL);
    private final JTextField recipient = new JTextField(WalletFacade.RECIPIENT);
    private final JTextField amount = new JTextField("10.00");
    private final JComboBox<FeeOption> fee = new JComboBox<>(FeeOption.values());
    private final JPasswordField pin = new JPasswordField();
    private final JTextField code = new JTextField();
    private final CardLayout transferCards = new CardLayout();
    private final JPanel transferForm = new JPanel(transferCards);
    private final DefaultTableModel rows = new DefaultTableModel(
            new String[]{"Transfer ID", "Recipient", "Amount (DCO)", "Fee (DCO)", "Status", "Block"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable history = new JTable(rows);
    private final JButton createBlock = button("Create block", true, "create-block", this::mine);
    private WalletFacade.PreparedTransfer prepared;

    WalletPanel(WalletFacade wallet) {
        super(new BorderLayout());
        this.wallet = wallet;
        setPreferredSize(new Dimension(1080, 720));
        setBackground(BACKGROUND);
        setNames();

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));
        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(balanceCards(), BorderLayout.NORTH);
        top.add(transferArea(), BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);
        content.add(historyArea(), BorderLayout.CENTER);
        content.add(notice, BorderLayout.SOUTH);
        add(header(), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        refresh();
    }

    private void setNames() {
        available.setName("available");
        confirmed.setName("confirmed");
        recipientBalance.setName("recipient-balance");
        notice.setName("notice");
        quote.setName("quote");
        demoCode.setName("demo-code");
        recipient.setName("recipient");
        amount.setName("amount");
        fee.setName("fee");
        pin.setName("pin");
        code.setName("code");
        history.setName("history");
    }

    private static JPanel header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(INK);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JPanel titles = column();
        titles.setOpaque(false);
        titles.add(label("Blockchain Wallet", 27, true, Color.WHITE));
        titles.add(label("A design patterns demonstration", 13, false, new Color(0xC1D2DF)));
        header.add(titles, BorderLayout.WEST);
        header.add(label("LOCAL SIMULATION  /  DEMO COIN (DCO)", 12, true, new Color(0x9FE0D3)), BorderLayout.EAST);
        return header;
    }

    private JPanel balanceCards() {
        JPanel balances = new JPanel(new GridLayout(1, 3, 12, 0));
        balances.setOpaque(false);
        balances.add(balanceCard("AVAILABLE TO SPEND", available));
        balances.add(balanceCard("ALICE  /  CONFIRMED", confirmed));
        balances.add(balanceCard("BOB  /  CONFIRMED", recipientBalance));
        return balances;
    }

    private static JPanel balanceCard(String title, JLabel value) {
        JPanel card = column();
        decorate(card);
        value.setMaximumSize(new Dimension(Integer.MAX_VALUE, value.getFontMetrics(value.getFont()).getHeight()));
        card.add(label(title, 12, true, MUTED));
        card.add(Box.createVerticalStrut(4));
        card.add(value);
        return card;
    }

    private JPanel transferArea() {
        JPanel area = new JPanel(new BorderLayout(12, 0));
        area.setOpaque(false);
        transferForm.add(draftForm(), "draft");
        transferForm.add(approvalForm(), "approval");
        area.add(transferForm, BorderLayout.CENTER);

        JPanel guide = column();
        decorate(guide);
        guide.setPreferredSize(new Dimension(305, 0));
        guide.add(label("Demo guide", 18, true, INK));
        guide.add(Box.createVerticalStrut(8));
        guide.add(label("Sender: " + WalletFacade.SENDER, 13, false, MUTED));
        guide.add(label("Recipient: " + WalletFacade.RECIPIENT, 13, false, MUTED));
        guide.add(label("Demo PIN: " + DemoAuthenticator.DEMO_PIN, 13, false, MUTED));
        guide.add(Box.createVerticalStrut(12));
        guide.add(blockInfo);
        guide.add(blockHash);
        area.add(guide, BorderLayout.EAST);
        return area;
    }

    private JPanel draftForm() {
        JPanel form = column();
        decorate(form);
        form.add(label("New transfer", 18, true, INK));
        form.add(Box.createVerticalStrut(8));
        JPanel fields = new JPanel(new GridLayout(1, 3, 10, 0));
        fields.setOpaque(false);
        fields.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(field("Recipient address", recipient));
        fields.add(field("Amount (DCO)", amount));
        fields.add(field("Fee strategy", fee));
        form.add(fields);
        form.add(Box.createVerticalStrut(7));
        form.add(label("Standard: 1.00 DCO   |   Priority: 10% of amount, minimum 2.00 DCO", 12, false, MUTED));
        form.add(Box.createVerticalStrut(8));
        form.add(actions(button("Review transfer", true, "review", this::review)));
        return form;
    }

    private JPanel approvalForm() {
        JPanel form = column();
        decorate(form);
        form.add(label("Confirm transfer", 18, true, INK));
        form.add(Box.createVerticalStrut(6));
        form.add(quote);
        form.add(demoCode);
        form.add(Box.createVerticalStrut(7));
        JPanel fields = new JPanel(new GridLayout(1, 2, 10, 0));
        fields.setOpaque(false);
        fields.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(field("Wallet PIN", pin));
        fields.add(field("One-time code", code));
        form.add(fields);
        form.add(Box.createVerticalStrut(8));
        form.add(actions(button("Authorize & send", true, "send", this::submit),
                button("Cancel", false, "cancel", this::cancel)));
        return form;
    }

    private JPanel historyArea() {
        JPanel area = new JPanel(new BorderLayout(0, 10));
        decorate(area);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label("Transaction history", 18, true, INK), BorderLayout.WEST);
        top.add(createBlock, BorderLayout.EAST);
        area.add(top, BorderLayout.NORTH);

        history.setRowHeight(32);
        history.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        history.setForeground(INK);
        history.setGridColor(LINE);
        history.setShowVerticalLines(false);
        history.setFillsViewportHeight(true);
        history.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        history.getTableHeader().setReorderingAllowed(false);
        history.getTableHeader().setPreferredSize(new Dimension(0, 28));
        history.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        history.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                super.setValue(value);
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground("Confirmed".equals(value) ? TEAL : new Color(0x8A5300));
            }
        });
        JScrollPane scroll = new JScrollPane(history);
        scroll.setColumnHeaderView(history.getTableHeader());
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        area.add(scroll, BorderLayout.CENTER);
        return area;
    }

    private void review() {
        FeeOption option = (FeeOption) fee.getSelectedItem();
        prepared = wallet.prepare(recipient.getText(), amount.getText(), option.strategy);
        Transaction transaction = prepared.transaction();
        quote.setText("To " + transaction.recipient() + "  |  Amount " + money(transaction.amount())
                + "  |  Fee " + money(transaction.fee()) + "  |  Total " + money(transaction.total()));
        demoCode.setText("Simulated device code: " + prepared.demoCode() + "  (valid for 2 minutes)");
        transferCards.show(transferForm, "approval");
        pin.requestFocusInWindow();
        message("Review the total, then enter the demo PIN and device code.", TEAL);
    }

    private void submit() {
        wallet.submit(prepared, new String(pin.getPassword()), code.getText().strip());
        showDraft();
        refresh();
        message("Transfer accepted. Create a block to confirm it.", TEAL);
    }

    private void cancel() {
        wallet.cancel(prepared);
        showDraft();
        message("Transfer review canceled.", TEAL);
    }

    private void showDraft() {
        prepared = null;
        pin.setText("");
        code.setText("");
        demoCode.setText("");
        transferCards.show(transferForm, "draft");
    }

    private void mine() {
        WalletNetwork.BlockSummary block = wallet.createBlock();
        blockInfo.setText("Chain tip: block #" + block.height());
        blockHash.setText("SHA-256: " + block.hash().substring(0, 16) + "...");
        blockHash.setToolTipText(block.hash());
        refresh();
        message("Block #" + block.height() + " confirmed " + block.transactionCount() + " transfer(s).", TEAL);
    }

    private void refresh() {
        WalletFacade.Balances balances = wallet.balances();
        available.setText(money(balances.available()));
        confirmed.setText(money(balances.confirmed()));
        recipientBalance.setText(money(balances.recipient()));
        List<TransactionHistory.Entry> entries = wallet.history();
        rows.setRowCount(0);
        for (TransactionHistory.Entry entry : entries) {
            Transaction transaction = entry.transaction();
            WalletNetwork.Receipt receipt = entry.receipt();
            rows.addRow(new Object[]{transaction.id().toString().substring(0, 8), transaction.recipient(),
                    transaction.amount().toPlainString(), transaction.fee().toPlainString(), receipt.status().label(),
                    receipt.status() == WalletNetwork.Status.CONFIRMED ? "#" + receipt.blockHeight() : "-"});
        }
        boolean hasPending = entries.stream().anyMatch(entry -> entry.receipt().status() == WalletNetwork.Status.PENDING);
        createBlock.setEnabled(hasPending);
        createBlock.setBackground(hasPending ? TEAL : BACKGROUND);
    }

    private static String money(BigDecimal value) {
        return value.toPlainString() + " DCO";
    }

    private void message(String text, Color color) {
        notice.setText(text);
        notice.setForeground(color);
    }

    private static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JPanel field(String name, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel caption = label(name, 12, true, MUTED);
        caption.setLabelFor(input);
        input.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        input.setPreferredSize(new Dimension(100, 29));
        panel.add(caption, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel actions(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        for (JButton button : buttons) {
            panel.add(button);
            panel.add(Box.createHorizontalStrut(8));
        }
        return panel;
    }

    private static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private static void decorate(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
    }

    private JButton button(String title, boolean primary, String name, Runnable action) {
        JButton button = new JButton(title);
        button.setName(name);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setBackground(primary ? TEAL : BACKGROUND);
        button.setForeground(primary ? Color.WHITE : INK);
        button.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        button.addActionListener(event -> {
            try {
                action.run();
            } catch (IllegalArgumentException exception) {
                message(exception.getMessage(), ERROR);
            }
        });
        return button;
    }
}
