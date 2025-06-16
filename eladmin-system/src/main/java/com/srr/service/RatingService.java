package com.srr.service;

import com.srr.domain.PlayerAnswer;
import java.util.List;

/**
 * Centralised service for all player rating calculations.
 */
public interface RatingService {
    /**
     * Calculates the initial rating based on player's self-assessment answers.
     *
     * @param answers list of {@link PlayerAnswer} provided in assessment
     * @return initial rating value bounded by configured limits
     */
    double calculateInitialRating(List<PlayerAnswer> answers);

    /**
     * Updates ratings for a doubles match.
     * @param teamARatings list of PlayerSportRating for team A (size 2)
     * @param teamBRatings list of PlayerSportRating for team B (size 2)
     * @param scoreA points scored by team A
     * @param scoreB points scored by team B
     */
    void updateRatingsForDoubles(java.util.List<com.srr.domain.PlayerSportRating> teamARatings,
                                 java.util.List<com.srr.domain.PlayerSportRating> teamBRatings,
                                 int scoreA,
                                 int scoreB);

    /**
     * Updates ratings for a singles match using SRR-v1 algorithm.
     * @param playerRating rating record for player
     * @param opponentRating rating record for opponent
     * @param gamesPlayedPlayer games already played by player
     * @param gamesPlayedOpponent games already played by opponent
     * @param scoreP points scored by player
     * @param scoreO points scored by opponent
     */
    void updateRatingsForSingles(com.srr.domain.PlayerSportRating playerRating,
                                 com.srr.domain.PlayerSportRating opponentRating,
                                 int gamesPlayedPlayer,
                                 int gamesPlayedOpponent,
                                 int scoreP,
                                 int scoreO);
}
