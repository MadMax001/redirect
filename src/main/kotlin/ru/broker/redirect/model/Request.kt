package ru.broker.redirect.model

import java.util.UUID

data class Request (
  val url: String,
  val requestId: UUID?
)