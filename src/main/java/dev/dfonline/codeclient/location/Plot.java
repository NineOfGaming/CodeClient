package dev.dfonline.codeclient.location;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class Plot extends Location {
    protected Integer id;
    protected String name;
    protected String owner;

    protected Integer originX;
    protected Integer originZ;

    protected Boolean hasBuild;
    protected Boolean hasDev;

    protected Size size;

    public void setOrigin(int x, int z) {
        this.originX = x;
        this.originZ = z;
    }

    public void copyValuesFrom(Plot plot) {
        this.id = plot.id;
        this.name = plot.name;
        this.owner = plot.owner;
        this.originX = plot.originX;
        this.originZ = plot.originZ;
        this.hasBuild = plot.hasBuild;
        this.hasDev = plot.hasDev;
        this.size = plot.size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Size getSize() {
        return size;
    }

    public Integer getX() {
        return originX;
    }

    public Integer getZ() {
        return originZ;
    }

    public Boolean isInPlot(BlockPos pos) {
        if(size == null) return null;

        return isInArea(pos.toCenterPos()) || isInDev(pos.toCenterPos());
    }

    /**
     * The play or build area.
     */
    public Boolean isInArea(Vec3d pos) {
        if(size == null) return null;

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = (x >= originX) && (x <= originX + size.size + 1);
        boolean inZ = (z >= originZ) && (z <= originZ + size.size + 1);

        return inX && inZ;
    }
    public Boolean isInDev(Vec3d pos) {
        if(size == null) return null;

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = (x < originX) && ((x >= originX - 20) || ((pos.getY() >= 50) && (x >= originX - 21)));
        boolean inZ = (z >= originZ) && (z <= originZ + size.size + 1);

        return inX && inZ;
    }

    public enum Size {
        BASIC(50),
        LARGE(100),
        MASSIVE(300);

        public final int size;
        Size(int size) {
            this.size = size;
        }
    }
}
