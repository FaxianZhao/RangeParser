package my;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 * @author faxianzhao <faxianzhao@gmail.com>
 * Created on 2018-10-17
 */
class RangeCombinerTest {

    RangeSet<Integer> getActualResult(String message) {
        RangeLexer lexer = new RangeLexer(CharStreams.fromString(message));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        RangeParser parser = new RangeParser(tokens);

        IntRangeCombiner visitor = new IntRangeCombiner();

        RangeSet<Integer> ret = visitor.visit(parser.prog());

        return ret;
    }

    @Test
    void test1() {
        String message = "1 or 2";
        RangeSet<Integer> actual = getActualResult(message);

        ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
        builder.add(Range.closed(1, 1));
        builder.add(Range.closed(2, 2));
        RangeSet<Integer> expected = builder.build();

        assertEquals(expected, actual);
    }

    @Test
    void test2() {
        String message = "((1,3) or 5) && [2,4]";
        RangeSet<Integer> actual = getActualResult(message);

        RangeSet<Integer> expected = ImmutableRangeSet.of(Range.closedOpen(2, 3));

        assertEquals(expected, actual);
    }

    @Test
    void test3() {
        String message = "!(1 or !2)";
        RangeSet<Integer> actual = getActualResult(message);

        ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
        builder.add(Range.closed(2,2));
        RangeSet<Integer> expected = builder.build();

        assertEquals(expected, actual);
    }

    @Test
    void test4() {
        String message = "!1 or !2";
        RangeSet<Integer> actual = getActualResult(message);

        ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
        builder.add(Range.all());
        RangeSet<Integer> expected = builder.build();

        assertEquals(expected, actual);
    }

    @Test
    void test5() {
        String message = "!-1";
        RangeSet<Integer> actual = getActualResult(message);

        ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
        builder.add(Range.lessThan(-1));
        builder.add(Range.greaterThan(-1));
        RangeSet<Integer> expected = builder.build();

        assertEquals(expected, actual);
    }

    @Test
    void test6() {
        String message = "< -8 or >=10";
        RangeSet<Integer> actual = getActualResult(message);

        ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
        builder.add(Range.lessThan(-8));
        builder.add(Range.atLeast(10));
        RangeSet<Integer> expected = builder.build();

        assertEquals(expected, actual);
    }

    @Test
    void test7() {
        String message = "=1 and =2";
        RangeSet<Integer> actual = getActualResult(message);

        RangeSet<Integer> expected = ImmutableRangeSet.of();
        assertEquals(expected, actual);
    }

}