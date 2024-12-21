package com.asaad27.life

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform