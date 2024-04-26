package in.shushu;

import in.shushu.exception.ComplementarySubnetException;

public class Main {

    public static void main(String[] args) {
        try {
            new Calculator()
                    .calc()
                    .forEach(System.out::println);
        } catch (ComplementarySubnetException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
