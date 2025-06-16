package com.srr.service.impl;

import com.srr.domain.PlayerAnswer;
import com.srr.domain.PlayerSportRating;
import com.srr.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    public static final double MIN_ELO = 800.0;
    public static final double MAX_ELO = 3000.0;
    public static final double INITIAL_ASSESSMENT_MAX = 1400.0;

    @Override
    public double calculateInitialRating(List<PlayerAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return MIN_ELO;
        }
        int total = answers.stream().mapToInt(PlayerAnswer::getAnswerValue).sum();
        double rating;
        if (total <= 9) {
            rating = 800;
        } else if (total <= 15) {
            rating = 1000;
        } else if (total <= 18) {
            rating = 1200;
        } else {
            rating = 1400;
        }
        rating = Math.max(MIN_ELO, rating);
        rating = Math.min(INITIAL_ASSESSMENT_MAX, rating);
        return rating;
    }

    // ===== Doubles rating update =====

    @Override
    public void updateRatingsForDoubles(List<PlayerSportRating> teamARatings,
                                        List<PlayerSportRating> teamBRatings,
                                        int scoreA,
                                        int scoreB) {
        // Validation
        if (teamARatings.size() != 2 || teamBRatings.size() != 2) {
            throw new IllegalArgumentException("Each team must have exactly 2 players for doubles rating update");
        }

        validateScores(scoreA, scoreB);

        double teamAvgA = (teamARatings.get(0).getRateScore() + teamARatings.get(1).getRateScore()) / 2.0;
        double teamAvgB = (teamBRatings.get(0).getRateScore() + teamBRatings.get(1).getRateScore()) / 2.0;

        double expectedA = expectedScore(teamAvgA, teamAvgB);
        double expectedB = 1 - expectedA;

        double actualA;
        double actualB;
        if (scoreA > scoreB) {
            actualA = 1.0;
            actualB = 0.0;
        } else if (scoreA < scoreB) {
            actualA = 0.0;
            actualB = 1.0;
        } else { // draw
            actualA = 0.5;
            actualB = 0.5;
        }

        double multiplier = calculateMarginMultiplier(scoreA, scoreB);

        // Update each player with their own k-factor
        for (PlayerSportRating pr : teamARatings) {
            double delta = kFactor(pr) * multiplier * (actualA - expectedA);
            pr.setRateScore(clamp(pr.getRateScore() + delta, MIN_ELO, MAX_ELO));
        }
        for (PlayerSportRating pr : teamBRatings) {
            double delta = kFactor(pr) * multiplier * (actualB - expectedB);
            pr.setRateScore(clamp(pr.getRateScore() + delta, MIN_ELO, MAX_ELO));
        }
    }

    // ===== Singles SRR-v1 update =====
    private static final int PROVISIONAL_GAMES = 15;
    private static final int K_PROV = 40;
    private static final int K_STD = 24;
    private static final int K_EXP = 16;
    private static final double SCALE = 400.0;

    @Override
    public void updateRatingsForSingles(PlayerSportRating playerRating,
                                        PlayerSportRating opponentRating,
                                        int gamesPlayedPlayer,
                                        int gamesPlayedOpponent,
                                        int scoreP,
                                        int scoreO) {
        validateScores(scoreP, scoreO);

        boolean playerWon = scoreP > scoreO;
        double expectedP = expectedScore(playerRating.getRateScore(), opponentRating.getRateScore());
        double marginMultiplier = calculateMarginMultiplier(scoreP, scoreO);

        double deltaP = kFactor(playerRating) * marginMultiplier * ((playerWon ? 1.0 : 0.0) - expectedP);
        double deltaO = kFactor(opponentRating) * marginMultiplier * ((playerWon ? 0.0 : 1.0) - (1.0 - expectedP));

        playerRating.setRateScore(clamp(playerRating.getRateScore() + deltaP, MIN_ELO, MAX_ELO));
        opponentRating.setRateScore(clamp(opponentRating.getRateScore() + deltaO, MIN_ELO, MAX_ELO));
    }

    private double expectedScore(double rOwn, double rOpp) {
        return 1.0 / (1.0 + Math.pow(10.0, (rOpp - rOwn) / SCALE));
    }

    private int kFactor(double rating, int gamesPlayed) {
        if (gamesPlayed < PROVISIONAL_GAMES) return K_PROV;
        if (rating > 2000.0) return K_EXP;
        return K_STD;
    }

    private int kFactor(PlayerSportRating pr) {
        // Use provisional flag if available
        if (Boolean.TRUE.equals(pr.getProvisional())) return K_PROV;
        //if (pr.getRateScore() > 2000.0) return K_EXP;
        return K_STD;
    }

    private double calculateMarginMultiplier(int scoreP, int scoreO) {
        int diff = Math.abs(scoreP - scoreO);
        return 1.0 + Math.sqrt(diff / 21.0);
    }

    private void validateScores(int scoreP, int scoreO) {
        if (scoreP < 0 || scoreO < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }
        if ((scoreP < 21 && scoreO < 21) || Math.abs(scoreP - scoreO) < 2) {
            throw new IllegalArgumentException("Invalid score line: " + scoreP + "-" + scoreO);
        }
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
