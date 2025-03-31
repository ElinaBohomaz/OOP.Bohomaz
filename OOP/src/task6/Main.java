package task6;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

class GenerateAndDisplayCommand implements Command {
    private final List<Integer> numbers;
    private final int count;
    private final int bound;
    private final DisplayFactory displayFactory;
    private final int columns;
    private final int cellWidth;

    public GenerateAndDisplayCommand(List<Integer> numbers, int count, int bound,
                                     DisplayFactory displayFactory, int columns, int cellWidth) {
        this.numbers = numbers;
        this.count = count;
        this.bound = bound;
        this.displayFactory = displayFactory;
        this.columns = columns;
        this.cellWidth = cellWidth;
    }

    @Override
    public void execute() {
        numbers.clear();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            numbers.add(random.nextInt(bound));
        }
        System.out.println("Згенеровані числа: " + numbers);

        Calculator calculator = new Calculator(numbers, displayFactory);
        calculator.displayCustomResults(columns, cellWidth);
    }
}

class ParallelProcessCommand implements Command {
    private final List<Integer> numbers;

    public ParallelProcessCommand(List<Integer> numbers) {
        this.numbers = numbers;
    }

    @Override
    public void execute() {
        if (numbers.isEmpty()) {
            System.out.println("Немає чисел для обробки. Спочатку згенеруйте дані.");
            return;
        }

        System.out.println("Виконується паралельна обробка даних...");

        CompletableFuture<Integer> minFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().min(Integer::compare).orElse(0)
        );

        CompletableFuture<Integer> maxFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().max(Integer::compare).orElse(0)
        );

        CompletableFuture<Double> avgFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().mapToInt(Integer::intValue).average().orElse(0.0)
        );

        CompletableFuture<List<Integer>> evenFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().filter(n -> n % 2 == 0).collect(Collectors.toList())
        );

        CompletableFuture<Double> sumFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().mapToInt(Integer::intValue).sum() * 1.0
        );

        CompletableFuture<Map<Boolean, List<Integer>>> partitionFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().collect(Collectors.partitioningBy(n -> n > 50))
        );

        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                minFuture, maxFuture, avgFuture, evenFuture, sumFuture, partitionFuture
        );

        try {
            allDone.get();

            int min = minFuture.get();
            int max = maxFuture.get();
            double avg = avgFuture.get();
            List<Integer> evens = evenFuture.get();
            double sum = sumFuture.get();
            Map<Boolean, List<Integer>> partitioned = partitionFuture.get();

            System.out.println("\nРезультати паралельної обробки:");
            System.out.println("Мінімальне число: " + min);
            System.out.println("Максимальне число: " + max);
            System.out.println("Середнє значення: " + avg);
            System.out.println("Парні числа: " + evens);
            System.out.println("Сума всіх чисел: " + sum);
            System.out.println("Числа більше 50: " + partitioned.get(true));
            System.out.println("Числа менше або рівні 50: " + partitioned.get(false));

            DoubleSummaryStatistics stats = numbers.parallelStream()
                    .mapToDouble(Integer::doubleValue)
                    .summaryStatistics();

            System.out.println("\nСтатистична обробка:");
            System.out.println("Кількість елементів: " + stats.getCount());
            System.out.println("Сума: " + stats.getSum());
            System.out.println("Мінімум: " + stats.getMin());
            System.out.println("Максимум: " + stats.getMax());
            System.out.println("Середнє: " + stats.getAverage());

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Помилка при паралельній обробці: " + e.getMessage());
            Thread.currentThread().interrupt();
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

    public int getSum() {
        return numbers.stream().mapToInt(Integer::intValue).sum();
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

class AutoDataGenerator {
    private final List<Integer> numbers;
    private final int count;
    private final int bound;
    private final int columns;
    private final int cellWidth;
    private final DisplayFactory displayFactory;

    public AutoDataGenerator(List<Integer> numbers, int count, int bound, int columns, int cellWidth) {
        this.numbers = numbers;
        this.count = count;
        this.bound = bound;
        this.columns = columns;
        this.cellWidth = cellWidth;
        this.displayFactory = new ConsoleResultDisplayFactory();
    }

    public void generateAndDisplay() {
        Command generateCommand = new GenerateAndDisplayCommand(
                numbers, count, bound, displayFactory, columns, cellWidth
        );
        generateCommand.execute();
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        boolean running = true;

        CommandManager commandManager = CommandManager.getInstance();

        int defaultCount = 15;
        int defaultBound = 100;
        int defaultColumns = 5;
        int defaultCellWidth = 6;

        AutoDataGenerator autoGenerator = new AutoDataGenerator(
                numbers, defaultCount, defaultBound, defaultColumns, defaultCellWidth
        );


        autoGenerator.generateAndDisplay();

        while (running) {
            System.out.println("\n--- Меню ---");
            System.out.println("1 - Згенерувати нові числа");
            System.out.println("2 - Виконати паралельну обробку даних");
            System.out.println("3 - Вийти");
            System.out.print("Оберіть дію: ");

            try {
                int action = scanner.nextInt();
                switch (action) {
                    case 1 -> {

                        autoGenerator.generateAndDisplay();
                    }
                    case 2 -> {
                        if (numbers.isEmpty()) {
                            System.out.println("Спочатку згенеруйте числа!");
                            continue;
                        }

                        ParallelProcessCommand processCommand = new ParallelProcessCommand(numbers);
                        commandManager.executeCommand(processCommand);
                    }
                    case 3 -> {
                        running = false;
                        commandManager.shutdown();
                        System.out.println("Програма завершується...");
                    }
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