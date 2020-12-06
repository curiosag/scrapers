package util;

public class Tuple<T> {
    public final T left;
    public final T right;

    public Tuple(T left, T right) {
        this.left = left;
        this.right = right;
    }

    public static <T> Tuple<T> of(T left, T right) {
        return new Tuple<>(left, right);
    }
}
