package com.example.simulation;

import net.minecraft.nbt.ListTag;

public class ClientZooData {
    private static int balance = 100_000_000;
    private static int animalCount = 0;
    private static int staffCount = 0;
    private static int visitorCount = 0;
    private static int rating = 100;
    private static ListTag taggedAnimals = new ListTag();

    // ERROR FIX: Added this field so ZooComputerScreen can check for updates
    public static long lastUpdate = 0;

    public static int getBalance() {
        return balance;
    }

    public static void setBalance(int newBalance) {
        if (balance != newBalance) {
            balance = newBalance;
            markDirty();
        }
    }

    public static int getAnimalCount() {
        return animalCount;
    }

    public static void setAnimalCount(int count) {
        if (animalCount != count) {
            animalCount = count;
            markDirty();
        }
    }

    public static int getStaffCount() {
        return staffCount;
    }

    public static void setStaffCount(int count) {
        if (staffCount != count) {
            staffCount = count;
            markDirty();
        }
    }

    public static int getVisitorCount() {
        return visitorCount;
    }

    public static void setVisitorCount(int count) {
        if (visitorCount != count) {
            visitorCount = count;
            markDirty();
        }
    }

    public static int getRating() {
        return rating;
    }

    public static void setRating(int r) {
        if (rating != r) {
            rating = r;
            markDirty();
        }
    }

    public static ListTag getTaggedAnimals() {
        return taggedAnimals;
    }

    public static void setTaggedAnimals(ListTag list) {
        taggedAnimals = list;
        markDirty();
    }

    // Update the timestamp whenever data changes
    private static void markDirty() {
        lastUpdate = System.currentTimeMillis();
    }
}