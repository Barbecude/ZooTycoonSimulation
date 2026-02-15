package com.example.simulation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class ZooData extends SavedData {
    private int balance = 5000;

    public static ZooData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(ZooData::load, ZooData::new, "indozoo_data");
        }
        return new ZooData(); // Client side fallback
    }

    public ZooData() {
        this.balance = 5000;
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

    public static ZooData load(CompoundTag tag) {
        ZooData data = new ZooData();
        data.balance = tag.getInt("Balance");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Balance", balance);
        return tag;
    }
}
