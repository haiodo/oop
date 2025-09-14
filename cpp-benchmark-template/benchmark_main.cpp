#include <benchmark/benchmark.h>

class A
{
public:
  A()
  {
  }
  int method() { return 0; }
  int method_throw()
  {
    throw std::runtime_error("Error");
    return 0;
  }
};

class B
{
public:
  B()
  {
  }
  virtual ~B() {}
  virtual int method() { return 0; }
  virtual int method_throw()
  {
    throw std::runtime_error("Error");
    return 0;
  }
};

static void
BM_NewA(benchmark::State &state)
{
  for (auto _ : state)
    A a;
}

static void
BM_NewAHeap(benchmark::State &state)
{
  for (auto _ : state)
    new A();
}

static void
BM_NewB(benchmark::State &state)
{
  for (auto _ : state)
    B b;
}

static void BM_NewBHeap(benchmark::State &state)
{
  for (auto _ : state)
    new B();
}

static void BM_CallMethod(benchmark::State &state)
{
  A a;
  for (auto _ : state)
    a.method();
}

static void BM_CallVirtualMethod(benchmark::State &state)
{
  B b;
  for (auto _ : state)
    b.method();
}

static void BM_CheckException(benchmark::State &state)
{
  A a;
  for (auto _ : state)
    try
    {
      a.method_throw();
    }
    catch (...)
    {
    }
}

static void BM_CheckExceptionV(benchmark::State &state)
{
  B b;
  for (auto _ : state)
    try
    {
      b.method_throw();
    }
    catch (...)
    {
    }
}

// Register the function as a benchmark
BENCHMARK(BM_NewA);
BENCHMARK(BM_NewAHeap);
BENCHMARK(BM_NewB);
BENCHMARK(BM_NewBHeap);
BENCHMARK(BM_CallMethod);
BENCHMARK(BM_CallVirtualMethod);
BENCHMARK(BM_CheckException);
BENCHMARK(BM_CheckExceptionV);
// Run the benchmark
BENCHMARK_MAIN();