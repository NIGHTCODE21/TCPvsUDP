import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class UI extends JFrame {
    private boolean darkMode = false;
    private JSlider dropRateSlider;
    private JTextField messageInput;
    private JTextArea tipBox;
    private JCheckBox darkToggle;
    private String lastMessage = "";
    private int lastDropRate = 30;
    private StringBuilder tcpLog = new StringBuilder();
    private StringBuilder udpLog = new StringBuilder();
    private JLabel tcpStats;
    private JLabel udpStats;
    private int tcpSent = 0, tcpReceived = 0, udpSent = 0, udpReceived = 0, udpDropped = 0;
    private JPanel packetLayer;
    private java.util.List<JLabel> packetVisuals = new ArrayList<>();
    private javax.swing.Timer animationTimer;
    public UI() {
        setTitle("TCP vs UDP - Messaging Showdown");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.decode("#1e1e2f"));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(Color.decode("#1e1e2f"));
        centerPanel.add(buildProtocolPanel("TCP", new Color(72, 133, 237)));
        centerPanel.add(buildProtocolPanel("UDP", new Color(255, 152, 0)));

        packetLayer = new JPanel(null);
        packetLayer.setOpaque(false);
        packetLayer.setPreferredSize(new Dimension(800, 60));

        JPanel controlsPanel = buildControls(centerPanel);

        JPanel topWithPackets = new JPanel(new BorderLayout());
        topWithPackets.setOpaque(false);
        topWithPackets.add(packetLayer, BorderLayout.NORTH);
        topWithPackets.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(topWithPackets, BorderLayout.CENTER);
        mainPanel.add(controlsPanel, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel buildControls(JPanel centerPanel) {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBackground(Color.decode("#1e1e2f"));
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

        JLabel msgLabel = new JLabel("\u2709\ufe0f Message to send:");
        msgLabel.setForeground(Color.white);
        controlsPanel.add(msgLabel);

        messageInput = new JTextField("Hello World \ud83c\udf0d");
        messageInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        controlsPanel.add(messageInput);

        JLabel dropLabel = new JLabel("\uD83C\uDF27\uFE0F UDP Packet Drop Rate:");
        dropLabel.setForeground(Color.white);
        controlsPanel.add(dropLabel);

        dropRateSlider = new JSlider(0, 100, 30);
        dropRateSlider.setMajorTickSpacing(25);
        dropRateSlider.setPaintTicks(true);
        dropRateSlider.setPaintLabels(true);
        dropRateSlider.setForeground(Color.white);
        controlsPanel.add(dropRateSlider);

        JButton simulateButton = new JButton("\uD83D\uDE80 Simulate Message Flow");
        simulateButton.setFont(new Font("Arial", Font.BOLD, 16));
        simulateButton.setBackground(new Color(46, 204, 113));
        simulateButton.setForeground(Color.white);
        simulateButton.addActionListener(e -> simulateMessageFlow(centerPanel));
        controlsPanel.add(Box.createVerticalStrut(10));
        controlsPanel.add(simulateButton);

        JButton replayButton = new JButton("\uD83D\uDD04 Replay Last Simulation");
        replayButton.setBackground(Color.orange);
        replayButton.addActionListener(e -> simulateMessageFlow(centerPanel));
        controlsPanel.add(replayButton);

        JButton exportButton = new JButton("\uD83D\uDCC3 Export Logs");
        exportButton.addActionListener(e -> exportLogs());
        controlsPanel.add(exportButton);

        JButton screenshotButton = new JButton("\uD83D\uDCF7 Capture Screenshot");
        screenshotButton.addActionListener(e -> takeScreenshot());
        controlsPanel.add(screenshotButton);

        darkToggle = new JCheckBox("\uD83C\uDF19 Dark Mode");
        darkToggle.setForeground(Color.white);
        darkToggle.setBackground(Color.decode("#1e1e2f"));
        darkToggle.addActionListener(e -> toggleDarkMode(centerPanel));
        controlsPanel.add(darkToggle);

        tcpStats = new JLabel("\uD83D\uDCAC TCP Sent: 0 | Received: 0");
        udpStats = new JLabel("\uD83D\uDCAC UDP Sent: 0 | Received: 0 | Dropped: 0");
        tcpStats.setForeground(Color.white);
        udpStats.setForeground(Color.white);
        controlsPanel.add(tcpStats);
        controlsPanel.add(udpStats);

        tipBox = new JTextArea("\uD83D\uDCA1 TCP ensures ordered and reliable delivery.\n" +
                "\uD83D\uDCA1 UDP is faster but may drop or reorder packets.\n" +
                "Use UDP for gaming, TCP for banking!");
        tipBox.setEditable(false);
        tipBox.setBackground(new Color(255, 253, 208));
        tipBox.setLineWrap(true);
        tipBox.setWrapStyleWord(true);
        tipBox.setBorder(BorderFactory.createTitledBorder("\uD83D\uDCDA Learn Networking"));
        controlsPanel.add(Box.createVerticalStrut(10));
        controlsPanel.add(tipBox);

        return controlsPanel;
    }

    private JPanel buildProtocolPanel(String title, Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#2d2d44"));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(color, 3));

        JLabel titleLabel = new JLabel(title + " Messaging", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(color);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JTextArea messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBackground(Color.decode("#1e1e2f"));
        messageArea.setForeground(Color.white);
        messageArea.setFont(new Font("Consolas", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.putClientProperty("textArea", messageArea);
        return panel;
    }

    private void simulateMessageFlow(JPanel centerPanel) {
        if (animationTimer != null) animationTimer.stop();
        packetLayer.removeAll();
        packetVisuals.clear();

        String message = messageInput.getText();
        int dropRate = dropRateSlider.getValue();
        JPanel tcpPanel = (JPanel) centerPanel.getComponent(0);
        JPanel udpPanel = (JPanel) centerPanel.getComponent(1);
        JTextArea tcpArea = (JTextArea) tcpPanel.getClientProperty("textArea");
        JTextArea udpArea = (JTextArea) udpPanel.getClientProperty("textArea");

        tcpArea.setText("");
        udpArea.setText("");
        tcpLog.setLength(0);
        udpLog.setLength(0);
        tcpSent = tcpReceived = udpSent = udpReceived = udpDropped = 0;

        new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                String part = message + " - Part " + i;
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

                tcpArea.append("\uD83D\uDCEC [TCP Sent] #" + i + " at " + timestamp + ": " + part + "\n");
                tcpLog.append("[TCP Sent] #" + i + " at " + timestamp + ": " + part + "\n");
                animatePacket(i, true);
                tcpSent++;
                sleep(500);

                tcpArea.append("\uD83D\uDCE5 [TCP Received] #" + i + " at " + timestamp + ": " + part + " \u2705\n\n");
                tcpLog.append("[TCP Received] #" + i + " at " + timestamp + ": " + part + "\n\n");
                tcpReceived++;

                udpArea.append("\uD83D\uDCEC [UDP Sent] #" + i + " at " + timestamp + ": " + part + "\n");
                udpLog.append("[UDP Sent] #" + i + " at " + timestamp + ": " + part + "\n");
                boolean dropped = Math.random() * 100 < dropRate;
                animatePacket(i, false, dropped);
                udpSent++;
                sleep(500);

                if (!dropped) {
                    udpArea.append("\uD83D\uDCE5 [UDP Received] #" + i + " at " + timestamp + ": " + part + " \u2705\n\n");
                    udpLog.append("[UDP Received] #" + i + " at " + timestamp + ": " + part + "\n\n");
                    udpReceived++;
                } else {
                    udpArea.append("\u274C [UDP Dropped] #" + i + " at " + timestamp + "\n\n");
                    udpLog.append("[UDP Dropped] #" + i + " at " + timestamp + "\n\n");
                    udpDropped++;
                }

                updateStats();
                sleep(500);
            }
        }).start();
    }

    private void animatePacket(int index, boolean isTCP) {
        animatePacket(index, isTCP, false);
    }

    private void animatePacket(int index, boolean isTCP, boolean drop) {
        JLabel packet = new JLabel("⬤");
        packet.setForeground(isTCP ? Color.CYAN : Color.ORANGE);
        packet.setBounds(0, isTCP ? 10 : 30, 20, 20);
        packetLayer.add(packet);
        packetVisuals.add(packet);

        int duration = drop ? 400 : 800;
        javax.swing.Timer timer = new javax.swing.Timer(20, null); 
        final long start = System.currentTimeMillis();
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float progress = Math.min(1f, elapsed / (float) duration);
            int x = (int) (progress * 700);
            packet.setLocation(x, isTCP ? 10 : 30);

            if (progress >= 1f) { 
        ((javax.swing.Timer) e.getSource()).stop();           
         if (drop) packet.setForeground(new Color(255, 152, 0, 80));
            }
        });
        timer.start();
    }

    private void updateStats() {
        tcpStats.setText("\uD83D\uDCAC TCP Sent: " + tcpSent + " | Received: " + tcpReceived);
        udpStats.setText("\uD83D\uDCAC UDP Sent: " + udpSent + " | Received: " + udpReceived + " | Dropped: " + udpDropped);
    }

    private void exportLogs() {
        try {
            File file = new File("logs_" + System.currentTimeMillis() + ".txt");
            PrintWriter writer = new PrintWriter(file);
            writer.println("--- TCP Logs ---\n" + tcpLog);
            writer.println("--- UDP Logs ---\n" + udpLog);
            writer.close();
            JOptionPane.showMessageDialog(this, "Logs exported to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void takeScreenshot() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screen = robot.createScreenCapture(screenRect);
            File output = new File("screenshot_" + System.currentTimeMillis() + ".png");
            ImageIO.write(screen, "png", output);
            JOptionPane.showMessageDialog(this, "Screenshot saved to: " + output.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void toggleDarkMode(JPanel centerPanel) {
        darkMode = !darkMode;
        Color bg = darkMode ? Color.DARK_GRAY : Color.decode("#1e1e2f");
        Color fg = darkMode ? Color.GREEN : Color.white;

        centerPanel.setBackground(bg);
        for (Component panel : centerPanel.getComponents()) {
            if (panel instanceof JPanel) {
                panel.setBackground(bg);
                JTextArea area = (JTextArea) ((JPanel) panel).getClientProperty("textArea");
                area.setBackground(bg);
                area.setForeground(fg);
            }
        }

        tipBox.setForeground(fg);
        tipBox.setBackground(darkMode ? Color.GRAY : new Color(255, 253, 208));
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI::new);
    }
}
