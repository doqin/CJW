@file:JvmName("RPSController")

package org.example.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class RPSController {
    @GetMapping("/rps")
    fun matchmake() {
    }
}
