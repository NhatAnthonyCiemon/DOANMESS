package com.example.doanmess

import kotlinx.coroutines.sync.Mutex

object MutexProvider {
    val mutex = Mutex()
}