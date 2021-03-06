package com.ufoscout.vertk.bit.web

import com.ufoscout.vertk.BaseTest
import com.ufoscout.vertk.bit.VertkBit
import com.ufoscout.vertk.bit.json.JsonModule
import com.ufoscout.vertk.web.client.bodyAsJson
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors

class RouterModuleTest: BaseTest() {

    val port = getFreePort()
    var client = WebClient.create(vertk)

    @BeforeEach
    fun setup() = runBlocking {
        val koin = VertkBit.start(vertk,
                JsonModule(),
                RouterModule(RouterConfig(port), HttpServerOptions()),
                RouterTestModule()
        )
        assertNotNull(koin.get<RouterService>())
        assertNotNull(koin.get<WebExceptionService>())
    }

    @Test
    fun shouldThrow500() = runBlocking<Unit> {

        val message = UUID.randomUUID().toString()

        val response = client.get(port, "localhost", "/core/test/fatal/${message}").
                sendAwait()

        assertEquals(500, response.statusCode())

        val errorDetails = response.bodyAsJson<ErrorDetails>()
        assertEquals(response.statusCode(), errorDetails.code)

        assertTrue(errorDetails.message.contains("Error code:"))
        assertFalse(errorDetails.message.contains(message))
    }

    @Test
    fun badRequestExceptionShouldThrow400() = runBlocking<Unit> {

        val message = UUID.randomUUID().toString()

        val response = client.get(port, "localhost", "/core/test/badRequestException/${message}").sendAwait()

        assertEquals(400, response.statusCode())

        val errorDetails = response.bodyAsJson<ErrorDetails>()
        assertEquals(response.statusCode(), errorDetails.code)

        assertEquals(message, errorDetails.message)
    }

    @Test
    fun shouldThrowWebException() = runBlocking<Unit> {

        val message = UUID.randomUUID().toString()
        val statusCode = 400 + Random().nextInt(50)

        val response = client.get(port, "localhost", "/core/test/webException/${statusCode}/${message}").sendAwait()

        assertEquals(statusCode, response.statusCode())
        val errorDetails = response.bodyAsJson<ErrorDetails>()

        assertEquals(response.statusCode(), errorDetails.code)

        assertFalse(errorDetails.message.isEmpty())
        assertEquals(message, errorDetails.message)
    }

    @Test
    fun shouldMapWebExceptionFromCustomException() = runBlocking<Unit> {

        val response = client.get(port, "localhost", "/core/test/customException").sendAwait()
        assertEquals(12345, response.statusCode())

        val errorDetails = response.bodyAsJson<ErrorDetails>()
        assertEquals(response.statusCode(), errorDetails.code)

        assertFalse(errorDetails.message.isEmpty())
        assertEquals("CustomTestExceptionMessage", errorDetails.message)
    }

    @Test
    fun shouldThrow422andTheValidationDetails() = runBlocking<Unit> {

        val bean = BeanToValidate(null, null)

        val response = client.post(port, "localhost", "/core/test/validationException").sendJsonAwait(bean)
        assertEquals(422, response.statusCode())

        val errorDetails = response.bodyAsJson<ErrorDetails>()
        assertEquals(response.statusCode(), errorDetails.code)

        //assertEquals("Input validation failed", errorDetails.message)
        assertFalse(errorDetails.details.isEmpty())
        assertEquals(2, errorDetails.details.size)
        assertEquals("id should not be null", errorDetails.details["id"]!![0])
        assertEquals("name should not be null", errorDetails.details["name"]!![0])
    }


    @Test
    fun shouldUseMultipleThreads() = runBlocking<Unit> {

        val messages = 100
        val count = CountDownLatch(messages)

        for (i in 0..messages) {
                    Thread({
                        val urlString = "http://127.0.0.1:${port}/core/test/slow"
                        val url = URL(urlString)
                        val conn = url.openConnection()
                        val stream = conn.getInputStream()
                        read(stream)
                        stream.close()
                        count.countDown()
                    }).start()
                }
        count.await()
    }

    fun read(input: InputStream): String {
        val reader = BufferedReader(InputStreamReader(input))
        return reader.lines().collect(Collectors.joining("\n"))
    }

    @Synchronized private fun getFreePort(): Int {
        try {
            ServerSocket(0).use { socket ->
                socket.reuseAddress = true
                return socket.localPort
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }
}