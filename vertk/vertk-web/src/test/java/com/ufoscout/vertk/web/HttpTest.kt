package com.ufoscout.vertk.web

import com.ufoscout.vertk.deployVerticleAwait
import com.ufoscout.vertk.web.client.bodyAsJson
import com.ufoscout.vertk.web.verticle.HttpVerticle
import com.ufoscout.vertk.web.verticle.RequestDTO
import com.ufoscout.vertk.web.verticle.ResponseDTO
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.*

class HttpTest: BaseTest() {

    companion object {
        @BeforeAll @JvmStatic
        fun deployStubVerticle() = runBlocking<Unit> {
            vertk.deployVerticleAwait<HttpVerticle>()
        }
    }

    @Test
    fun shouldCallDelete() = runBlocking<Unit> {
        val response = client.delete(port, "localhost", HttpVerticle.path)
                .sendAwait()
        val body = response.bodyAsJson(ResponseDTO::class.java)
        assertNotNull(response)
        assertNotNull(body)
        assertEquals(200, response.statusCode())
        assertEquals("DELETE", body!!.message)
    }

    @Test
    fun shouldCallGet() = runBlocking<Unit> {
        val response = client.get(port, "localhost", HttpVerticle.path)
                .sendAwait()
        val body = response.bodyAsJson<ResponseDTO>()

        assertNotNull(response)
        assertNotNull(body)
        assertEquals(200, response.statusCode())
        assertEquals("GET", body!!.message)
    }

    @Test
    fun shouldCallPatch() = runBlocking<Unit> {
        val request = RequestDTO(UUID.randomUUID().toString())

        val response = client.patch(port, "localhost", HttpVerticle.path)
                .sendJsonAwait(request)

        val body = response.bodyAsJson(ResponseDTO::class.java)

        assertNotNull(response)
        assertNotNull(body)
        assertEquals(200, response.statusCode())
        assertEquals("PATCH-${request.message}", body!!.message)
    }

    @Test
    fun shouldCallPost() = runBlocking<Unit> {
        val request = RequestDTO(UUID.randomUUID().toString())

        val response = client.post(port, "localhost", HttpVerticle.path)
                .sendJsonAwait(request)

        val body = response.bodyAsJson(ResponseDTO::class.java)

        assertNotNull(response)
        assertNotNull(body)
        assertEquals(200, response.statusCode())
        assertEquals("POST-${request.message}", body!!.message)
    }

    @Test
    fun shouldCallPut() = runBlocking<Unit> {
        val request = RequestDTO(UUID.randomUUID().toString())
        val response = client.put(port, "localhost", HttpVerticle.path)
                .sendJsonAwait(request)

        val body = response.bodyAsJson(ResponseDTO::class.java)
        assertNotNull(response)
        assertNotNull(body)
        assertEquals(200, response.statusCode())
        assertEquals("PUT-${request.message}", body!!.message)
    }
}