package com.example.misc.scheduler

abstract class SkyblockRunnable : Runnable {
    var thisTask: ScheduledTask? = null
    fun cancel() {
        NewScheduler.INSTANCE.cancel(thisTask!!)
    }
}