package hometask7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class CollectionPerformanceExperiment {
    private static final int SIZE = 10_000;
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 7;

    private static long checksum;

    private CollectionPerformanceExperiment() {
    }

    private interface Scenario {
        List<Integer> prepare(Supplier<List<Integer>> factory);

        void run(List<Integer> list);
    }

    private record NamedScenario(String name, Scenario scenario) {
    }

    public static void main(String[] args) {
        List<NamedScenario> scenarios = createScenarios();
        System.out.printf("%-36s %10s %18s %18s%n",
                "Операция", "Элементов", "ArrayList, мкс", "LinkedList, мкс");
        for (NamedScenario named : scenarios) {
            long arrayTime = measure(named.scenario(), ArrayList::new);
            long linkedTime = measure(named.scenario(), LinkedList::new);
            System.out.printf("%-36s %10d %18d %18d%n",
                    named.name(), SIZE, arrayTime / 1_000, linkedTime / 1_000);
        }
        System.out.println("Контрольная сумма: " + checksum);
    }

    private static long measure(Scenario scenario, Supplier<List<Integer>> factory) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            List<Integer> list = scenario.prepare(factory);
            scenario.run(list);
        }
        long[] times = new long[MEASURED_RUNS];
        for (int i = 0; i < MEASURED_RUNS; i++) {
            List<Integer> list = scenario.prepare(factory);
            long start = System.nanoTime();
            scenario.run(list);
            times[i] = System.nanoTime() - start;
        }
        Arrays.sort(times);
        return times[MEASURED_RUNS / 2];
    }

    private static List<Integer> filled(Supplier<List<Integer>> factory) {
        List<Integer> list = factory.get();
        for (int i = 0; i < SIZE; i++) {
            list.add(i);
        }
        return list;
    }

    private static List<NamedScenario> createScenarios() {
        List<NamedScenario> scenarios = new ArrayList<>();

        scenarios.add(new NamedScenario("Добавление в конец", new Scenario() {
            public List<Integer> prepare(Supplier<List<Integer>> factory) {
                return factory.get();
            }

            public void run(List<Integer> list) {
                for (int i = 0; i < SIZE; i++) {
                    list.add(i);
                }
                checksum += list.size();
            }
        }));

        scenarios.add(new NamedScenario("Добавление в начало", new Scenario() {
            public List<Integer> prepare(Supplier<List<Integer>> factory) {
                return factory.get();
            }

            public void run(List<Integer> list) {
                for (int i = 0; i < SIZE; i++) {
                    list.add(0, i);
                }
                checksum += list.get(0);
            }
        }));

        scenarios.add(new NamedScenario("Вставка в середину", new Scenario() {
            public List<Integer> prepare(Supplier<List<Integer>> factory) {
                return factory.get();
            }

            public void run(List<Integer> list) {
                for (int i = 0; i < SIZE; i++) {
                    list.add(list.size() / 2, i);
                }
                checksum += list.get(list.size() / 2);
            }
        }));

        scenarios.add(new NamedScenario("Доступ по индексу", new Scenario() {
            public List<Integer> prepare(Supplier<List<Integer>> factory) {
                return filled(factory);
            }

            public void run(List<Integer> list) {
                long sum = 0;
                for (int i = 0; i < SIZE; i++) {
                    sum += list.get(i);
                }
                checksum += sum;
            }
        }));

        scenarios.add(new NamedScenario("Удаление из начала", new Scenario() {
            public List<Integer> prepare(Supplier<List<Integer>> factory) {
                return filled(factory);
            }

            public void run(List<Integer> list) {
                long sum = 0;
                while (!list.isEmpty()) {
                    sum += list.remove(0);
                }
                checksum += sum;
            }
        }));

        scenarios.add(new NamedScenario("Удаление из конца", new Scenario() {
            public List<Integer> prepare(Supplier<List<Integer>> factory) {
                return filled(factory);
            }

            public void run(List<Integer> list) {
                long sum = 0;
                while (!list.isEmpty()) {
                    sum += list.remove(list.size() - 1);
                }
                checksum += sum;
            }
        }));

        return scenarios;
    }
}
