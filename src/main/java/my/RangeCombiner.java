package my;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Token;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * @author faxianzhao <faxianzhao@gmail.com>
 * Created on 2018-10-16
 */
public abstract class RangeCombiner<T extends Comparable> extends RangeBaseVisitor<RangeSet<T>> {

    protected abstract T newInstance(String text);

    protected abstract T empty();

    protected abstract T negative(T t);

    @Override
    public RangeSet<T> visitStat(RangeParser.StatContext ctx) {
        return visit(ctx.expr());
    }

    private RangeSet<T> intersection(RangeSet<T> left, RangeSet<T> right) {
        TreeRangeSet rangeSet = TreeRangeSet.create();
        for (Range<T> l : left.asRanges()) {
            for (Range<T> r : right.asRanges()) {
                if (l.isConnected(r)) {
                    rangeSet.add(l.intersection(r));
                }
            }
        }
        return rangeSet;
    }

    @Override
    public RangeSet<T> visitAndOrExpr(RangeParser.AndOrExprContext ctx) {
        RangeSet<T> left = visit(ctx.expr(0));
        RangeSet<T> right = visit(ctx.expr(1));
        Token op = ctx.op;
        if (op.getType() == RangeParser.AND) {
            if (left.isEmpty()) {
                return left;
            }
            if (right.isEmpty()) {
                return right;
            }
            return intersection(left, right);
        } else {
            TreeRangeSet rangeSet = TreeRangeSet.create();
            Stream.concat(left.asRanges().stream(), right.asRanges().stream())
                    .forEach(r -> rangeSet.add(r));
            return rangeSet;
        }
    }

    @Override
    public RangeSet<T> visitComparLit(RangeParser.ComparLitContext ctx) {
        T c = newInstance(ctx.COMPARABLE().getText());
        return ImmutableRangeSet.of(Range.closed(c, c));
    }

    @Override
    public RangeSet<T> visitParenExpr(RangeParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public RangeSet<T> visitNonExpr(RangeParser.NonExprContext ctx) {
        List<RangeSet<T>> rangeSetList = new ArrayList<>();

        for (Range<T> range : visit(ctx.expr()).asRanges()) {
            TreeRangeSet rangeSet = TreeRangeSet.create();
            if (range.isEmpty()) {
                rangeSet.add(Range.all());
            }
            if (range.equals(Range.all())) {
                rangeSet.add(Range.open(empty(), empty()));
            }
            if (range.hasUpperBound()) {
                BoundType boundType = range.upperBoundType();
                if (boundType.equals(BoundType.CLOSED)) {
                    rangeSet.add(Range.greaterThan(range.upperEndpoint()));
                } else {
                    rangeSet.add(Range.atLeast(range.upperEndpoint()));
                }
            }
            if (range.hasLowerBound()) {
                BoundType boundType = range.lowerBoundType();
                if (boundType.equals(BoundType.CLOSED)) {
                    rangeSet.add(Range.lessThan(range.lowerEndpoint()));
                } else {
                    rangeSet.add(Range.atMost(range.lowerEndpoint()));
                }
            }

            rangeSetList.add(rangeSet);
        }
        return rangeSetList.stream().reduce(this::intersection).orElse(ImmutableRangeSet.of());
    }

    @Override
    public RangeSet<T> visitSelfExpr(RangeParser.SelfExprContext ctx) {

        T c = newInstance(ctx.COMPARABLE().getText());

        int type = ctx.signal.getType();
        if (RangeParser.EQUAL == type) {
            return ImmutableRangeSet.of(Range.closed(c, c));
        } else if (RangeParser.GEUQAL == type) {
            return ImmutableRangeSet.of(Range.atLeast(c));
        } else if (RangeParser.LEQUAL == type) {
            return ImmutableRangeSet.of(Range.atMost(c));
        } else if (RangeParser.GREATER == type) {
            return ImmutableRangeSet.of(Range.greaterThan(c));
        } else if (RangeParser.LESS == type) {
            return ImmutableRangeSet.of(Range.lessThan(c));
        }
        return super.visitSelfExpr(ctx);
    }

    @Override
    public RangeSet<T> visitBracksExpr(RangeParser.BracksExprContext ctx) {
        T left = newInstance(ctx.COMPARABLE(0).getText());
        T right = newInstance(ctx.COMPARABLE(1).getText());
        BoundType leftBound = BoundType.CLOSED;
        BoundType rightBound = BoundType.CLOSED;

        if (RangeParser.LOPEN == ctx.left.getType()) {
            leftBound = BoundType.OPEN;
        }

        if (RangeParser.ROPEN == ctx.right.getType()) {
            rightBound = BoundType.OPEN;
        }

        return ImmutableRangeSet.of(Range.range(left, leftBound, right, rightBound));
    }
}
