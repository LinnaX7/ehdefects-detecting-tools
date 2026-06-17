package fixeh.instrument.woventools;

public interface Counter {
    public int getCount(String signature);
    public int increase(String signature);
    public void reset();

}
