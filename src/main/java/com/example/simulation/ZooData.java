package com.example.simulation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class ZooData extends SavedData {
    private int balance = 0;
    private int ticketPrice = 20_000;
    private int marketingLevel = 1;

    private boolean zooBannerPurchased = false;
    private java.util.List<net.minecraft.core.BlockPos> entrances = new java.util.ArrayList<>();

    // Global Stats
    private int animalCount = 0;
    private int staffCount = 0;
    private int visitorCount = 0;
    private int trashCount = 0;
    private int rating = 100;

    // Tagged Animals List (ID, Name, Type)
    // Storing as CompoundTag list for simplicity
    private net.minecraft.nbt.ListTag taggedAnimals = new net.minecraft.nbt.ListTag();

    public static ZooData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(ZooData::load, ZooData::new, "indozoo_data");
        }
        return new ZooData(); // Client side fallback
    }

    public ZooData() {
        this.balance = 100_000_000;
        this.ticketPrice = 20_000;
        this.marketingLevel = 1;
        this.zooBannerPurchased = false;
        setDirty();
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
        setDirty();
    }

    public void addBalance(int amount) {
        this.balance += amount;
        setDirty();
    }

    public int getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(int price) {
        this.ticketPrice = price;
        setDirty();
    }

    public int getMarketingLevel() {
        return marketingLevel;
    }

    public void setMarketingLevel(int level) {
        this.marketingLevel = level;
        setDirty();
    }

    public boolean isZooBannerPurchased() {
        return zooBannerPurchased;
    }

    public void setZooBannerPurchased(boolean purchased) {
        this.zooBannerPurchased = purchased;
        setDirty();
    }

    public void addEntrance(net.minecraft.core.BlockPos pos) {
        if (!entrances.contains(pos)) {
            entrances.add(pos);
            setDirty();
        }
    }

    public void removeEntrance(net.minecraft.core.BlockPos pos) {
        if (entrances.contains(pos)) {
            entrances.remove(pos);
            setDirty();
        }
    }

    public java.util.List<net.minecraft.core.BlockPos> getEntrances() {
        return entrances;
    }

    public net.minecraft.core.BlockPos getRandomEntrance() {
        if (entrances.isEmpty())
            return null;
        return entrances.get(new java.util.Random().nextInt(entrances.size()));
    }

    public int getAnimalCount() {
        return animalCount;
    }

    public void setAnimalCount(int count) {
        this.animalCount = count;
        setDirty();
    }

    public void removeAnimal(int entityId) {
        for (int i = 0; i < taggedAnimals.size(); i++) {
            if (taggedAnimals.getCompound(i).getInt("id") == entityId) {
                taggedAnimals.remove(i);
                this.animalCount = taggedAnimals.size();
                setDirty();
                return;
            }
        }
    }

    public void addAnimal(int entityId, String name, String type) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", entityId);
        tag.putString("name", name);
        tag.putString("type", type);

        // Remove existing if any (update)
        removeAnimal(entityId); // Reuse remove logic

        taggedAnimals.add(tag);
        this.animalCount = taggedAnimals.size();
        setDirty();
    }

    public net.minecraft.nbt.ListTag getTaggedAnimals() {
        return taggedAnimals;
    }

    public int getStaffCount() {
        return staffCount;
    }

    public void setStaffCount(int count) {
        this.staffCount = count;
        setDirty();
    }

    public int getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(int count) {
        this.visitorCount = count;
        setDirty();
    }

    public int getTrashCount() {
        return trashCount;
    }

    public void setTrashCount(int count) {
        this.trashCount = count;
        setDirty();
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = Math.max(0, Math.min(100, rating));
        setDirty();
    }

    public void updateCounts(ServerLevel level) {
        int vCount = 0;
        int sCount = 0;
        int aCount = 0;

        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e.isRemoved())
                continue;
            if (e instanceof VisitorEntity)
                vCount++;
            if (e instanceof StaffEntity)
                sCount++;
        }

        setVisitorCount(vCount);
        setStaffCount(sCount);
        // Only count tagged animals managed by ZooData
        setAnimalCount(this.taggedAnimals.size());
    }

    public static ZooData load(CompoundTag tag) {
        ZooData data = new ZooData();
        data.balance = tag.getInt("Balance");
        if (tag.contains("TicketPrice"))
            data.ticketPrice = tag.getInt("TicketPrice");
        if (tag.contains("MarketingLevel"))
            data.marketingLevel = tag.getInt("MarketingLevel");

        if (tag.contains("ZooBannerPurchased"))
            data.zooBannerPurchased = tag.getBoolean("ZooBannerPurchased");

        if (tag.contains("Entrances")) {
            net.minecraft.nbt.ListTag list = tag.getList("Entrances", 10); // 10 = Compound
            for (int i = 0; i < list.size(); i++) {
                data.entrances.add(net.minecraft.nbt.NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
        data.animalCount = tag.getInt("AnimalCount");
        if (tag.contains("TaggedAnimals")) {
            data.taggedAnimals = tag.getList("TaggedAnimals", 10);
            data.animalCount = data.taggedAnimals.size(); // Sync count
        }
        data.staffCount = tag.getInt("StaffCount");
        data.visitorCount = tag.getInt("VisitorCount");
        data.trashCount = tag.getInt("TrashCount");
        if (tag.contains("Rating")) data.rating = tag.getInt("Rating");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Balance", balance);
        tag.putInt("TicketPrice", ticketPrice);
        tag.putInt("MarketingLevel", marketingLevel);
        tag.putBoolean("ZooBannerPurchased", zooBannerPurchased);

        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (net.minecraft.core.BlockPos pos : entrances) {
            list.add(net.minecraft.nbt.NbtUtils.writeBlockPos(pos));
        }
        tag.put("Entrances", list);
        tag.putInt("AnimalCount", animalCount);
        tag.putInt("StaffCount", staffCount);
        tag.putInt("VisitorCount", visitorCount);
        tag.putInt("TrashCount", trashCount);
        tag.putInt("Rating", rating);
        tag.put("TaggedAnimals", taggedAnimals);

        return tag;
    }
}
