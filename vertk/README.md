# vertk

Vertxk is a set of tools to simplify [Vertx](http://vertk.io/) application development in [Kotlin Programming Language](https://kotlinlang.org/) 

## VertxK: Dependency injection with [bit](https://github.com/SalomonBrys/bit)
[bit](https://github.com/SalomonBrys/bit) is a simple, easy to use and easy to configure dependency retrieval container.

By default, Vertx does not provide any real support for dependency injection framerworks. 

Vertxk solves this issue enabling simple DI in Vertx Verticles through bit. 

BTW, it works with coroutines too!

Some Features
-------------
- Only 20Kb 
- No external dependencies (well... it requires Vertx and bit of course!)
- Works with Vertx 3.5+ and bit 4.1+ 

Getting Started
---------------

1. To get started, add vertk-bit dependency to your project:
 
```xml
        <dependency>
            <groupId>com.ufoscout.vertk</groupId>
            <artifactId>vertk-bit</artifactId>
            <version>${vertk.version}</version>
        </dependency>
```

2. Inject the VertxkBit Module in your bit Container:

```Kotlin
import com.github.salomonbrys.bit.bit
import com.github.salomonbrys.bit.jxinject.jxInjectorModule
import com.ufoscout.vertk.VertxkBit
...

        // setup vertk
        val vertk = Vertx.vertx()

        val bit = bit {
            import(jxInjectorModule)
            import(VertxkBit.module(vertk))
            import(// IMPORT YOUR MODULES)
        }

        VertxkBit.registerFactory(vertk, bit)

        awaitResult<String> {
            VertxkBit.deployVerticle<MainVerticle>(vertk, it)
        }
```

3. Register vertk Factory in Vertx:

```Kotlin
        VertxkBit.registerFactory(vertk, bit)
```

4. Everything's ready now! You can now inject whatever bean in your Verticles:

```Kotlin
// In this example I use a Coroutine based Verticle. 
// It works with standard verticles too.
// WARN: To use the coroutines you need to import the vertk-lang-kotlin-coroutines dependency
class MyVerticle (val myServiceOne: MyServiceOne, val myServiceTwo: MyServiceTwo) : CoroutineVerticle() {

    override suspend fun start() {
        println("Start main verticle with injected services!!!!")
    }

} 
```

5. Deploy your verticles and have fun :)
```Kotlin
    // With coroutines. it works even with default async deploy.   
    awaitResult<String> {
        VertxkBit.deployVerticle<MyVerticle>(vertk, it)
    }
```

The complete example
--------------------
Let's put all together.

Creata a Verticle and inject your services:

```Kotlin
class MyVerticle (val myServiceOne: MyServiceOne, val myServiceTwo: MyServiceTwo) : CoroutineVerticle() {

    override suspend fun start() {
        println("Start main verticle with injected services!!!!")
    }

} 
```

Instantiate Vertx and bit:

```Kotlin
        val vertk = Vertx.vertx()

        val bit = bit {
            import(jxInjectorModule)
            import(VertxkBit.module(vertk))
            import(// IMPORT YOUR MODULES)
        }

        VertxkBit.registerFactory(vertk, bit)

        awaitResult<String> {
            VertxkBit.deployVerticle<MyVerticle>(vertk, it)
        }
```

Cheers.

A complete working exmple can be found at [https://github.com/ufoscout/kotlin-vertk3](https://github.com/ufoscout/kotlin-vertk3)

