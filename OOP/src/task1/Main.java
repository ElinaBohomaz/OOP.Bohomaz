package task1;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Готова до практичної роботи з ООП");
            return;
        }

        System.out.println("Аргументи командного рядка:");
        for (String argument : args) {
            System.out.println(" - " + argument);
        }
    }
}