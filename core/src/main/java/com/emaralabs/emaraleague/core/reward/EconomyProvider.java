package com.emaralabs.emaraleague.core.reward;

/**
 * Abstraction for economy operations — allows core to remain decoupled
 * from Vault/specific economy plugins.
 * P1-004 FIX: Replaces fragile reflection-based Vault access with
 * constructor-injected dependency. Implementation provided by bootstrap module.
 */
public interface EconomyProvider {

    /**
     * Check if economy is available.
     */
    boolean isAvailable();

    /**
     * Deposit money to a player's account.
     * @return true if transaction succeeded
     */
    boolean deposit(String playerName, double amount);

    /**
     * No-op implementation when no economy plugin is present.
     */
    EconomyProvider NONE = new EconomyProvider() {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public boolean deposit(String playerName, double amount) {
            return false;
        }
    };
}
