package com.example.broadcast

class Member {
     var ip = ""
    var latency: Long = 100
    var difference: Long = 100

    constructor(_ip: String, _latency: Long, _difference: Long) {
        ip = _ip
        latency = _latency
        difference = _difference
    }

    constructor(_ip: String) {
        ip = _ip
        latency = 100
        difference = 100
    }
}