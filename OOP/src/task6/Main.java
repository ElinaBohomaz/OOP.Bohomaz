package task6;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Інтерфейс для відображення результатів
 * Визначає метод для показу даних у табличному форматі
 */
interface ResultDisplay {
    void displayCustomTable(Data data, int columns, int cellWidth);
}

/**
 * Інтерфейс фабрики для створення об'єктів відображення
 * Частина шаблону проектування "Фабричний метод"
 */
interface DisplayFactory {
    ResultDisplay createDisplay();
}

/**
 * Абстрактний клас, що реалізує інтерфейс фабрики
 * Використовується як базовий клас для конкретних фабрик
 */
abstract class ResultDisplayFactory implements DisplayFactory {
    @Override
    public abstract ResultDisplay createDisplay();
}

/**
 * Реалізація відображення результатів для консолі
 * Відповідає за форматування та виведення даних у консоль
 */
class ConsoleResultDisplay implements ResultDisplay {
    @Override
    public void displayCustomTable(Data data, int columns, int cellWidth) {
        System.out.println("Налаштована таблиця:");
        List<Integer> numbers = data.getNumbers();

        printHorizontalLine(columns, cellWidth);

        for (int i = 0; i < numbers.size(); i++) {
            // Форматування даних з вирівнюванням по правому краю в клітинці
            System.out.printf("| %" + (cellWidth - 2) + "d ", numbers.get(i));

            // Перехід на новий рядок після заповнення рядка або в кінці списку
            if ((i + 1) % columns == 0 || i == numbers.size() - 1) {
                System.out.println("|");
                printHorizontalLine(columns, cellWidth);
            }
        }
    }

    /**
     * Допоміжний метод для друку горизонтальної лінії таблиці
     * @param columns кількість стовпців
     * @param cellWidth ширина клітинки
     */
    private void printHorizontalLine(int columns, int cellWidth) {
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < cellWidth; j++) {
                System.out.print("-");
            }
        }
        System.out.println();
    }
}

/**
 * Фабрика для створення об'єктів ConsoleResultDisplay
 * Конкретна реалізація фабричного методу
 */
class ConsoleResultDisplayFactory extends ResultDisplayFactory {
    @Override
    public ResultDisplay createDisplay() {
        return new ConsoleResultDisplay();
    }
}

/**
 * Інтерфейс команди
 * Частина шаблону проектування "Команда"
 */
interface Command {
    void execute();
}

/**
 * Менеджер команд
 * Реалізує шаблони "Одинак" та "Команда" з використанням паралельного виконання
 * Використовує чергу команд і пул потоків для асинхронного виконання
 */
class CommandManager {
    private static CommandManager instance;
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;

    /**
     * Приватний конструктор для шаблону "Одинак"
     * Ініціалізує пул потоків та запускає обробку команд
     */
    private CommandManager() {
        // Створення пулу потоків з 2 потоками
        executor = Executors.newFixedThreadPool(2);

        // Запуск обробника команд у окремому потоці
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Очікування та виконання команд з черги
                    Command command = commandQueue.take();
                    command.execute();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Отримання єдиного екземпляра менеджера команд (шаблон "Одинак")
     * @return екземпляр CommandManager
     */
    public static synchronized CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    /**
     * Додати команду до черги для асинхронного виконання
     * @param command команда для виконання
     */
    public void executeCommand(Command command) {
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Переривання при додаванні команди до черги: " + e.getMessage());
        }
    }

    /**
     * Завершення роботи пулу потоків
     * Викликається при закінченні роботи програми
     */
    public void shutdown() {
        executor.shutdownNow();
        try {
            // Очікування завершення всіх потоків протягом 5 секунд
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Пул потоків не завершив роботу коректно.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Команда для генерації випадкових чисел та їх відображення
 * Реалізує шаблон "Команда"
 */
class GenerateAndDisplayCommand implements Command {
    private final List<Integer> numbers;
    private final int count;
    private final int bound;
    private final DisplayFactory displayFactory;
    private final int columns;
    private final int cellWidth;

    /**
     * Конструктор команди генерації та відображення
     * @param numbers список для зберігання згенерованих чисел
     * @param count кількість чисел для генерації
     * @param bound верхня межа для генерації випадкових чисел
     * @param displayFactory фабрика для створення відображення
     * @param columns кількість стовпців для відображення
     * @param cellWidth ширина клітинки для відображення
     */
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
        // Очищення попередніх даних
        numbers.clear();
        Random random = new Random();

        // Генерація нових випадкових чисел
        for (int i = 0; i < count; i++) {
            numbers.add(random.nextInt(bound));
        }
        System.out.println("Згенеровані числа: " + numbers);

        // Відображення згенерованих чисел у вигляді таблиці
        Calculator calculator = new Calculator(numbers, displayFactory);
        calculator.displayCustomResults(columns, cellWidth);
    }
}

/**
 * Команда для паралельної обробки даних з використанням CompletableFuture
 * Демонструє використання сучасних можливостей Java для конкурентного програмування
 */
class ParallelProcessCommand implements Command {
    private final List<Integer> numbers;

    /**
     * Конструктор команди паралельної обробки
     * @param numbers список чисел для обробки
     */
    public ParallelProcessCommand(List<Integer> numbers) {
        this.numbers = numbers;
    }

    @Override
    public void execute() {
        // Перевірка наявності даних
        if (numbers.isEmpty()) {
            System.out.println("Немає чисел для обробки. Спочатку згенеруйте дані.");
            return;
        }

        System.out.println("Виконується паралельна обробка даних...");

        // Створення асинхронних завдань для різних операцій обробки даних

        // Знаходження мінімального значення
        CompletableFuture<Integer> minFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().min(Integer::compare).orElse(0)
        );

        // Знаходження максимального значення
        CompletableFuture<Integer> maxFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().max(Integer::compare).orElse(0)
        );

        // Обчислення середнього значення
        CompletableFuture<Double> avgFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().mapToInt(Integer::intValue).average().orElse(0.0)
        );

        // Фільтрація парних чисел
        CompletableFuture<List<Integer>> evenFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().filter(n -> n % 2 == 0).collect(Collectors.toList())
        );

        // Обчислення суми всіх чисел
        CompletableFuture<Double> sumFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().mapToInt(Integer::intValue).sum() * 1.0
        );

        // Розділення чисел на групи за умовою (більше 50 і менше або рівні 50)
        CompletableFuture<Map<Boolean, List<Integer>>> partitionFuture = CompletableFuture.supplyAsync(() ->
                numbers.parallelStream().collect(Collectors.partitioningBy(n -> n > 50))
        );

        // Об'єднання всіх асинхронних завдань
        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                minFuture, maxFuture, avgFuture, evenFuture, sumFuture, partitionFuture
        );

        try {
            // Очікування завершення всіх завдань
            allDone.get();

            // Отримання результатів обчислень
            int min = minFuture.get();
            int max = maxFuture.get();
            double avg = avgFuture.get();
            List<Integer> evens = evenFuture.get();
            double sum = sumFuture.get();
            Map<Boolean, List<Integer>> partitioned = partitionFuture.get();

            // Виведення результатів обробки
            System.out.println("\nРезультати паралельної обробки:");
            System.out.println("Мінімальне число: " + min);
            System.out.println("Максимальне число: " + max);
            System.out.println("Середнє значення: " + avg);
            System.out.println("Парні числа: " + evens);
            System.out.println("Сума всіх чисел: " + sum);
            System.out.println("Числа більше 50: " + partitioned.get(true));
            System.out.println("Числа менше або рівні 50: " + partitioned.get(false));

            // Додаткове отримання статистики за допомогою SummaryStatistics
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

/**
 * Клас для зберігання та обробки даних
 */
class Data {
    private final List<Integer> numbers;

    /**
     * Конструктор класу даних
     * @param numbers початковий список чисел
     */
    public Data(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }

    /**
     * Отримати копію списку чисел
     * @return новий список з тими ж числами
     */
    public List<Integer> getNumbers() {
        return new ArrayList<>(numbers);
    }

    /**
     * Обчислити суму всіх чисел
     * @return сума чисел
     */
    public int getSum() {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }
}

/**
 * Клас калькулятора для роботи з даними
 */
class Calculator {
    private final Data data;
    private final ResultDisplay display;

    /**
     * Конструктор калькулятора
     * @param numbers список чисел
     * @param factory фабрика для створення інтерфейсу відображення
     */
    public Calculator(List<Integer> numbers, DisplayFactory factory) {
        this.data = new Data(numbers);
        this.display = factory.createDisplay();
    }

    /**
     * Відобразити дані у вигляді таблиці з заданими параметрами
     * @param columns кількість стовпців
     * @param cellWidth ширина клітинки
     */
    public void displayCustomResults(int columns, int cellWidth) {
        display.displayCustomTable(data, columns, cellWidth);
    }
}

/**
 * Клас для автоматичної генерації та відображення даних
 * Інкапсулює логіку створення та виконання команди генерації
 */
class AutoDataGenerator {
    private final List<Integer> numbers;
    private final int count;
    private final int bound;
    private final int columns;
    private final int cellWidth;
    private final DisplayFactory displayFactory;

    /**
     * Конструктор генератора даних
     * @param numbers список для зберігання згенерованих чисел
     * @param count кількість чисел для генерації
     * @param bound верхня межа для генерації випадкових чисел
     * @param columns кількість стовпців для відображення
     * @param cellWidth ширина клітинки для відображення
     */
    public AutoDataGenerator(List<Integer> numbers, int count, int bound, int columns, int cellWidth) {
        this.numbers = numbers;
        this.count = count;
        this.bound = bound;
        this.columns = columns;
        this.cellWidth = cellWidth;
        this.displayFactory = new ConsoleResultDisplayFactory();
    }

    /**
     * Генерація та відображення даних
     * Створює і виконує команду генерації
     */
    public void generateAndDisplay() {
        Command generateCommand = new GenerateAndDisplayCommand(
                numbers, count, bound, displayFactory, columns, cellWidth
        );
        generateCommand.execute();
    }
}

/**
 * Головний клас програми
 */
public class Main {
    /**
     * Точка входу в програму
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        boolean running = true;

        // Отримання єдиного екземпляра менеджера команд
        CommandManager commandManager = CommandManager.getInstance();

        // Встановлення параметрів за замовчуванням
        int defaultCount = 15;
        int defaultBound = 100;
        int defaultColumns = 5;
        int defaultCellWidth = 6;

        // Створення об'єкта для автоматичної генерації даних
        AutoDataGenerator autoGenerator = new AutoDataGenerator(
                numbers, defaultCount, defaultBound, defaultColumns, defaultCellWidth
        );

        // Початкова генерація даних при запуску програми
        autoGenerator.generateAndDisplay();

        // Головний цикл програми
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
                        // Генерація нових чисел
                        autoGenerator.generateAndDisplay();
                    }
                    case 2 -> {
                        // Перевірка наявності даних перед обробкою
                        if (numbers.isEmpty()) {
                            System.out.println("Спочатку згенеруйте числа!");
                            continue;
                        }

                        // Створення та додавання команди паралельної обробки до черги
                        ParallelProcessCommand processCommand = new ParallelProcessCommand(numbers);
                        commandManager.executeCommand(processCommand);
                    }
                    case 3 -> {
                        // Завершення роботи програми
                        running = false;
                        commandManager.shutdown();
                        System.out.println("Програма завершується...");
                    }
                    default -> System.out.println("Невідома команда");
                }
            } catch (InputMismatchException e) {
                // Обробка некоректного вводу
                System.out.println("Помилка вводу. Введіть коректне значення.");
                scanner.nextLine();
            }
        }
        scanner.close();
    }
}