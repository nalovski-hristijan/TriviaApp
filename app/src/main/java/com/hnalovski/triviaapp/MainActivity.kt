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
        currentQuestionIndex = prefs!!.getState()

        binding!!.highestScoreText.setText(
            MessageFormat.format(
                "Highest: {0}",
                prefs!!.getHighestScore()
            ).toString()
        )
        binding!!.scoreText.setText(
            MessageFormat.format(
                "Current score: {0}",
                score!!.score.toString()
            )
        )

        questionList =
            Repository().getQuestions(AnswerListAsyncResponse { questionArrayList: ArrayList<Question?>? ->
                binding!!.questionTextview.setText(
                    questionArrayList!!.get(currentQuestionIndex)!!.answer
                )
                updateCounter(questionArrayList)
            }

            )

        binding!!.buttonShare.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_SUBJECT, "I am playing Trivia")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "My current score: " + score!!.score + ". And my highest is: " + prefs!!.getHighestScore()
            )
            startActivity(intent)
        })

        binding!!.buttonNext.setOnClickListener(View.OnClickListener { view: View? ->
            this.nextQuestion
        })

        binding!!.buttonTrue.setOnClickListener(View.OnClickListener { view: View? ->
            checkAnswer(true)
            updateQuestion()
        })

        binding!!.buttonFalse.setOnClickListener(View.OnClickListener { view: View? ->
            checkAnswer(false)
            updateQuestion()
        })
    }

    private val nextQuestion: Unit
        get() {
            currentQuestionIndex = (currentQuestionIndex + 1) % questionList!!.size
            updateQuestion()
        }

    @SuppressLint("ResourceType")
    private fun checkAnswer(userChoseCorrect: Boolean) {
        val answer = questionList!!.get(currentQuestionIndex)!!.answerTrue
        var snackMessageId = 0
        if (userChoseCorrect == answer) {
            snackMessageId = R.string.correct_answer
            fadeAnimaton()
            addPoints()
            val mp = MediaPlayer.create(getApplicationContext(), R.raw.correct_sound_effect)
            mp.start()
        } else {
            snackMessageId = R.string.incorrect_answer
            shakeAnimation()
            deductPoints()
            val mp = MediaPlayer.create(getApplicationContext(), R.raw.wrong_sound_effect)
            mp.start()
        }
        Snackbar.make(binding!!.cardView, snackMessageId, Snackbar.LENGTH_SHORT).show()
    }

    private fun updateCounter(questionArrayList: ArrayList<Question?>) {
        binding!!.textViewOutOf.setText(
            MessageFormat.format(
                "Question: {0}/{1}",
                currentQuestionIndex,
                questionArrayList.size
            )
        )
    }

    private fun fadeAnimaton() {
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.setDuration(300)
        alphaAnimation.setRepeatCount(1)
        alphaAnimation.setRepeatMode(Animation.REVERSE)

        binding!!.cardView.setAnimation(alphaAnimation)

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
        val question = questionList!!.get(currentQuestionIndex)!!.answer
        binding!!.questionTextview.setText(question)
        updateCounter((questionList as java.util.ArrayList<Question?>?)!!)
    }

    private fun shakeAnimation() {
        val shake = AnimationUtils.loadAnimation(this@MainActivity, R.anim.shake_animation)
        binding!!.cardView.setAnimation(shake)

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
            binding!!.scoreText.setText(
                MessageFormat.format(
                    "Current score: {0}",
                    score!!.score.toString()
                )
            )
        } else {
            scoreCounter = 0
            score!!.score = scoreCounter
        }
    }

    private fun addPoints() {
        scoreCounter += 100
        score!!.score = scoreCounter
        binding!!.scoreText.setText(score!!.score.toString())
        binding!!.scoreText.setText(
            MessageFormat.format(
                "Current score: {0}",
                score!!.score.toString()
            )
        )
    }

    override fun onPause() {
        prefs!!.savedHighestScore(score!!.score)
        prefs!!.state = currentQuestionIndex
        Log.d("Pause", "onPause: saving score " + prefs!!.getHighestScore())
        super.onPause()
    }
}