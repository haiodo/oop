---
marp: true
theme: default
paginate: true
---

# Nanotime on Different Operating Systems

---

## Slide 1: Introduction

### Understanding Nanotime

Overview of nanotime and its importance in computing.

---

## Slide 2: What is Nanotime?

### Definition

Explanation of nanotime and its precision.

---

## Slide 3: Importance of Nanotime

### Why Nanotime?

Use cases and significance in high-precision tasks.

---

## Slide 4: Nanotime in Linux

### Linux Implementation

How Linux handles nanotime using `clock_gettime`.

---

## Slide 5: Linux Example

### Code Example

```c
#include <time.h>
#include <stdio.h>

int main() {
  struct timespec ts;
  clock_gettime(CLOCK_MONOTONIC, &ts);
  printf("Nanoseconds: %ld\n", ts.tv_nsec);
  return 0;
}
```

---

## Slide 6: Nanotime in Windows

### Windows Implementation

How Windows handles nanotime using `QueryPerformanceCounter`.

---

## Slide 7: Windows Example

### Code Example

```c
#include <windows.h>
#include <stdio.h>

int main() {
  LARGE_INTEGER frequency, counter;
  QueryPerformanceFrequency(&frequency);
  QueryPerformanceCounter(&counter);
  printf("Nanoseconds: %lld\n", (counter.QuadPart * 1000000000) / frequency.QuadPart);
  return 0;
}
```

---

## Slide 8: Nanotime in macOS

### macOS Implementation

How macOS handles nanotime using `mach_absolute_time`.

---

## Slide 9: macOS Example

### Code Example

```c
#include <mach/mach_time.h>
#include <stdio.h>

int main() {
  uint64_t time = mach_absolute_time();
  printf("Nanoseconds: %llu\n", time);
  return 0;
}
```

---

## Slide 10: Nanotime in FreeBSD

### FreeBSD Implementation

How FreeBSD handles nanotime using `clock_gettime`.

---

## Slide 11: FreeBSD Example

### Code Example

```c
#include <time.h>
#include <stdio.h>

int main() {
  struct timespec ts;
  clock_gettime(CLOCK_MONOTONIC, &ts);
  printf("Nanoseconds: %ld\n", ts.tv_nsec);
  return 0;
}
```

---

## Slide 12: Nanotime in Android

### Android Implementation

How Android handles nanotime using `System.nanoTime`.

---

## Slide 13: Android Example

### Code Example

```java
public class Main {
  public static void main(String[] args) {
    long nanotime = System.nanoTime();
    System.out.println("Nanoseconds: " + nanotime);
  }
}
```

---

## Slide 14: Nanotime in iOS

### iOS Implementation

How iOS handles nanotime using `mach_absolute_time`.

---

## Slide 15: iOS Example

### Code Example

```c
#include <mach/mach_time.h>
#include <stdio.h>

int main() {
  uint64_t time = mach_absolute_time();
  printf("Nanoseconds: %llu\n", time);
  return 0;
}
```

---

## Slide 16: Comparison

### Cross-Platform Comparison

Comparing nanotime implementations across different OS.

---

## Slide 17: Performance

### Performance Considerations

Performance implications of using nanotime.

---

## Slide 18: Accuracy

### Accuracy and Precision

Accuracy and precision of nanotime across different OS.

---

## Slide 19: Use Cases

### Practical Use Cases

Real-world applications of nanotime.

---

## Slide 20: Conclusion

### Summary

Recap of key points and final thoughts.
