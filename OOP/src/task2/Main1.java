package task2;

import java.io.*;
import java.util.*;

class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Double> numbers;
    private double result;

    public Data(List<Double> numbers) {
        this.numbers = new ArrayList<>(numbers);
        this.result = calculateSum(numbers);
    }

    private double calculateSum(List<Double> numbers) {
        return numbers.stream().mapToDouble(Double::doubleValue).sum();
    }

    public List<Double> getNumbers() { return Collections.unmodifiableList(numbers); }
    public double getResult() { return result; }

    @Override
    public String toString() {
        return "Числа- " + numbers + ", Сума- " + result;
    }
}

class Calculator {
    private Data data;

    public Calculator(List<Double> numbers) {
        this.data = new Data(numbers);
    }

    public Data getData() {
        return data;
    }

    public void saveData(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Числа: " + data.getNumbers());
            writer.newLine();
            writer.write("Сума: " + data.getResult());
            System.out.println("Дані успішно збережено до файлу: " + filename);
        } catch (IOException e) {
            System.err.println("Помилка збереження даних: " + e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename + ".ser"))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Помилка серіалізації: " + e.getMessage());
        }
    }

    public void loadData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename + ".ser"))) {
            this.data = (Data) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Помилка завантаження даних: " + e.getMessage());
        }
    }
}

public class Main1  {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            List<Double> numbers = new ArrayList<>();

            System.out.println("Введіть числа для обчислення суми");
            System.out.println("Для завершення введення натисніть 0:");

            while (true) {
                System.out.print("Введіть число: ");

                while (!scanner.hasNextDouble()) {
                    System.out.println("Некоректне введення");
                    scanner.next();
                }

                double number = scanner.nextDouble();

                if (number == 0) break;
                numbers.add(number);
            }

            if (numbers.isEmpty()) {
                System.out.println("Жодного числа не введено.");
                return;
            }

            Calculator calculator = new Calculator(numbers);
            Data result = calculator.getData();

            System.out.println(result);

            String filename = "C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt";
            calculator.saveData(filename);
            calculator.loadData(filename);

            result = calculator.getData();
            System.out.println("Дані після повторного завантаження: " + result);
        } catch (Exception e) {
            System.err.println("Сталася помилка: " + e.getMessage());
        }
    }
}