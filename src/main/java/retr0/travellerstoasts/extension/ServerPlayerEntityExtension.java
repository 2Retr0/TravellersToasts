package retr0.travellerstoasts.extension;

import static net.minecraft.SharedConstants.TICKS_PER_MINUTE;

public interface ServerPlayerEntityExtension {
    void beginTracking(int maxInhabitedTimeTicks);

    void stopTracking(boolean finishedQuery);
}
