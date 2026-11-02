import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import hometask7.Student;
import hometask7.StudentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StudentRegistryTest {
    private Student anna;
    private Student boris;
    private Student vera;
    private Student gleb;
    private List<Student> all;
    private Map<Integer, Student> hashMap;
    private NavigableMap<Integer, Student> treeMap;

    @BeforeEach
    void setUp() {
        anna = new Student(1, "Анна", 5.0);
        boris = new Student(2, "Борис", 7.5);
        vera = new Student(3, "Вера", 10.0);
        gleb = new Student(4, "Глеб", 0.0);
        all = List.of(anna, boris, vera, gleb);
        hashMap = StudentRegistry.createHashMap(all);
        treeMap = StudentRegistry.createDescendingTreeMap(all);
    }

    @Test
    void studentRejectsNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () -> new Student(0, "Анна", 5.0));
        assertThrows(IllegalArgumentException.class, () -> new Student(-1, "Анна", 5.0));
    }

    @ParameterizedTest
    @CsvSource({"''", "'   '"})
    void studentRejectsBlankName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Student(1, name, 5.0));
    }

    @Test
    void studentRejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Student(1, null, 5.0));
    }

    @ParameterizedTest
    @CsvSource({"-0.1", "10.1", "NaN"})
    void studentRejectsBadGrade(double grade) {
        assertThrows(IllegalArgumentException.class, () -> new Student(1, "Анна", grade));
    }

    @Test
    void studentAcceptsGradeBounds() {
        assertEquals(0.0, new Student(1, "Анна", 0.0).grade());
        assertEquals(10.0, new Student(1, "Анна", 10.0).grade());
    }

    @Test
    void equalsAndHashCodeAreConsistent() {
        Student first = new Student(1, "Анна", 5.0);
        Student second = new Student(1, "Анна", 5.0);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(!first.equals(new Student(2, "Анна", 5.0)));
    }

    @Test
    void hashMapUsesIdAsKey() {
        assertEquals(4, hashMap.size());
        assertEquals(boris, hashMap.get(2));
    }

    @Test
    void treeMapKeysAreDescending() {
        assertEquals(List.of(4, 3, 2, 1), new ArrayList<>(treeMap.keySet()));
    }

    @Test
    void rangeIncludesBothBounds() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 5.0, 10.0);
        assertEquals(Set.of(anna, boris, vera), new HashSet<>(result));
    }

    @Test
    void rangeWholeScale() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 0.0, 10.0);
        assertEquals(Set.copyOf(all), new HashSet<>(result));
    }

    @Test
    void rangeSingleValue() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 7.5, 7.5);
        assertEquals(List.of(boris), result);
    }

    @Test
    void rangeWithNoMatches() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 8.0, 9.0);
        assertTrue(result.isEmpty());
    }

    @Test
    void rangeOnEmptyMap() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(Map.of(), 0.0, 10.0);
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"8.0, 2.0", "-1.0, 5.0", "0.0, 10.5", "-2.0, -1.0", "11.0, 12.0"})
    void rangeRejectsBadArguments(double min, double max) {
        assertThrows(IllegalArgumentException.class,
                () -> StudentRegistry.findStudentsByGradeRange(hashMap, min, max));
    }

    @Test
    void rangeReturnsNewMutableListAndKeepsMap() {
        List<Student> result = StudentRegistry.findStudentsByGradeRange(hashMap, 0.0, 10.0);
        result.clear();
        assertEquals(4, hashMap.size());
        assertTrue(result instanceof ArrayList);
    }

    @Test
    void topNReturnsLargestIdsInDescendingOrder() {
        assertEquals(List.of(gleb, vera), StudentRegistry.getTopNStudentsById(treeMap, 2));
    }

    @Test
    void topNLargerThanSizeReturnsAll() {
        assertEquals(List.of(gleb, vera, boris, anna), StudentRegistry.getTopNStudentsById(treeMap, 100));
    }

    @Test
    void topNEqualToSize() {
        assertEquals(List.of(gleb, vera, boris, anna), StudentRegistry.getTopNStudentsById(treeMap, 4));
    }

    @Test
    void topNZeroReturnsEmptyList() {
        assertTrue(StudentRegistry.getTopNStudentsById(treeMap, 0).isEmpty());
    }

    @Test
    void topNOnEmptyMap() {
        NavigableMap<Integer, Student> empty = StudentRegistry.createDescendingTreeMap(List.of());
        assertTrue(StudentRegistry.getTopNStudentsById(empty, 3).isEmpty());
    }

    @Test
    void topNRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> StudentRegistry.getTopNStudentsById(treeMap, -1));
    }

    @Test
    void topNReturnsNewMutableListAndKeepsMap() {
        List<Student> first = StudentRegistry.getTopNStudentsById(treeMap, 2);
        List<Student> second = StudentRegistry.getTopNStudentsById(treeMap, 2);
        assertNotSame(first, second);
        first.clear();
        assertEquals(4, treeMap.size());
        assertTrue(first instanceof ArrayList);
    }
}
