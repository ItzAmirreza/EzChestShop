package me.deadlight.ezchestshop.Utils;

public class Pair<S, T> {


    private final S obj1;
    private final T obj2;

    public Pair(S obj1, T obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public static <S, T> Pair<S, T> of(S obj1, T obj2) {
        return new Pair<>(obj1, obj2);
    }


    public S getFirst() {
        return obj1;
    }

    public T getSecond() {
        return obj2;
    }
}
