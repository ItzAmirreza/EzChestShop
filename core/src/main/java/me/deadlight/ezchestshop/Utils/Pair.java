package me.deadlight.ezchestshop.Utils;

public class Pair<S, T> {


    private S obj1;
    private T obj2;

    public Pair(S obj1, T obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public static <S, T> Pair of(S obj1, T obj2) {
        return new Pair(obj1, obj2);
    }


    public S getFirst() {
        return obj1;
    }

    public T getSecond() {
        return obj2;
    }
}
