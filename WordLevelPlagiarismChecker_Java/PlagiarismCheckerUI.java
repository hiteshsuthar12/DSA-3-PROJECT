import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PlagiarismCheckerUI extends JFrame {

    private File inputFile;
    private File referenceFile;

    private JLabel inputFileNameLabel;
    private JTextArea inputPreview;

    private JLabel refFileNameLabel;
    private JTextArea refPreview;

    private JLabel similarityLabel;
    private JLabel matchingWordsLabel;
    private JLabel processedWordsLabel;
    private JLabel classificationLabel;
    private JProgressBar similarityProgress;
    private JTextArea sequencesArea;

    private JPanel resultsPanel;

    private Color primaryColor = new Color(41, 128, 185);
    private Color bgColor = new Color(245, 247, 250);
    private Color cardColor = Color.WHITE;
    private Color textColor = new Color(44, 62, 80);
    private Color textSecondary = new Color(127, 140, 141);

    public PlagiarismCheckerUI() {
        setTitle("Word-Level Plagiarism Checker");
        setSize(1000, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        getContentPane().setBackground(bgColor);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Word-Level Plagiarism Checker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Analyze document similarity and identify matching word sequences");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(236, 240, 241));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(bgColor);
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Document Panels
        JPanel docsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        docsPanel.setBackground(bgColor);
        docsPanel.setMaximumSize(new Dimension(1000, 300));

        JPanel inputPanel = createDocPanel("Input Document", true);
        JPanel refPanel = createDocPanel("Reference Document", false);

        docsPanel.add(inputPanel);
        docsPanel.add(refPanel);

        mainContent.add(docsPanel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 25)));

        // Action Button
        JButton checkButton = new JButton("CHECK PLAGIARISM");
        checkButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkButton.setBackground(primaryColor);
        checkButton.setForeground(Color.BLACK);
        checkButton.setFocusPainted(false);
        checkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkButton.setMaximumSize(new Dimension(300, 50));
        checkButton.addActionListener(e -> checkPlagiarism());
        
        mainContent.add(checkButton);
        mainContent.add(Box.createRigidArea(new Dimension(0, 25)));

        // Results Section
        resultsPanel = createResultsPanel();
        resultsPanel.setVisible(false);
        resultsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainContent.add(resultsPanel);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDocPanel(String title, boolean isInput) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(223, 230, 233), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(textColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(cardColor);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> clearDocument(isInput));
        topPanel.add(clearBtn, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        JPanel fileSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fileSelectionPanel.setBackground(cardColor);

        JButton chooseBtn = new JButton("Choose File");
        chooseBtn.addActionListener(e -> chooseFile(isInput));

        JLabel fileNameLabel = new JLabel("No file selected");
        fileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        fileNameLabel.setForeground(textSecondary);

        if (isInput) inputFileNameLabel = fileNameLabel;
        else refFileNameLabel = fileNameLabel;

        fileSelectionPanel.add(chooseBtn);
        fileSelectionPanel.add(fileNameLabel);

        JTextArea preview = new JTextArea();
        // Allow copy-pasting by not making it read-only
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        preview.setFont(new Font("Consolas", Font.PLAIN, 12));
        preview.setForeground(textColor);
        preview.setBackground(new Color(250, 251, 252));
        
        if (isInput) inputPreview = preview;
        else refPreview = preview;

        JScrollPane scrollPreview = new JScrollPane(preview);
        scrollPreview.setPreferredSize(new Dimension(0, 150));
        scrollPreview.setBorder(new LineBorder(new Color(223, 230, 233)));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(cardColor);
        centerPanel.add(fileSelectionPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPreview, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setMaximumSize(new Dimension(1000, 600));

        JLabel resTitle = new JLabel("ANALYSIS RESULTS");
        resTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        resTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Stats boxes
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(bgColor);

        JPanel simPanel = createStatBox("Similarity", "0.00%", true);
        similarityLabel = (JLabel) simPanel.getClientProperty("valueLabel");
        
        JPanel matchPanel = createStatBox("Matching Words", "0", false);
        matchingWordsLabel = (JLabel) matchPanel.getClientProperty("valueLabel");
        
        JPanel procPanel = createStatBox("Processed Words", "0", false);
        processedWordsLabel = (JLabel) procPanel.getClientProperty("valueLabel");

        statsPanel.add(simPanel);
        statsPanel.add(matchPanel);
        statsPanel.add(procPanel);
        panel.add(statsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Classification
        classificationLabel = new JLabel("MODERATE SIMILARITY");
        classificationLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        classificationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(classificationLabel);
        
        // Progress bar
        similarityProgress = new JProgressBar(0, 100);
        similarityProgress.setStringPainted(true);
        similarityProgress.setPreferredSize(new Dimension(600, 30));
        similarityProgress.setMaximumSize(new Dimension(600, 30));
        similarityProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(similarityProgress);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Sequences
        JPanel seqPanel = new JPanel(new BorderLayout(0, 5));
        seqPanel.setBackground(cardColor);
        seqPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(223, 230, 233), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel seqTitle = new JLabel("Common 3-Word Sequences");
        seqTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        seqPanel.add(seqTitle, BorderLayout.NORTH);

        sequencesArea = new JTextArea();
        sequencesArea.setEditable(false);
        sequencesArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sequencesArea.setBackground(new Color(250, 251, 252));
        
        JScrollPane seqScroll = new JScrollPane(sequencesArea);
        seqScroll.setPreferredSize(new Dimension(0, 120));
        seqScroll.setBorder(new LineBorder(new Color(223, 230, 233)));
        seqPanel.add(seqScroll, BorderLayout.CENTER);

        panel.add(seqPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Bottom actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionsPanel.setBackground(bgColor);

        JButton viewReportBtn = new JButton("View Report");
        viewReportBtn.addActionListener(e -> openReport());
        
        JButton resetBtn = new JButton("Reset Analysis");
        resetBtn.addActionListener(e -> resetAnalysis());

        actionsPanel.add(viewReportBtn);
        actionsPanel.add(resetBtn);

        panel.add(actionsPanel);

        return panel;
    }

    private JPanel createStatBox(String title, String value, boolean isPrimary) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(cardColor);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(223, 230, 233), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(textSecondary);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, isPrimary ? 32 : 24));
        valueLabel.setForeground(isPrimary ? primaryColor : textColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titleLabel);
        box.add(Box.createRigidArea(new Dimension(0, 10)));
        box.add(valueLabel);
        
        box.putClientProperty("valueLabel", valueLabel);

        return box;
    }

    private void chooseFile(boolean isInput) {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                // Keep the exact same loading behavior as Main
                String content = Main.readFile(file.getAbsolutePath());
                if (isInput) {
                    inputFile = file;
                    inputFileNameLabel.setText(file.getName());
                    inputPreview.setText(content);
                } else {
                    referenceFile = file;
                    refFileNameLabel.setText(file.getName());
                    refPreview.setText(content);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Unable to read the selected document.\nPlease check the file and try again.",
                    "Error Reading File", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearDocument(boolean isInput) {
        if (isInput) {
            inputFile = null;
            inputFileNameLabel.setText("No file selected");
            inputPreview.setText("");
        } else {
            referenceFile = null;
            refFileNameLabel.setText("No file selected");
            refPreview.setText("");
        }
        resultsPanel.setVisible(false);
    }

    private void checkPlagiarism() {
        String inputText = inputPreview.getText().trim();
        String refText = refPreview.getText().trim();

        if (inputText.isEmpty() && refText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both documents or paste text before starting the analysis.", "Missing Documents", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or paste an input document.", "Missing Document", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (refText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or paste a reference document.", "Missing Document", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String inName = (inputFile != null) ? inputFile.getName() : "Pasted_Input";
            String refName = (referenceFile != null) ? referenceFile.getName() : "Pasted_Reference";

            Main.AnalysisResult result = Main.analyze(inputText, refText, inName, refName);

            // Update UI
            similarityLabel.setText(String.format(java.util.Locale.US, "%.2f%%", result.similarity));
            matchingWordsLabel.setText(String.valueOf(result.matchingWordCount));
            processedWordsLabel.setText(String.valueOf(result.inputWordCount));

            similarityProgress.setValue((int) result.similarity);

            classificationLabel.setText(result.classification);
            Color resultColor;
            switch (result.similarityLevel) {
                case HIGH:
                    resultColor = new Color(231, 76, 60); // Red
                    break;
                case MODERATE:
                    resultColor = new Color(243, 156, 18); // Orange
                    break;
                default:
                    resultColor = new Color(46, 204, 113); // Green
                    break;
            }
            classificationLabel.setForeground(resultColor);
            similarityProgress.setForeground(resultColor);

            StringBuilder seqText = new StringBuilder();
            if (result.commonSequences.isEmpty()) {
                seqText.append("No common 3-word sequences found.");
            } else {
                for (int i = 0; i < result.commonSequences.size(); i++) {
                    seqText.append((i + 1)).append(". ").append(result.commonSequences.get(i)).append("\n");
                }
            }
            sequencesArea.setText(seqText.toString());

            resultsPanel.setVisible(true);
            
            // Revalidate and repaint to ensure new components are rendered properly
            getContentPane().revalidate();
            getContentPane().repaint();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during analysis:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openReport() {
        File report = new File("plagiarism_report.txt");
        if (report.exists()) {
            try {
                Desktop.getDesktop().open(report);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Unable to open the report file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Report file not found. Please run an analysis first.", "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetAnalysis() {
        clearDocument(true);
        clearDocument(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PlagiarismCheckerUI().setVisible(true);
        });
    }
}
