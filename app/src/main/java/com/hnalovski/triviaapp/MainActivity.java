package com.hnalovski.triviaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.hnalovski.triviaapp.data.Repository;
import com.hnalovski.triviaapp.databinding.ActivityMainBinding;
import com.hnalovski.triviaapp.model.Question;
import com.hnalovski.triviaapp.model.Score;
import com.hnalovski.triviaapp.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<Question> questionList;
    private ActivityMainBinding binding;
    private int currentQuestionIndex = 0;
    private int scoreCounter = 0;
    private Score score;
    private Prefs prefs;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        score = new Score();
        prefs = new Prefs(MainActivity.this);

        // Retrieve the last state
        currentQuestionIndex = prefs.getState();

        binding.highestScoreText.setText(String.valueOf(MessageFormat.format("Highest: {0}", prefs.getHighestScore())));
        binding.scoreText.setText(MessageFormat.format("Current score: {0}", String.valueOf(score.getScore())));

        questionList = new Repository().getQuestions(questionArrayList -> {
                    binding.questionTextview.setText(questionArrayList.get(currentQuestionIndex).getAnswer()
                    );
                    updateCounter(questionArrayList);
                }

        );

        binding.buttonShare.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "I am playing Trivia");
            intent.putExtra(Intent.EXTRA_TEXT, "My current score: "+ score.getScore() + ". And my highest is: " + prefs.getHighestScore());
            startActivity(intent);
        });

        binding.buttonNext.setOnClickListener(view -> {
            getNextQuestion();
        });

        binding.buttonTrue.setOnClickListener(view -> {
            checkAnswer(true);
            updateQuestion();
        });

        binding.buttonFalse.setOnClickListener(view -> {
            checkAnswer(false);
            updateQuestion();
        });


    }

    private void getNextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }

    @SuppressLint("ResourceType")
    private void checkAnswer(boolean userChoseCorrect) {
        boolean answer = questionList.get(currentQuestionIndex).isAnswerTrue();
        int snackMessageId = 0;
        if (userChoseCorrect == answer) {
            snackMessageId = R.string.correct_answer;
            fadeAnimaton();
            addPoints();
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.correct_sound_effect);
            mp.start();
        } else {
            snackMessageId = R.string.incorrect_answer;
            shakeAnimation();
            deductPoints();
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.wrong_sound_effect);
            mp.start();
        }
        Snackbar.make(binding.cardView, snackMessageId, Snackbar.LENGTH_SHORT).show();
    }

    private void updateCounter(ArrayList<Question> questionArrayList) {
        binding.textViewOutOf.setText(MessageFormat.format("Question: {0}/{1}", currentQuestionIndex, questionArrayList.size()));
    }

    private void fadeAnimaton() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        binding.cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextview.setTextColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextview.setTextColor(Color.WHITE);
                getNextQuestion();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        binding.questionTextview.setText(question);
        updateCounter((ArrayList<Question>) questionList);
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        binding.cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextview.setTextColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextview.setTextColor(Color.WHITE);
                getNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void deductPoints() {
        if (scoreCounter > 0) {
            scoreCounter -= 100;
            score.setScore(scoreCounter);
            binding.scoreText.setText(MessageFormat.format("Current score: {0}", String.valueOf(score.getScore())));
        } else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
        }
    }

    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);
        binding.scoreText.setText(String.valueOf(score.getScore()));
        binding.scoreText.setText(MessageFormat.format("Current score: {0}", String.valueOf(score.getScore())));

    }

    @Override
    protected void onPause() {
        prefs.savedHighestScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        Log.d("Pause", "onPause: saving score " + prefs.getHighestScore());
        super.onPause();
    }
}