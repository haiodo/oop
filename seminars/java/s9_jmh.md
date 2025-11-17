---
marp: true
---

# Java JMH Framework

## Инструмент для микробенчмаркинга в Java

---

## Слайд 1: Введение

- Java Microbenchmark Harness (JMH) - фреймворк для микробенчмаркинга
- Разработан инженерами OpenJDK для точного измерения производительности
- Предназначен для тестирования производительности кода на Java
- Позволяет получать достоверные результаты с учетом оптимизаций JVM

---

## Слайд 2: Зачем нужен JMH?

- **Проблема:** Обычные подходы к замеру времени исполнения часто неточны

  ```java
  // Неточный способ замера времени
  long start = System.currentTimeMillis();
  method(); // код, который мы хотим измерить
  long end = System.currentTimeMillis();
  System.out.println("Время: " + (end - start) + " мс");
  ```

- **Решение JMH:**
  - Учитывает оптимизации JIT-компилятора
  - Предотвращает "мертвый" код
  - Предоставляет статистический анализ результатов
  - Инструменты для прогрева JVM (warmup)

---

## Слайд 3: Настройка JMH

- Добавление зависимостей в Maven (файл `pom.xml`):

```xml
<dependency>
  <groupId>org.openjdk.jmh</groupId>
  <artifactId>jmh-core</artifactId>
  <version>1.36</version>
</dependency>
<dependency>
  <groupId>org.openjdk.jmh</groupId>
  <artifactId>jmh-generator-annprocess</artifactId>
  <version>1.36</version>
</dependency>
```

- Для быстрого старта можно использовать Maven архетип:

```
mvn archetype:generate \
  -DinteractiveMode=false \
  -DarchetypeGroupId=org.openjdk.jmh \
  -DarchetypeArtifactId=jmh-java-benchmark-archetype \
  -DgroupId=org.sample \
  -DartifactId=test \
  -Dversion=1.0
```

---

## Слайд 4: Простой пример бенчмарка

```java
package org.sample;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // режим измерения: среднее время выполнения
@OutputTimeUnit(TimeUnit.NANOSECONDS) // единица измерения: наносекунды
@Warmup(iterations = 3) // количество прогревочных итераций
@Measurement(iterations = 5) // количество измерительных итераций
@Fork(1) // количество запусков в отдельных процессах
public class MyBenchmark {
    @Benchmark // метод, который нужно измерить
    public void testMethod() {
        // Код, производительность которого измеряется
        // Например:
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += i;
        }
    }
}
```

---

## Слайд 5: Запуск бенчмарков

- **Способ 1:** Через плагин Maven

```sh
mvn clean install
java -jar target/benchmarks.jar
```

- **Способ 2:** С помощью JMH Runner API

```java
public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
}
```

- **Запуск с фильтрацией по шаблону:**

```sh
java -jar target/benchmarks.jar MyBenchmark.test.*
```

---

## Слайд 6: Режимы бенчмарков

JMH поддерживает несколько режимов измерения производительности:

- **Throughput** - количество операций в единицу времени
- **AverageTime** - среднее время выполнения одной операции
- **SampleTime** - распределение времени выполнения операции
- **SingleShotTime** - время выполнения одиночной операции
- **All** - все режимы сразу

---

## Слайд 7: Режим пропускной способности (Throughput)

```java
@BenchmarkMode(Mode.Throughput) // операции в секунду
@OutputTimeUnit(TimeUnit.SECONDS)
@Benchmark
public void testThroughput() {
    // Чем больше операций в секунду, тем лучше
    int sum = 0;
    for (int i = 0; i < 100; i++) {
        sum += i;
    }
}
```

Результаты показывают, сколько операций может быть выполнено за единицу времени.

Пример вывода:

```
Benchmark                 Mode  Cnt     Score    Error  Units
MyBenchmark.testThroughput  thrpt    5  1234,567 ± 12,345  ops/s
```

---

## Слайд 8: Режим среднего времени (AverageTime)

```java
@BenchmarkMode(Mode.AverageTime) // среднее время на операцию
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Benchmark
public void testAverageTime() {
    // Чем меньше времени на операцию, тем лучше
    int sum = 0;
    for (int i = 0; i < 100; i++) {
        sum += i;
    }
}
```

Результаты показывают среднее время выполнения операции.

Пример вывода:

```
Benchmark                 Mode  Cnt     Score    Error  Units
MyBenchmark.testAverageTime  avgt    5    123,456 ±   1,234  ns/op
```

---

## Слайд 9: Режим распределения времени (SampleTime)

```java
@BenchmarkMode(Mode.SampleTime) // распределение времени выполнения
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Benchmark
public void testSampleTime() {
    // Показывает распределение времени выполнения
    int sum = 0;
    for (int i = 0; i < 1000; i++) {
        sum += i;
    }
}
```

Результаты показывают распределение времени выполнения по перцентилям.

Пример вывода:

```
Benchmark                      Mode  Cnt   Score   Error  Units
MyBenchmark.testSampleTime     sample  1000  123,456 ± 1,234  us/op
  MyBenchmark.testSampleTime:p0.00    sample       120,000          us/op
  MyBenchmark.testSampleTime:p0.99    sample       130,000          us/op
```

---

## Слайд 10: Режим одиночного выполнения (SingleShotTime)

```java
@BenchmarkMode(Mode.SingleShotTime) // время одиночного выполнения
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Benchmark
public void testSingleShotTime() {
    // Измеряет время однократного выполнения
    // Полезно для "холодных" запусков
    int sum = 0;
    for (int i = 0; i < 10000000; i++) {
        sum += i;
    }
}
```

Результаты показывают время одиночного выполнения операции.

Пример вывода:

```
Benchmark                         Mode  Cnt   Score   Error  Units
MyBenchmark.testSingleShotTime     ss    5   12,345 ± 0,123  ms/op
```

---

## Слайд 11: Параметризация бенчмарков

- Используйте аннотацию `@Param` для тестирования с различными входными данными

```java
@State(Scope.Benchmark)
public class ParamBenchmark {
    @Param({"10", "100", "1000"}) // тестирование на разных размерах
    public int size;

    private int[] array;

    @Setup
    public void setup() {
        array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
    }

    @Benchmark
    public int sum() {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }
}
```

---

## Слайд 12: Настройка и очистка состояний

- `@Setup` - выполняется перед бенчмарком
- `@TearDown` - выполняется после бенчмарка
- Уровни: Trial (запуск), Iteration (итерация), Invocation (вызов)

```java
@State(Scope.Benchmark)
public class SetupTeardownBenchmark {
    private List<Integer> data;

    @Setup(Level.Trial) // выполняется один раз перед всеми итерациями
    public void setupTrial() {
        System.out.println("Инициализация данных перед всеми тестами");
        data = new ArrayList<>();
    }

    @Setup(Level.Iteration) // перед каждой итерацией
    public void setupIteration() {
        for (int i = 0; i < 1000; i++) {
            data.add(i);
        }
    }

    @TearDown(Level.Iteration) // после каждой итерации
    public void teardownIteration() {
        data.clear();
    }

    @Benchmark
    public int test() {
        return data.stream().mapToInt(i -> i).sum();
    }
}
```

---

## Слайд 13: Аннотации состояния (@State)

JMH использует состояния для хранения данных между вызовами бенчмарка:

- `@State(Scope.Benchmark)` - одно состояние для всех потоков
- `@State(Scope.Thread)` - состояние для каждого потока
- `@State(Scope.Group)` - состояние для группы потоков

```java
@State(Scope.Thread) // каждый поток получает свою копию состояния
public class ThreadState {
    // Данные, специфичные для потока
    public ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> 0);
}

@State(Scope.Benchmark) // все потоки используют общее состояние
public class SharedState {
    // Общие данные
    public AtomicInteger counter = new AtomicInteger();
}
```

---

## Слайд 14: Пример использования состояний

```java
@State(Scope.Benchmark) // общее для всех потоков
public class BenchmarkState {
    public final List<Integer> list = new ArrayList<>();

    @Setup
    public void setup() {
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
    }
}

@State(Scope.Thread) // независимое для каждого потока
public class ThreadState {
    public int localSum = 0;
}

@Benchmark
public int testMethod(BenchmarkState bState, ThreadState tState) {
    // использование состояний
    tState.localSum += bState.list.get(ThreadLocalRandom.current().nextInt(bState.list.size()));
    return tState.localSum;
}
```

---

## Слайд 15: Использование бенчмарк-состояний

```java
@State(Scope.Benchmark)
public class SortingBenchmark {
    @Param({"100", "1000", "10000"})
    private int arraySize;

    private int[] unsortedArray;
    private int[] sortedArray;

    @Setup(Level.Iteration)
    public void setup() {
        unsortedArray = new int[arraySize];
        sortedArray = new int[arraySize];
        Random random = new Random(42); // фиксированное seed для воспроизводимости

        for (int i = 0; i < arraySize; i++) {
            unsortedArray[i] = random.nextInt(arraySize);
            sortedArray[i] = unsortedArray[i];
        }
        Arrays.sort(sortedArray); // предварительно отсортированный массив
    }

    @Benchmark
    public int[] testSort() {
        int[] copy = Arrays.copyOf(unsortedArray, unsortedArray.length);
        Arrays.sort(copy);
        return copy;
    }
}
```

---

## Слайд 16: Форки (Fork) в JMH

- `@Fork` контролирует запуск в отдельных процессах JVM
- Важно для изоляции бенчмарков от влияния друг на друга
- Параметр `jvmArgs` позволяет настраивать параметры JVM

```java
@Fork(
    value = 2, // 2 разных запуска в отдельных процессах
    jvmArgs = {"-Xms2G", "-Xmx2G"} // аргументы JVM
)
@Benchmark
public void testWithCustomJVMOptions() {
    // Код бенчмарка с заданными параметрами JVM
    // Например, с фиксированным размером памяти 2GB
}

@Fork(0) // для отладочных целей (без форка)
@Benchmark
public void testWithoutFork() {
    // Запустится в том же процессе JVM
    // Не рекомендуется для реальных бенчмарков!
}
```

---

## Слайд 17: Прогрев JVM (Warmup)

- `@Warmup` определяет количество итераций для прогрева JVM
- Прогрев необходим, чтобы JIT-компилятор оптимизировал код
- Результаты прогрева не учитываются в итоговых показателях

```java
@Warmup(
    iterations = 5, // количество итераций прогрева
    time = 1, // время на итерацию
    timeUnit = TimeUnit.SECONDS // единица измерения времени
)
@Benchmark
public void testWithWarmup() {
    // JVM выполнит 5 итераций по 1 секунде
    // для прогрева перед измерением
    double result = 0;
    for (int i = 0; i < 1000; i++) {
        result += Math.sqrt(i);
    }
}
```

---

## Слайд 18: Измерительные итерации

- `@Measurement` определяет параметры итераций измерения
- Результаты этих итераций используются для финальной статистики

```java
@Measurement(
    iterations = 10, // количество измерительных итераций
    time = 2, // время на каждую итерацию
    timeUnit = TimeUnit.SECONDS, // единица измерения времени
    batchSize = 1000 // количество операций за одно измерение
)
@Benchmark
public void testWithCustomMeasurement() {
    // Будет выполнено 10 итераций по 2 секунды
    // В каждой итерации операция повторится 1000 раз
    double x = Math.random();
    double y = Math.sin(x);
}
```

---

## Слайд 19: Результаты бенчмарка

Пример вывода JMH:

```
Benchmark                        Mode  Cnt     Score     Error  Units
MyBenchmark.testMethod          thrpt    5  1234,567 ±  12,345  ops/s
MyBenchmark.testSecondMethod    thrpt    5   567,890 ±   5,678  ops/s
```

Где:

- **Score** - среднее значение измерения
- **Error** - погрешность (доверительный интервал)
- **Units** - единицы измерения (ops/s = операций в секунду)
- **Cnt** - количество измерений
- **Mode** - режим измерения

---

## Слайд 20: Интерпретация результатов

- **Сравнение реализаций:**

  - Внимательно проверяйте показатели погрешности (Error)
  - Если доверительные интервалы пересекаются, разница может быть статистически незначимой

- **Анализ производительности:**

  - В режиме Throughput: чем выше Score, тем лучше
  - В режимах AverageTime/SampleTime/SingleShotTime: чем ниже Score, тем лучше

- **Используйте параметризацию:**
  - Для понимания, как меняется производительность при изменении входных данных

---

## Слайд 21: Продвинутые возможности

- **Исключение влияния компиляции:**

  ```java
  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public void methodNotInlined() { /* ... */ }

  @CompilerControl(CompilerControl.Mode.INLINE)
  public void methodForceInlined() { /* ... */ }
  ```

- **Многопоточное тестирование:**
  ```java
  @Threads(4) // Запуск в 4 потока
  @Benchmark
  public void testMultiThreaded() { /* ... */ }
  ```

---

## Слайд 22: Пользовательские профайлеры

- Позволяют собирать дополнительную информацию при бенчмаркинге
- Доступны встроенные профайлеры: GC, Comp, HS_THR, etc.
- Можно создать пользовательские профайлеры, реализовав интерфейс `Profiler`

```java
// Запуск с профилированием GC
java -jar target/benchmarks.jar -prof gc

// Запуск с несколькими профайлерами
java -jar target/benchmarks.jar -prof gc -prof comp
```

Пример вывода профайлера GC:

```
Benchmark          Mode  Cnt    Score    Error   Units
test.MyBench.test  thrpt   10  123.456 ± 1.234   ops/s

Benchmark          Mode  Cnt    Score    Error   Units
test.MyBench.test:·gc.alloc.rate       thrpt   10  12.345 ± 0.123  MB/sec
test.MyBench.test:·gc.count            thrpt   10   3.000          counts
```

---

## Слайд 23: Использование Blackhole для предотвращения оптимизаций

```java
@Benchmark
public void testMethodWithBlackhole(Blackhole blackhole) {
    // JIT компилятор может удалить "неиспользуемые" вычисления
    int result1 = compute1(); // может быть оптимизировано!

    // Blackhole "поглощает" значение,
    // предотвращая оптимизации мертвого кода
    blackhole.consume(result1);

    int result2 = compute2();
    blackhole.consume(result2);
}
```

Без `Blackhole`, JIT-компилятор может удалить "неиспользуемые" вычисления,
что приведет к некорректным результатам измерений.

---

## Слайд 24: Предотвращение оптимизаций мертвого кода

**Проблема:**

```java
@Benchmark
public void wrongWay() {
    int sum = 0;
    for (int i = 0; i < 1000; i++) {
        sum += i;
    }
    // sum не используется - JIT может полностью удалить цикл!
}
```

**Решение 1: Blackhole**

```java
@Benchmark
public void rightWay(Blackhole bh) {
    int sum = 0;
    for (int i = 0; i < 1000; i++) {
        sum += i;
    }
    bh.consume(sum); // предотвращаем оптимизацию
}
```

**Решение 2: Возвращаемое значение**

```java
@Benchmark
public int alsoRightWay() {
    int sum = 0;
    for (int i = 0; i < 1000; i++) {
        sum += i;
    }
    return sum; // JMH автоматически "поглощает" возвращаемое значение
}
```

---

## Слайд 25: Советы по бенчмаркингу

1. **Изолируйте тесты:**

   - Один бенчмарк должен тестировать одну операцию
   - Используйте @Fork для изоляции в отдельных процессах

2. **Избегайте сайд-эффектов:**

   - Не выполняйте I/O операции в бенчмарках
   - Не меняйте глобальное состояние между запусками

3. **Учитывайте влияние JIT-компилятора:**

   - Всегда используйте достаточное количество прогревов
   - Учитывайте возможные оптимизации кода

4. **Избегайте частых сборок мусора:**
   - Инициализируйте объекты в @Setup
   - Используйте профайлер GC для контроля

---

## Слайд 26: Типичные ошибки

1. **Игнорирование прогрева JVM**

   ```java
   // Неправильно:
   @Warmup(iterations = 0)
   ```

2. **Неверная интерпретация результатов**

   ```
   Score: 100.0 ± 50.0 ops/s
   // Погрешность 50% делает результат ненадежным
   ```

3. **Игнорирование влияния сборщика мусора (GC)**

   ```java
   @Benchmark
   public List<String> createManyObjects() {
       // Создание многих объектов может вызвать GC
       // и исказить результаты
   }
   ```

4. **Сравнение неоптимизированного и оптимизированного кода**

   ```java
   // Неправильно сравнивать:
   @CompilerControl(CompilerControl.Mode.DONT_INLINE)
   public void method1() { /* ... */ }

   // С обычным методом:
   public void method2() { /* ... */ }
   ```

---

## Слайд 27: Практический пример: сравнение методов конкатенации строк

Задача: Сравнить производительность различных методов соединения строк:

- Оператор `+`
- `StringBuilder`
- `StringBuffer`
- `String.concat()`

---

## Слайд 28: Бенчмарк конкатенации строк

```java
@State(Scope.Benchmark)
public class StringConcatBenchmark {
    @Param({"10", "100", "1000"})
    private int iterations;

    private String[] strings;

    @Setup
    public void setup() {
        strings = new String[iterations];
        for (int i = 0; i < iterations; i++) {
            strings[i] = "str" + i;
        }
    }

    @Benchmark
    public String testPlus() {
        String result = "";
        for (String s : strings) {
            result = result + s;
        }
        return result;
    }

    @Benchmark
    public String testStringBuilder() {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Benchmark
    public String testStringBuffer() {
        StringBuffer sb = new StringBuffer();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Benchmark
    public String testStringConcat() {
        String result = "";
        for (String s : strings) {
            result = result.concat(s);
        }
        return result;
    }
}
```

---

## Слайд 29: Анализ результатов конкатенации строк

- **Для малого числа строк (10):**

  - Различия между методами незначительны
  - Оператор `+` может быть оптимизирован JIT-компилятором

- **Для среднего числа строк (100):**

  - `StringBuilder` в 10-100 раз быстрее, чем `+`
  - `StringBuffer` медленнее `StringBuilder` из-за синхронизации

- **Для большого числа строк (1000):**
  - `+` и `String.concat()` очень медленные (O(n²) сложность)
  - `StringBuilder` значительно эффективнее
  - Частые сборки мусора при использовании `+`

---

## Слайд 30: Заключение

- **JMH** - мощный инструмент для точного бенчмаркинга Java-кода
- Преимущества:

  - Учитывает особенности работы JVM
  - Предотвращает распространенные ошибки измерений
  - Предоставляет детальную статистику

- **Основные принципы бенчмаркинга:**

  - Измеряйте только то, что нужно измерить
  - Учитывайте оптимизации компилятора
  - Всегда проверяйте погрешность измерений
  - Используйте соответствующие режимы бенчмаркинга

- **Где использовать:**
  - Сравнение алгоритмов
  - Оптимизация производительности
  - Выбор между альтернативными реализациями

---

## Слайд 31: Практическое задание

1. Создайте проект с JMH
2. Напишите бенчмарки для сравнения:
   - ArrayList vs LinkedList для различных операций
   - HashMap vs TreeMap для поиска элементов
3. Проанализируйте результаты и подготовьте краткий отчет
4. Объясните, почему одни структуры данных показывают лучшие результаты, чем другие для разных операций

---

## Слайд 32: Полезные ресурсы

- [JMH Homepage](https://openjdk.org/projects/code-tools/jmh/)
- [JMH Samples на GitHub](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [JMH Visualizer](https://jmh.morethan.io/) - онлайн-инструмент для визуализации результатов
- [Java Performance: The Definitive Guide](https://www.oreilly.com/library/view/java-performance-the/9781449363512/)
- [Baeldung - Java Microbenchmark Harness](https://www.baeldung.com/java-microbenchmark-harness)
