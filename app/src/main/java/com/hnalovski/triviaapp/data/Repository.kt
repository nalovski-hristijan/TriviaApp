package com.hnalovski.triviaapp.data

import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.hnalovski.triviaapp.controller.AppController
import com.hnalovski.triviaapp.model.Question
import org.json.JSONArray
import org.json.JSONException

class Repository {
    var questionArrayList: ArrayList<Question?> = ArrayList<Question?>()

    var url: String =
        "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json"

    fun getQuestions(callBack: AnswerListAsyncResponse?): MutableList<Question?> {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response: JSONArray? ->
                for (i in 0..<response!!.length()) {
                    try {
                        val question = Question(
                            response.getJSONArray(i).get(0).toString(),
                            response.getJSONArray(i).getBoolean(1)
                        )

                        //Add questions to arraylist/list
                        questionArrayList.add(question)

                        Log.d("Hello", "getQuestions: " + questionArrayList)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                callBack?.processFinished(questionArrayList)
            },
            { error: VolleyError? -> })
        AppController.getInstance().addToRequestQueue<JSONArray?>(jsonArrayRequest)


        return questionArrayList
    }
}
