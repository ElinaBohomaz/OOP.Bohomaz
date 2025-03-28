package task5;

import java.util.*;
import java.io.*;

interface ResultDisplay {
    void displayCustomTable(Data data, int columns, int cellWidth);
}

interface DisplayFactory {
    ResultDisplay createDisplay();
}

abstract class ResultDisplayFactory implements DisplayFactory {
    @Override
    public abstract ResultDisplay createDisplay();
}

class ConsoleResultDisplay implements ResultDisplay {

    @Override
    public void displayCustomTable(Data data, int columns, int cellWidth) {
        System.out.println("Налаштована таблиця:");
        List<Integer> numbers = data.getNumbers();

        printHorizontalLine(columns, cellWidth);

        for (int i = 0; i < numbers.size(); i++) {
            System.out.printf("| %" + (cellWidth - 2) + "d ", numbers.get(i));

            if ((i + 1) % columns == 0 || i == numbers.size() - 1) {
                System.out.println("|");
                printHorizontalLine(columns, cellWidth);
            }
        }
    }

    private void printHorizontalLine(int columns, int cellWidth) {
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < cellWidth; j++) {
                System.out.print("-");
            }
        }
        System.out.println();
    }
}

class ConsoleResultDisplayFactory extends ResultDisplayFactory {
    @Override
    public ResultDisplay createDisplay() {
        return new ConsoleResultDisplay();
    }
}

interface Command {
    void execute();
    void undo();
}

class CommandManager {
    private static CommandManager instance;
    private final Stack<Command> commandHistory = new Stack<>();

    private CommandManager() {}

    public static synchronized CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    public void executeCommand(Command command) {
        command.execute();
        commandHistory.push(command);
    }

    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
        }
    }
}

class MacroCommand implements Command {
    private final List<Command> commands;
    private final List<Integer> savedNumbers;

    public MacroCommand(List<Command> commands, List<Integer> savedNumbers) {
        this.commands = commands;
        this.savedNumbers = savedNumbers;
    }

    @Override
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
        saveToFile(savedNumbers);
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    private void saveToFile(List<Integer> numbers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt"))) {
            for (Integer number : numbers) {
                writer.write(number + " ");
            }
            writer.newLine();
            System.out.println("Дані збережено в файл");
        } catch (IOException e) {
            System.err.println("Помилка збереження даних у файл: " + e.getMessage());
        }
    }
}

class Data {
    private final List<Integer> numbers;

    public Data(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }

    public List<Integer> getNumbers() {
        return new ArrayList<>(numbers);
    }

    public int getResult() {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }

    public void printTable() {
        numbers.forEach(num -> System.out.print(num + " "));
        System.out.println();
    }
}

class Calculator {
    private final Data data;
    private final ResultDisplay display;

    public Calculator(List<Integer> numbers, DisplayFactory factory) {
        this.data = new Data(numbers);
        this.display = factory.createDisplay();
    }

    public void displayCustomResults(int columns, int cellWidth) {
        display.displayCustomTable(data, columns, cellWidth);
    }
}

class GenerateNumbersCommand implements Command {
    private final List<Integer> numbers;
    private final List<Integer> previousNumbers;

    public GenerateNumbersCommand(List<Integer> numbers) {
        this.numbers = numbers;
        this.previousNumbers = new ArrayList<>(numbers);
    }

    @Override
    public void execute() {
        numbers.clear();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            numbers.add(random.nextInt(100));
        }
    }

    @Override
    public void undo() {
        numbers.clear();
        numbers.addAll(previousNumbers);
    }
}

class CalculatorTest {
    public static boolean testCustomTableDisplay(List<Integer> numbers) {
        try {
            Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
            calculator.displayCustomResults(3, 5);
            return true;
        } catch (Exception e) {
            System.err.println("Помилка в тестуванні налаштованої таблиці: " + e.getMessage());
            return false;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<Integer> numbers = new ArrayList<>();
        boolean running = true;

        CommandManager commandManager = CommandManager.getInstance();

        while (running) {
            System.out.println("\n--- Меню ---");
            System.out.println("1 - Згенерувати числа");
            System.out.println("2 - Виконати макрокоманду");
            System.out.println("3 - Виконати тестування");
            System.out.println("4 - Скасувати останню команду");
            System.out.println("5 - Вийти");
            System.out.print("Оберіть дію: ");

            try {
                int action = scanner.nextInt();
                switch (action) {
                    case 1 -> {
                        GenerateNumbersCommand generateCommand = new GenerateNumbersCommand(numbers);
                        commandManager.executeCommand(generateCommand);
                        System.out.println("Згенеровані числа: " + numbers);
                    }
                    case 2 -> {
                        if (numbers.isEmpty()) {
                            System.out.println("Спочатку згенеруйте числа!");
                            continue;
                        }

                        List<Command> macroCommands = Arrays.asList(
                                new GenerateNumbersCommand(numbers)
                        );
                        MacroCommand macroCommand = new MacroCommand(macroCommands, numbers);
                        commandManager.executeCommand(macroCommand);

                        Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
                        calculator.displayCustomResults(3, 5);
                    }
                    case 3 -> {
                        if (numbers.isEmpty()) {
                            System.out.println("Спочатку згенеруйте числа!");
                            continue;
                        }

                        boolean testResult = CalculatorTest.testCustomTableDisplay(numbers);
                        System.out.println("Тест налаштованої таблиці: " + (testResult ? "ПРОЙДЕНО" : "ПРОВАЛЕНО"));
                    }
                    case 4 -> {
                        commandManager.undoLastCommand();
                        System.out.println("Остання команда скасована.");
                    }
                    case 5 -> running = false;
                    default -> System.out.println("Невідома команда");
                }
            } catch (InputMismatchException e) {
                System.out.println("Помилка вводу. Введіть коректне значення.");
                scanner.nextLine();
            }
        }
        scanner.close();
    }
}
