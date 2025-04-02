package task7;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.text.DecimalFormat;

interface Command {
    void execute();
}

class CommandManager {
    private static CommandManager instance;
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;

    private CommandManager() {
        executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Command command = commandQueue.take();
                    command.execute();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public static synchronized CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    public void executeCommand(Command command) {
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Переривання при додаванні команди до черги: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Пул потоків не завершив роботу коректно.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Клас даних
class Data {
    private final List<Integer> numbers;

    public Data(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }

    public List<Integer> getNumbers() {
        return new ArrayList<>(numbers);
    }

    public int getSum() {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }
}

class CustomBorderFactory {
    public static Border createRoundedBorder(Color color, int thickness, int radius) {
        return new RoundedBorder(color, thickness, radius);
    }
}

// Додаємо клас для красивих рамок
class RoundedBorder implements Border {
    private Color color;
    private int thickness;
    private int radius;

    RoundedBorder(Color color, int thickness, int radius) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRoundRect(x + thickness/2, y + thickness/2,
                width - thickness, height - thickness,
                radius, radius);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}

class CustomPinkRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null && !value.toString().isEmpty()) {
            try {
                int num = Integer.parseInt(value.toString());
                if (num % 2 == 0) {
                    c.setBackground(new Color(255, 230, 240)); // Світло-рожевий для парних
                } else {
                    c.setBackground(new Color(255, 200, 220)); // Темніший рожевий для непарних
                }
            } catch (NumberFormatException e) {
                c.setBackground(new Color(255, 230, 240));
            }
        } else {
            c.setBackground(new Color(255, 230, 240));
        }

        setHorizontalAlignment(JLabel.CENTER);
        return c;
    }
}

class GenerateAndDisplayUICommand implements Command {
    private final List<Integer> numbers;
    private final int count;
    private final int bound;
    private final DefaultTableModel tableModel;
    private final JTextArea resultArea;
    private final JTable dataTable;
    private final int columns;

    public GenerateAndDisplayUICommand(List<Integer> numbers, int count, int bound,
                                       DefaultTableModel tableModel, JTextArea resultArea,
                                       JTable dataTable, int columns) {
        this.numbers = numbers;
        this.count = count;
        this.bound = bound;
        this.tableModel = tableModel;
        this.resultArea = resultArea;
        this.dataTable = dataTable;
        this.columns = columns;
    }

    @Override
    public void execute() {
        numbers.clear();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            numbers.add(random.nextInt(bound));
        }

        updateTable();
        resultArea.setText("✨ Згенеровані нові числа! ✨\n" +
                "Всього: " + numbers.size() + " чисел від 0 до " + bound);
    }

    private void updateTable() {
        tableModel.setColumnCount(columns);
        String[] columnNames = new String[columns];
        for (int i = 0; i < columns; i++) {
            columnNames[i] = "🔢 " + (i + 1);
        }
        tableModel.setColumnIdentifiers(columnNames);

        // Розрахунок кількості рядків
        int rows = (int) Math.ceil((double) numbers.size() / columns);
        tableModel.setRowCount(rows);

        // Заповнення таблиці
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index < numbers.size()) {
                    tableModel.setValueAt(numbers.get(index), row, col);
                } else {
                    tableModel.setValueAt("", row, col);
                }
            }
        }

        // Налаштування стилю таблиці
        for (int i = 0; i < columns; i++) {
            dataTable.getColumnModel().getColumn(i).setCellRenderer(new CustomPinkRenderer());
        }
    }
}

class ParallelProcessUICommand implements Command {
    private final List<Integer> numbers;
    private final JTextArea resultArea;

    public ParallelProcessUICommand(List<Integer> numbers, JTextArea resultArea) {
        this.numbers = numbers;
        this.resultArea = resultArea;
    }

    @Override
    public void execute() {
        resultArea.setText("🔄 Виконується паралельна обробка даних...\n");

        try {
            Thread.sleep(500); // Імітація обробки для UI ефекту

            // Обробка даних
            int min = numbers.stream().min(Integer::compare).orElse(0);
            int max = numbers.stream().max(Integer::compare).orElse(0);
            double avg = numbers.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            List<Integer> evens = numbers.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());
            double sum = numbers.stream().mapToInt(Integer::intValue).sum() * 1.0;
            Map<Boolean, List<Integer>> partitioned = numbers.stream()
                    .collect(Collectors.partitioningBy(n -> n > 50));

            DecimalFormat df = new DecimalFormat("#.##");

            StringBuilder sb = new StringBuilder();
            sb.append("✅ Результати обробки даних:\n\n");
            sb.append("🔽 Мінімальне число: ").append(min).append("\n");
            sb.append("🔼 Максимальне число: ").append(max).append("\n");
            sb.append("📊 Середнє значення: ").append(df.format(avg)).append("\n");
            sb.append("🔢 Парні числа: ").append(evens.size()).append(" шт\n");
            sb.append("💯 Сума всіх чисел: ").append(sum).append("\n");
            sb.append("⬆️ Числа більше 50: ").append(partitioned.get(true).size()).append(" шт\n");
            sb.append("⬇️ Числа менше або рівні 50: ").append(partitioned.get(false).size()).append(" шт\n\n");

            sb.append("💕 Статистична обробка:\n");
            sb.append("📝 Кількість елементів: ").append(numbers.size()).append("\n");
            sb.append("✨ Мінімум: ").append(min).append("\n");
            sb.append("✨ Максимум: ").append(max).append("\n");
            sb.append("✨ Середнє: ").append(df.format(avg)).append("\n");

            resultArea.setText(sb.toString());

        } catch (Exception e) {
            resultArea.setText("❌ Помилка при обробці: " + e.getMessage());
        }
    }
}

class PinkUI extends JFrame {
    private final List<Integer> numbers;
    private final CommandManager commandManager = CommandManager.getInstance();
    private final JTextArea resultArea;
    private final int defaultCount = 15;
    private final int defaultBound = 100;
    private final int defaultColumns = 5;
    private final int defaultCellWidth = 6;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public PinkUI() {
        numbers = new ArrayList<>();

        // Базове налаштування вікна
        setTitle("Обробник Даних ");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(255, 200, 230));

        // Макет головного вікна
        setLayout(new BorderLayout(10, 10));

        // Розділ кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(new Color(255, 200, 230));

        // Стиль кнопок
        Font buttonFont = new Font("Arial", Font.BOLD, 12);

        JButton genButton = createPinkButton("Згенерувати числа", buttonFont);
        JButton processButton = createPinkButton("Обробити дані", buttonFont);
        JButton exitButton = createPinkButton("Вийти", buttonFont);

        buttonPanel.add(genButton);
        buttonPanel.add(processButton);
        buttonPanel.add(exitButton);

        // Область результатів
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(255, 230, 240));
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 105, 180), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Таблиця для відображення чисел
        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setBackground(new Color(255, 230, 240));
        dataTable.setGridColor(new Color(255, 182, 193));
        dataTable.getTableHeader().setBackground(new Color(255, 182, 193));
        dataTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 105, 180), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Заголовок
        JLabel headerLabel = new JLabel("Обробник Даних", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(new Color(220, 20, 140));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Додавання компонентів
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(new Color(255, 200, 230));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(tableScroll);
        centerPanel.add(scrollPane);

        add(headerLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Обробники подій
        genButton.addActionListener(e -> generateData());
        processButton.addActionListener(e -> processData());
        exitButton.addActionListener(e -> exitApplication());

        // Автоматично генеруємо дані при запуску
        SwingUtilities.invokeLater(this::generateData);
    }

    private JButton createPinkButton(String text, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(new Color(255, 105, 180));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                CustomBorderFactory.createRoundedBorder(new Color(220, 80, 160), 2, 10),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Ефекти при наведенні
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 20, 147));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 105, 180));
            }
        });

        return button;
    }

    private void generateData() {
        commandManager.executeCommand(new GenerateAndDisplayUICommand(
                numbers, defaultCount, defaultBound, tableModel, resultArea, dataTable, defaultColumns
        ));
    }

    private void processData() {
        if (numbers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Спочатку згенеруйте числа!",
                    "Повідомлення",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        commandManager.executeCommand(new ParallelProcessUICommand(numbers, resultArea));
    }

    private void exitApplication() {
        commandManager.shutdown();
        dispose();
        System.exit(0);
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            PinkUI ui = new PinkUI();
            ui.setVisible(true);
        });
    }
}