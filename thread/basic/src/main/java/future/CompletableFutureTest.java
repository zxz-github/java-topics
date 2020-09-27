package future;

import java.util.Random;
import java.util.concurrent.*;

public class CompletableFutureTest {
    private static final Executor executor = ForkJoinPool.commonPool();
    private static final Executor executor2 = new ThreadPoolExecutor(5, 10, 1000L,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(20),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "任务A");
        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> "任务B");
        CompletableFuture<String> futureC = futureB.thenApply(b -> {
            System.out.println("执行任务C.");
            System.out.println("参数:" + b);//参数:任务B
            return "a";
        });
        CompletableFuture<Double> cf = CompletableFuture.supplyAsync(CompletableFutureTest::fetchPrice, executor);
        cf.thenApplyAsync(v -> {
            return v;
        });
    }

    public static void supplyAsync(String[] args) {
        //有返回值
        //public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier){..}
        //public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,Executor executor){..}

    }

    public static void runAsync(String[] args) {
        //无返回值
        //public static CompletableFuture<Void> runAsync(Runnable runnable){..}
        //public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor){..}
    }

    public static void thenAccept(String[] args) {
        //public CompletionStage<Void> thenAccept(Consumer<? super T> action);
        //public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
        //public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
    }

    public static void thenRun(String[] args) {
        //public CompletionStage<Void> thenRun(Runnable action);
        //public CompletionStage<Void> thenRunAsync(Runnable action);
        //public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);
    }

    //当前任务正常完成以后执行，当前任务的执行的结果会作为下一任务的输入参数,有返回值
    public static void thenApply(String[] args) {
        //public <U> CompletableFuture<U>     thenApply(Function<? super T,? extends U> fn)
        //public <U> CompletableFuture<U>     thenApplyAsync(Function<? super T,? extends U> fn)
        //public <U> CompletableFuture<U>     thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");
        CompletableFuture<String> futureB = futureA.thenApply(s -> s + " world");
        CompletableFuture<String> future3 = futureB.thenApply(String::toUpperCase);
        System.out.println(future3.join());
        //上面的代码,我们当然可以先调用future.join()先得到任务A的返回值,然后再拿返回值做入参去执行任务B,
        // 而thenApply的存在就在于帮我简化了这一步,我们不必因为等待一个计算完成而一直阻塞着调用线程，而是告诉CompletableFuture你啥时候执行完就啥时候进行下一步. 就把多个任务串联起来了
    }

    //这个方法和thenApply非常像,都是接受上一个任务的结果作为入参,执行自己的操作,然后返回.那具体有什么区别呢?
    //
    //　　thenApply():它的功能相当于将CompletableFuture<T>转换成CompletableFuture<U>,改变的是同一个CompletableFuture中的泛型类型
    //
    //　　thenCompose():用来连接两个CompletableFuture，返回值是一个新的CompletableFuture
    public static void thenCompose(String[] args) {
        //public <U> CompletableFuture<U>     thenCompose(Function<? super T,? extends CompletionStage<U>> fn)
        //public <U> CompletableFuture<U>     thenComposeAsync(Function<? super T,? extends CompletionStage<U>> fn)
        //public <U> CompletableFuture<U>     thenComposeAsync(Function<? super T,? extends CompletionStage<U>> fn, Executor executor)
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");

        CompletableFuture<String> futureB = futureA.thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "world"));

        CompletableFuture<String> future3 = futureB.thenCompose(s -> CompletableFuture.supplyAsync(s::toUpperCase));

        System.out.println(future3.join());

    }

    //thenCombine(..)是结合两个任务的返回值进行转化后再返回,那如果不需要返回呢,那就需要thenAcceptBoth(..),
    // 同理,如果连两个任务的返回值也不关心呢,那就需要runAfterBoth了,如果理解了上面三个方法,thenApply,thenAccept,thenRun,这里就不需要单独再提这两个方法了,只在这里提一下.
    public static void thenCombine_thenCombineAsync_thenCombineAsync(String[] args) {
        //public <U,V> CompletableFuture<V>     thenCombine(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn)
        //public <U,V> CompletableFuture<V>     thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn)
        //public <U,V> CompletableFuture<V>     thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn, Executor executor)
        //需要根据商品id查询商品的当前价格,分两步,查询商品的原始价格和折扣,这两个查询相互独立,当都查出来的时候用原始价格乘折扣,算出当前价格. 使用方法:thenCombine(..)
        CompletableFuture<Double> futurePrice = CompletableFuture.supplyAsync(() -> 100d);
        CompletableFuture<Double> futureDiscount = CompletableFuture.supplyAsync(() -> 0.8);
        CompletableFuture<Double> futureResult = futurePrice.thenCombine(futureDiscount, (price, discount) -> price * discount);
        System.out.println("最终价格为:" + futureResult.join()); //最终价格为:80.0
    }

    //功能:执行两个CompletionStage的结果,那个先执行完了,就是用哪个的返回值进行下一步操作
    //　　场景:假设查询商品a,有两种方式,A和B,但是A和B的执行速度不一样,我们希望哪个先返回就用那个的返回值.
    public static void either(String[] args) {
        //public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,Function<? super T, U> fn);
        //public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn);
        //public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn,Executor executor);
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "通过方式A获取商品a";
        });
        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "通过方式B获取商品a";
        });
        CompletableFuture<String> futureC = futureA.applyToEither(futureB, product -> "结果:" + product);
        System.out.println(futureC.join()); //结果:通过方式A获取商品a
    }

    //　　功能:当运行出现异常时,调用该方法可进行一些补偿操作,如设置默认值.
    public static void exceptionally(String[] args) {
        CompletableFuture<String> futureA = CompletableFuture.
                supplyAsync(() -> "执行结果:" + (100 / 0))
                .thenApply(s -> "futureA result:" + s)
                .exceptionally(e -> {
                    System.out.println(e.getMessage()); //java.lang.ArithmeticException: / by zero
                    return "futureA result: 100";
                });
        CompletableFuture<String> futureB = CompletableFuture.
                supplyAsync(() -> "执行结果:" + 50)
                .thenApply(s -> "futureB result:" + s)
                .exceptionally(e -> "futureB result: 100");
        System.out.println(futureA.join());//futureA result: 100
        System.out.println(futureB.join());//futureB result:执行结果:50
    }

    public static void whenComplete(String[] args) {
        //public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action);
        //public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action);
        //public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action,Executor executor);
        CompletableFuture<String> futureA = CompletableFuture.
                supplyAsync(() -> "执行结果:" + (100 / 0))
                .thenApply(s -> "apply result:" + s)
                .whenComplete((s, e) -> {
                    if (s != null) {
                        System.out.println(s);//未执行
                    }
                    if (e == null) {
                        System.out.println(s);//未执行
                    } else {
                        System.out.println(e.getMessage());//java.lang.ArithmeticException: / by zero
                    }
                })
                .exceptionally(e -> {
                    System.out.println("ex" + e.getMessage()); //ex:java.lang.ArithmeticException: / by zero
                    return "futureA result: 100";
                });
        System.out.println(futureA.join());//futureA result: 100
        //根据控制台,我们可以看出执行流程是这样,supplyAsync->whenComplete->exceptionally,可以看出并没有进入thenApply执行,原因也显而易见,在supplyAsync中出现了异常,thenApply只有当正常返回时才会去执行.而whenComplete不管是否正常执行,还要注意一点,whenComplete是没有返回值的.
        //
        //　　上面代码我们使用了函数式的编程风格并且先调用whenComplete再调用exceptionally,如果我们先调用exceptionally,再调用whenComplete会发生什么呢,我们看一下
        //代码先执行了exceptionally后执行whenComplete,可以发现,由于在exceptionally中对异常进行了处理,并返回了默认值,whenComplete中接收到的结果是一个正常的结果,被exceptionally美化过的结果,这一点需要留意一下.
    }

    //　　功能:当CompletableFuture的计算结果完成，或者抛出异常的时候，可以通过handle方法对结果进行处理
    //根据控制台输出,可以看到先执行handle,打印了异常信息,并对接过设置了默认值500,exceptionally并没有执行,因为它得到的是handle返回给它的值,由此我们大概推测handle和whenComplete的区别
    //
    //　　　1.都是对结果进行处理,handle有返回值,whenComplete没有返回值
    //
    //　　　2.由于1的存在,使得handle多了一个特性,可在handle里实现exceptionally的功能
    public static void handle(String[] args) {
        CompletableFuture<String> futureA = CompletableFuture.
                supplyAsync(() -> "执行结果:" + (100 / 0))
                .thenApply(s -> "apply result:" + s)
                .exceptionally(e -> {
                    System.out.println("ex:" + e.getMessage()); //java.lang.ArithmeticException: / by zero
                    return "futureA result: 100";
                })
                .handle((s, e) -> {
                    if (e == null) {
                        System.out.println(s);//futureA result: 100
                    } else {
                        System.out.println(e.getMessage());//未执行
                    }
                    return "handle result:" + (s == null ? "500" : s);
                });
        System.out.println(futureA.join());//handle result:futureA result: 100
    }

    //allOf:当所有的CompletableFuture都执行完后执行计算
    //
    //　　anyOf:最快的那个CompletableFuture执行完之后执行计算
    public static void allOf_anyOf(String[] args) {

            ExecutorService executorService = Executors.newFixedThreadPool(4);

            long start = System.currentTimeMillis();
            CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000 + new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "商品详情";
            },executorService);

            CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000 + new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "卖家信息";
            },executorService);

            CompletableFuture<String> futureC = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000 + new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "库存信息";
            },executorService);

            CompletableFuture<String> futureD = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000 + new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "订单信息";
            },executorService);

            CompletableFuture<Void> allFuture = CompletableFuture.allOf(futureA, futureB, futureC, futureD);
            allFuture.join();

            System.out.println(futureA.join() + futureB.join() + futureC.join() + futureD.join());
            System.out.println("总耗时:" + (System.currentTimeMillis() - start));

    }

    public static void main1(String[] args) throws Exception {
        // 创建异步执行任务:
        CompletableFuture<Double> cf = CompletableFuture.supplyAsync(CompletableFutureTest::fetchPrice);
        // 如果执行成功:
        cf.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 如果执行异常:
        cf.exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });
        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
        Thread.sleep(200);
    }

    static Double fetchPrice() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        if (Math.random() < 0.3) {
            throw new RuntimeException("fetch price failed!");
        }
        return 5 + Math.random() * 20;
    }
}
