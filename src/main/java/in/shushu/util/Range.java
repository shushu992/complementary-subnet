package in.shushu.util;

import in.shushu.exception.ComplementarySubnetException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Range {

    public final long start;
    public final long end;

    private Range(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public static Range fromCIDR(String cidr) {
        Pattern pattern = Pattern.compile("^\\d+.\\d+.\\d+.\\d+/\\d+$");
        boolean valid = pattern.matcher(cidr).matches();
        if (!valid) {
            throw new ComplementarySubnetException("Invalid CIDR: %s".formatted(cidr));
        }

        long[] segments = new long[]{0, 0, 0, 0, 0};
        int i = 0;

        for (char c : cidr.toCharArray()) {
            if (c == '.' || c == '/') {
                i++;
                continue;
            }

            segments[i] *= 10;
            segments[i] += c - '0';
        }

        if (segments[0] > 255 || segments[1] > 255 || segments[2] > 255 || segments[3] > 255) {
            throw new ComplementarySubnetException("Invalid CIDR: %s".formatted(cidr));
        }

        if (segments[4] > 32) {
            throw new ComplementarySubnetException("Invalid CIDR: %s".formatted(cidr));
        }

        long ip = segments[0] * 0x1000000
                + segments[1] * 0x10000
                + segments[2] * 0x100
                + segments[3];

        int mask = 0;
        for (int j = 1; j <= segments[4]; j++) {
            mask |= 1 << 32 - j;
        }

        long start = ip & mask;
        long end = start + ~mask;

        return new Range(start, end);
    }

    public static Range Entire() {
        return new Range(0x01000000L, 0xffffffffL);
    }

    public static Range Manual(long start, long end) {
        if (start < 0 || start > 0xffffffffL) {
            throw new ComplementarySubnetException("Invalid IP Address: %s".formatted(start));
        }

        if (end < 0 || end > 0xffffffffL) {
            throw new ComplementarySubnetException("Invalid IP Address: %s".formatted(end));
        }

        if (start > end) {
            throw new ComplementarySubnetException("Invalid IP Range: %s - %s".formatted(start, end));
        }

        return new Range(start, end);
    }

    public Relation compare(Range other) {
        if (this.end < other.start) {
            return Relation.BeforeSeparated;
        }

        if (other.end < this.start) {
            return Relation.AfterSeparated;
        }

        if (other.start == this.start && other.end == this.end) {
            return Relation.OVERLAP;
        }

        if (other.start < this.start && this.end < other.end) {
            return Relation.Contained;
        }

        if (this.start < other.start && other.end < this.end) {
            return Relation.Containing;
        }

        if (this.start <= other.start && this.end <= other.end) {
            return Relation.BeforeIntersecting;
        }

        if (other.start <= this.start && other.end <= this.end) {
            return Relation.AfterIntersecting;
        }

        throw new ComplementarySubnetException("Some thing went wrong"); // Impossible
    }

    public List<String> cidr() {
        ArrayList<String> result = new ArrayList<>();

        long start = this.start;
        long end = this.end;

        while (start < end) {
            for (int i = 0; i < 32; i++) {
                if (start + (1 << i) > end) {
                    String s = "%d.%d.%d.%d/%d".formatted(
                            (start & 0xff000000L) >> 24,
                            (start & 0xff0000L) >> 16,
                            (start & 0xff00L) >> 8,
                            (start & 0xffL),
                            32 - i
                    );
                    result.add(s);

                    start++;
                    break;
                }
                start += 1L << i;
            }
        }

        return result;
    }
}
