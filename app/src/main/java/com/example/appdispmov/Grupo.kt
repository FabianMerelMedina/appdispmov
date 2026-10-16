package com.example.appdispmov

data class Grupo(
    var id: String = "",
    var nombre: String = "",
    var propietarioId: String = "",
    var miembros: List<String> = emptyList(),
    var noLeidos: Map<String, Long> = emptyMap()
)