package com.miro.coroutinetest

import kotlinx.coroutines.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.time.measureTime

class CoroutinetestApplication

fun main(args: Array<String>) {
    val nrOfJobs = 1_000_000
    val threadPoolSize = 4
    val queueSize = 1_000_000 // at queuesize = 1000 tasks are dropped, as a result the program doesn't finish
    val submitDelay = 0L // ms
    val taskDuration = 3000L // ms
    val rejectPolicy = AbortPolicy()

    val executorService =
        ThreadPoolExecutor(
            threadPoolSize,
            threadPoolSize,
            60,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(queueSize),
            rejectPolicy,
        )

    val coroutineScope =
        CoroutineScope(
            executorService.asCoroutineDispatcher() + SupervisorJob() + CoroutineName("myscope"),
        )

    runBlocking {
        val jobs: Array<Deferred<Int>> =
            Array(nrOfJobs) {
                delay(submitDelay)
                coroutineScope.async {
                    val duration =
                        measureTime {
                            delay(taskDuration)
                        }
                    if (it % 1000 == 0) {
                        println("$it Hello from ${Thread.currentThread().name} $duration")
                    }
                    return@async it
                }
            }

        println("Waiting for jobs to finish")
        val results: List<Int> = jobs.map { it.await() }
        println("Results: ${results.size}")
        exitProcess(0)
    }
}
