package com.hnalovski.triviaapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.hnalovski.triviaapp.data.AnswerListAsyncResponse
import com.hnalovski.triviaapp.data.Repository
import com.hnalovski.triviaapp.databinding.ActivityMainBinding
import com.hnalovski.triviaapp.model.Question
import com.hnalovski.triviaapp.model.Score
import com.hnalovski.triviaapp.util.Prefs
import java.text.MessageFormat

class MainActivity : AppCompatActivity() {
    var questionList: MutableList<Question?>? = null
    private var binding: ActivityMainBinding? = null
    private var currentQuestionIndex = 0
    private var scoreCounter = 0
    private var score: Score? = null
    private var prefs: Prefs? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        score = Score(0)
        prefs = Prefs(this@MainActivity)

        // Retrieve the last state
        currentQuestionIndex = prefs!!.state

        binding!!.highestScoreText.text = MessageFormat.format(
            "Highest: {0}",
            prefs!!.highestScore
        ).toString()
        binding!!.scoreText.text = MessageFormat.format(
            "Current score: {0}",
            score!!.score.toString()
        )

        questionList =
            Repository().getQuestions({ questionArrayList: ArrayList<Question?>? ->
                binding!!.questionTextview.text = questionArrayList!![currentQuestionIndex]!!.answer
                updateCounter(questionArrayList)
            } as AnswerListAsyncResponse?

            )

        binding!!.buttonShare.setOnClickListener { _: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "I am playing Trivia")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "My current score: " + score!!.score + ". And my highest is: " + prefs!!.highestScore
            )
            startActivity(intent)
        }

        binding!!.buttonNext.setOnClickListener { _: View? ->
            this.nextQuestion
        }

        binding!!.buttonTrue.setOnClickListener { _: View? ->
            checkAnswer(true)
            updateQuestion()
        }

        binding!!.buttonFalse.setOnClickListener { _: View? ->
            checkAnswer(false)
            updateQuestion()
        }
    }

    private val nextQuestion: Unit
        get() {
            currentQuestionIndex = (currentQuestionIndex + 1) % questionList!!.size
            updateQuestion()
        }

    @SuppressLint("ResourceType")
    private fun checkAnswer(userChoseCorrect: Boolean) {
        val answer = questionList!![currentQuestionIndex]!!.answerTrue
        var snackMessageId: Int
        if (userChoseCorrect == answer) {
            snackMessageId = R.string.correct_answer
            fadeAnimation()
            addPoints()
            val mp = MediaPlayer.create(applicationContext, R.raw.correct_sound_effect)
            mp.start()
        } else {
            snackMessageId = R.string.incorrect_answer
            shakeAnimation()
            deductPoints()
            val mp = MediaPlayer.create(applicationContext, R.raw.wrong_sound_effect)
            mp.start()
        }
        Snackbar.make(binding!!.cardView, snackMessageId, Snackbar.LENGTH_SHORT).show()
    }

    private fun updateCounter(questionArrayList: ArrayList<Question?>) {
        binding!!.textViewOutOf.text = MessageFormat.format(
            "Question: {0}/{1}",
            currentQuestionIndex,
            questionArrayList.size
        )
    }

    private fun fadeAnimation() {
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.duration = 300
        alphaAnimation.repeatCount = 1
        alphaAnimation.repeatMode = Animation.REVERSE

        binding!!.cardView.animation = alphaAnimation

        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding!!.questionTextview.setTextColor(Color.GREEN)
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding!!.questionTextview.setTextColor(Color.WHITE)
                nextQuestion
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }

    private fun updateQuestion() {
        val question = questionList!![currentQuestionIndex]!!.answer
        binding!!.questionTextview.text = question
        updateCounter((questionList as java.util.ArrayList<Question?>?)!!)
    }

    private fun shakeAnimation() {
        val shake = AnimationUtils.loadAnimation(this@MainActivity, R.anim.shake_animation)
        binding!!.cardView.animation = shake

        shake.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding!!.questionTextview.setTextColor(Color.RED)
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding!!.questionTextview.setTextColor(Color.WHITE)
                nextQuestion
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }

    private fun deductPoints() {
        if (scoreCounter > 0) {
            scoreCounter -= 100
            score!!.score = scoreCounter
            binding!!.scoreText.text = MessageFormat.format(
                "Current score: {0}",
                score!!.score.toString()
            )
        } else {
            scoreCounter = 0
            score!!.score = scoreCounter
        }
    }

    private fun addPoints() {
        scoreCounter += 100
        score!!.score = scoreCounter
        binding!!.scoreText.text = score!!.score.toString()
        binding!!.scoreText.text = MessageFormat.format(
            "Current score: {0}",
            score!!.score.toString()
        )
    }

    override fun onPause() {
        prefs!!.savedHighestScore(score!!.score)
        prefs!!.state = currentQuestionIndex
        Log.d("Pause", "onPause: saving score " + prefs!!.highestScore)
        super.onPause()
    }
}