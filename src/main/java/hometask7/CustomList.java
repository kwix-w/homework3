package hometask7;

public interface CustomList<E> extends Iterable<E> {
    void add(E element);

    E get(int index);

    E remove(int index);

    int size();

    boolean isEmpty();
}
