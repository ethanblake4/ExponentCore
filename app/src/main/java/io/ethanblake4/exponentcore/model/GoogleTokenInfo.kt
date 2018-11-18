package io.ethanblake4.exponentcore.model

data class GoogleTokenInfo (
        val token: String,
        val SID: String?,
        val LSID: String?,
        val services: List<String>?
)