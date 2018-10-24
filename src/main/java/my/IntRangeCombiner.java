package my;

/**
 * @author faxianzhao <faxianzhao@gmail.com>
 * Created on 2018-10-16
 */
public class IntRangeCombiner extends RangeCombiner<Integer> {

    protected Integer newInstance(String text) {
        return Integer.parseInt(text);
    }

    @Override
    protected Integer empty() {
        return 0;
    }

    @Override
    protected Integer negative(Integer integer) {
        return -integer;
    }

}
