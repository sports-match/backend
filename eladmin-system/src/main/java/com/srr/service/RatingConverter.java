package com.srr.service;

/**
 * Converts ratings between external systems (e.g., UBR) and the internal SRR scale.
 */
public interface RatingConverter {

    /**
     * Converts a UBR rating value into the SRR scale.
     * @param ubrRating rating on the UBR 3 500–8 500 scale
     * @return equivalent SRR rating (800–3 000)
     */
    double ubrToSrr(double ubrRating);

    /**
     * (Optional) Converts an SRR rating back to UBR scale.
     * @param srr rating on the SRR scale
     * @return equivalent UBR rating
     */
    double srrToUbr(double srr);
}
