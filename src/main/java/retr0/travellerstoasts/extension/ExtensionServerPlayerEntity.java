package retr0.travellerstoasts.extension;

public interface ExtensionServerPlayerEntity {
    /**
     * Begins tracking the current chunk's inhabited time only sending a response back to the client when a chunk with
     * an inhabited time less than the input argument is found.
     */
    void travellersToasts$beginTracking(int maxInhabitedTimeTicks);

    /**
     * Cancels tracking for a player informing the respective client of the cancellation.
     * @param finishedQuery {@code true} if original request was successfully filed; otherwise, {@code false}.
     */
    void travellersToasts$stopTracking(boolean finishedQuery);
}
