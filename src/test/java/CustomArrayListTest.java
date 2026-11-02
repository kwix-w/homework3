import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import hometask7.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CustomArrayListTest {
    private CustomArrayList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new CustomArrayList<>();
    }

    private void fill(int count) {
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
    }

    @Test
    void newListIsEmpty() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void addOneElement() {
        list.add(42);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
    }

    @Test
    void addSeveralElementsKeepsOrder() {
        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    void addMoreThanTenElementsKeepsData() {
        fill(100);
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, list.get(i));
        }
    }

    @Test
    void getWorksForFirstMiddleAndLast() {
        fill(5);
        assertEquals(0, list.get(0));
        assertEquals(2, list.get(2));
        assertEquals(4, list.get(4));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 5, 6, 1000})
    void getRejectsBadIndex(int index) {
        fill(5);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(index));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 5, 6, 1000})
    void removeRejectsBadIndex(int index) {
        fill(5);
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(index));
        assertEquals(5, list.size());
    }

    @Test
    void getOnEmptyListThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    static Stream<Arguments> removeCases() {
        return Stream.of(
                Arguments.of(0, 0, List.of(1, 2, 3, 4)),
                Arguments.of(2, 2, List.of(0, 1, 3, 4)),
                Arguments.of(4, 4, List.of(0, 1, 2, 3))
        );
    }

    @ParameterizedTest
    @MethodSource("removeCases")
    void removeReturnsValueAndShiftsTail(int index, int expectedValue, List<Integer> expectedRest) {
        fill(5);
        assertEquals(expectedValue, list.remove(index));
        assertEquals(expectedRest.size(), list.size());
        for (int i = 0; i < expectedRest.size(); i++) {
            assertEquals(expectedRest.get(i), list.get(i));
        }
    }

    @Test
    void removedSlotCannotBeReadAndSizeShrinks() {
        fill(3);
        list.remove(2);
        assertEquals(2, list.size());
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(2));
    }

    @Test
    void removeClearsInternalReference() throws Exception {
        fill(3);
        list.remove(0);
        java.lang.reflect.Field field = CustomArrayList.class.getDeclaredField("elements");
        field.setAccessible(true);
        Object[] array = (Object[]) field.get(list);
        assertNull(array[2]);
    }

    @Test
    void addNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> list.add(null));
        assertEquals(0, list.size());
    }

    @Test
    void iteratorTraversesInOrder() {
        fill(15);
        List<Integer> seen = new ArrayList<>();
        for (Integer value : list) {
            seen.add(value);
        }
        assertEquals(15, seen.size());
        for (int i = 0; i < 15; i++) {
            assertEquals(i, seen.get(i));
        }
    }

    @Test
    void finishedIteratorThrowsNoSuchElementException() {
        fill(2);
        Iterator<Integer> iterator = list.iterator();
        iterator.next();
        iterator.next();
        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorOfEmptyListThrowsNoSuchElementException() {
        Iterator<Integer> iterator = list.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    @Test
    void constructorRejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new CustomArrayList<Integer>(0));
    }

    @Test
    void smallInitialCapacityStillGrows() {
        CustomArrayList<Integer> small = new CustomArrayList<>(1);
        for (int i = 0; i < 50; i++) {
            small.add(i);
        }
        assertEquals(50, small.size());
        assertEquals(49, small.get(49));
    }
}



