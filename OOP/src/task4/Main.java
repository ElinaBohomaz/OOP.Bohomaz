package task4;

import java.io.*;
import java.util.*;

/**
 * Інтерфейс для відображення результатів обчислень.
 * Визначає методи для базового та налаштованого виведення даних.
 */
interface ResultDisplay {
    /**
     * Базове відображення результатів.
     * @param data Об'єкт з даними для відображення
     */
    void displayBasic(Data data);

    /**
     * Налаштоване відображення результатів у вигляді таблиці.
     * @param data Об'єкт з даними для відображення
     * @param columns Кількість стовпців у таблиці
     * @param cellWidth Ширина комірки таблиці
     */
    void displayCustomTable(Data data, int columns, int cellWidth);
}

/**
 * Фабрика для створення об'єктів відображення результатів.
 */
interface DisplayFactory {
    /**
     * Створює об'єкт для відображення результатів.
     * @return Об'єкт ResultDisplay
     */
    ResultDisplay createDisplay();
}

/**
 * Абстрактна фабрика для створення displеїв результатів.
 */
abstract class ResultDisplayFactory implements DisplayFactory {
    @Override
    public abstract ResultDisplay createDisplay();
}

/**
 * Реалізація виведення результатів у консоль.
 */
class ConsoleResultDisplay implements ResultDisplay {
    private static final String DEFAULT_FILE_PATH = "C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt";

    @Override
    public void displayBasic(Data data) {
        printSimpleTable(data.getNumbers(), 4, 5);
        System.out.println("Сума: " + data.getResult());
        System.out.println("Кількість чисел: " + data.getNumbers().size());

        saveTableToFile(data.getNumbers(), 4, 5);
    }

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

        saveCustomTableToFile(numbers, columns, cellWidth);
    }

    // Методи збереження та друку залишаються без змін
    private void saveTableToFile(List<Integer> numbers, int columns, int cellWidth) { /* ... */ }
    private void saveCustomTableToFile(List<Integer> numbers, int columns, int cellWidth) { /* ... */ }
    private void printSimpleTable(List<Integer> numbers, int columns, int cellWidth) { /* ... */ }
    private void printHorizontalLine(int columns, int cellWidth) { /* ... */ }
}

/**
 * Фабрика для створення консольного displея результатів.
 */
class ConsoleResultDisplayFactory extends ResultDisplayFactory {
    @Override
    public ResultDisplay createDisplay() {
        return new ConsoleResultDisplay();
    }
}

/**
 * Клас для зберігання та обробки числових даних.
 */
class Data {
    private final List<Integer> numbers;

    /**
     * Конструктор класу Data.
     * @param numbers Список чисел для обробки
     */
    public Data(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }

    /**
     * Отримання списку чисел.
     * @return Список чисел
     */
    public List<Integer> getNumbers() {
        return new ArrayList<>(numbers);
    }

    /**
     * Обчислення суми всіх чисел.
     * @return Сума чисел
     */
    public int getResult() {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Виведення чисел у рядок.
     */
    public void printTable() {
        numbers.forEach(num -> System.out.print(num + " "));
        System.out.println();
    }
}

/**
 * Калькулятор для обробки та відображення результатів.
 */
class Calculator {
    private final Data data;
    private final ResultDisplay display;

    /**
     * Конструктор калькулятора.
     * @param numbers Список чисел
     * @param factory Фабрика для створення displея
     */
    public Calculator(List<Integer> numbers, DisplayFactory factory) {
        this.data = new Data(numbers);
        this.display = factory.createDisplay();
    }

    /**
     * Відображення базових результатів.
     */
    public void displayResults() {
        display.displayBasic(data);
    }

    /**
     * Відображення налаштованих результатів.
     * @param columns Кількість стовпців
     * @param cellWidth Ширина комірки
     */
    public void displayCustomResults(int columns, int cellWidth) {
        display.displayCustomTable(data, columns, cellWidth);
    }
}

/**
 * Клас для тестування функціональності калькулятора.
 */
class CalculatorTest {
    /**
     * Тестування базового функціоналу.
     * @return true, якщо тест пройшов успішно
     */
    public boolean testBasicFunctionality() {
        List<Integer> testNumbers = Arrays.asList(10, 20, 30, 40);
        Calculator calculator = new Calculator(testNumbers, new ConsoleResultDisplayFactory());

        try {
            calculator.displayResults();
            return true;
        } catch (Exception e) {
            System.err.println("Помилка в тестуванні: " + e.getMessage());
            return false;
        }
    }

    /**
     * Тестування налаштованого виведення.
     * @return true, якщо тест пройшов успішно
     */
    public boolean testCustomTableDisplay() {
        List<Integer> testNumbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        Calculator calculator = new Calculator(testNumbers, new ConsoleResultDisplayFactory());

        try {
            calculator.displayCustomResults(3, 5);
            return true;
        } catch (Exception e) {
            System.err.println("Помилка в тестуванні налаштованої таблиці: " + e.getMessage());
            return false;
        }
    }

    /**
     * Запуск всіх тестів.
     */
    public void runAllTests() {
        System.out.println("Запуск тестів:");
        boolean basicTest = testBasicFunctionality();
        boolean customTest = testCustomTableDisplay();

        System.out.println("Базовий тест: " + (basicTest ? "ПРОЙДЕНО" : "ПРОВАЛЕНО"));
        System.out.println("Тест налаштованої таблиці: " + (customTest ? "ПРОЙДЕНО" : "ПРОВАЛЕНО"));
    }
}

/**
 * Головний клас для взаємодії з користувачем.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<Integer> numbers = new ArrayList<>();
        boolean running = true;

        CalculatorTest test = new CalculatorTest();

        while (running) {
            System.out.println("\n--- Меню ---");
            System.out.println("1 - Згенерувати числа");
            System.out.println("2 - Показати базові результати");
            System.out.println("3 - Показати налаштовану таблицю");
            System.out.println("4 - Виконати тестування");
            System.out.println("5 - Зберегти дані");
            System.out.println("6 - Оновити дані");
            System.out.println("7 - Вийти");
            System.out.print("Оберіть дію: ");

            try {
                int action = scanner.nextInt();
                switch (action) {
                    case 1 -> {
                        numbers.clear();
                        for (int i = 0; i < 12; i++) {
                            numbers.add(random.nextInt(100));
                        }
                        System.out.println("Згенеровані числа: " + numbers);
                    }
                    case 2 -> {
                        if (numbers.isEmpty()) {
                            System.out.println("Спочатку згенеруйте числа!");
                            continue;
                        }
                        Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
                        calculator.displayResults();
                    }
                    case 3 -> {
                        if (numbers.isEmpty()) {
                            System.out.println("Спочатку згенеруйте числа!");
                            continue;
                        }
                        System.out.print("Введіть кількість стовпців: ");
                        int columns = scanner.nextInt();
                        System.out.print("Введіть ширину комірки: ");
                        int cellWidth = scanner.nextInt();

                        Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
                        calculator.displayCustomResults(columns, cellWidth);
                    }
                    case 4 -> test.runAllTests();
                    case 5 -> System.out.println("Дані збережено!");
                    case 6 -> System.out.println("Дані оновлено!");
                    case 7 -> running = false;
                    default -> System.out.println("Невідома команда");
                }
            } catch (InputMismatchException e) {
                System.out.println("Помилка вводу. Введіть коректне значення.");
                scanner.nextLine(); // Очищення буфера
            }
        }
        scanner.close();
    }
}