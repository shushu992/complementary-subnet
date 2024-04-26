package in.shushu;

import in.shushu.exception.ComplementarySubnetException;
import in.shushu.util.Range;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Calculator {

    public Calculator() {
    }

    public List<String> calc() {
        List<Range> breakpoints = this.parse();
        List<Range> complementary = this.complement(breakpoints);
        return this.cidr(complementary);
    }

    private List<Range> parse() throws ComplementarySubnetException {
        File file = new File("input.txt");

        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new ComplementarySubnetException("File Not Found");
        }

        List<Range> breakpoints = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Range range = Range.fromCIDR(line);
            breakpoints.add(range);
        }

        scanner.close();

        breakpoints.sort(Comparator.comparingLong(a -> a.start));
        return breakpoints;
    }

    private List<Range> complement(List<Range> breakpoints) {
        List<Range> complementary = new ArrayList<>();
        complementary.add(Range.Entire());

        for (Range b : breakpoints) {
            for (ListIterator<Range> iterator = complementary.listIterator();
                 iterator.hasNext(); ) {
                Range c = iterator.next();

                switch (b.compare(c)) {
                    case BeforeSeparated,
                         AfterSeparated,
                         OVERLAP -> {
                    }
                    case Containing -> {
                        iterator.remove();
                    }
                    case Contained -> {
                        iterator.remove();
                        iterator.add(Range.Manual(c.start, b.start - 1));
                        iterator.add(Range.Manual(b.end + 1, c.end));
                    }
                    case BeforeIntersecting -> {
                        iterator.remove();
                        iterator.add(Range.Manual(b.end + 1, c.end));
                    }
                    case AfterIntersecting -> {
                        iterator.remove();
                        iterator.add(Range.Manual(c.start, b.start - 1));
                    }
                }
            }
        }

        return complementary;
    }

    private List<String> cidr(List<Range> complementary) {
        ArrayList<String> result = new ArrayList<>();

        for (Range c : complementary) {
            result.addAll(c.cidr());
        }

        return result;
    }
}
