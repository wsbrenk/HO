package core.model.player;

/**
 * Type of recovery estimate.
 * REALISTIC - if date should match the real recovery
 * OPTIMISTIC - if date of real recovery is most likely after the estimated date
 * PESSIMISTIC  - if date of real recovery is most likely before the estimated date
 */
public enum TypeOfRecoveryEstimation {
    REALISTIC_ESTIMATE,
    OPTIMISTIC_ESTIMATE,
    PESSIMISTIC_ESTIMATE,
}
