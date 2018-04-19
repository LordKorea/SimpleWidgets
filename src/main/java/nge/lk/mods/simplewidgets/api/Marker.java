package nge.lk.mods.simplewidgets.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a marker in the world.
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Marker {

    /**
     * The color of the marker.
     */
    private int color;

    /**
     * The x position of the marker.
     */
    private int worldX;

    /**
     * The z position of the marker.
     */
    private int worldZ;

    /**
     * Whether this marker is enabled.
     */
    private boolean enabled;
}
