package com.hnalovski.triviaapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val answer: String,
    val answerTrue: Boolean
)
