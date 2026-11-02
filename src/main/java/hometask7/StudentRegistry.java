package hometask7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class StudentRegistry {
    public static final double MIN_GRADE = 0.0;

    public static final double MAX_GRADE = 10.0;

    private StudentRegistry() {
    }

    public static Map<Integer, Student> createHashMap(List<Student> students) {
        Map<Integer, Student> map = new HashMap<>();
        for (Student student : students) {
            map.put(student.id(), student);
        }
        return map;
    }

    public static NavigableMap<Integer, Student> createDescendingTreeMap(List<Student> students) {
        NavigableMap<Integer, Student> map = new TreeMap<>(Collections.reverseOrder());
        for (Student student : students) {
            map.put(student.id(), student);
        }
        return map;
    }

    public static List<Student> findStudentsByGradeRange(
            Map<Integer, Student> students,
            double minGrade,
            double maxGrade) {
        if (Double.isNaN(minGrade) || Double.isNaN(maxGrade)
                || minGrade > maxGrade
                || minGrade < MIN_GRADE || maxGrade > MAX_GRADE) {
            throw new IllegalArgumentException("Некорректный диапазон оценок: [" + minGrade + ", " + maxGrade + "]");
        }
        List<Student> result = new ArrayList<>();
        for (Student student : students.values()) {
            if (student.grade() >= minGrade && student.grade() <= maxGrade) {
                result.add(student);
            }
        }
        return result;
    }

    public static List<Student> getTopNStudentsById(NavigableMap<Integer, Student> students, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n не может быть отрицательным: " + n);
        }
        List<Student> result = new ArrayList<>();
        if (n == 0) {
            return result;
        }
        Iterator<Student> iterator = students.values().iterator();
        while (iterator.hasNext() && result.size() < n) {
            result.add(iterator.next());
        }
        return result;
    }
}
