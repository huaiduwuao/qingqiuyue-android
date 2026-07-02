package com.qingqiuyue.app

import com.qingqiuyue.app.data.api.dto.LoginRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class APIServiceTest {

    @Test
    fun loginRequest_serializes() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(LoginRequest::class.java)
        val json = adapter.toJson(LoginRequest("alice", "secret"))
        assertEquals(true, json.contains("alice"))
        assertEquals(true, json.contains("secret"))
    }
}