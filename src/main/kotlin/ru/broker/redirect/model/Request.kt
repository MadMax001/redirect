package ru.broker.redirect.model

import java.util.UUID

data class Request (
  val url: String,
  val gpbId: UUID?,
  val clientIP: String?,
  val clientAgent: String?
)