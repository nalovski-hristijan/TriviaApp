package com.hnalovski.triviaapp.data

import com.hnalovski.triviaapp.model.Question

interface AnswerListAsyncResponse {
    fun processFinished(questionArrayList: ArrayList<Question?>?)
}
