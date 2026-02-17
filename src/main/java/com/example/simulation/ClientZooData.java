package com.example.simulation;

public class ClientZooData {
    private static int balance = 100_000_000;
    private static int animalCount = 0;
    private static int staffCount = 0;
    private static int visitorCount = 0;

    public static int getBalance() {
        return balance;
    }

    public static void setBalance(int newBalance) {
        balance = newBalance;
    }

    public static int getAnimalCount() {
        return animalCount;
    }

    public static void setAnimalCount(int count) {
        animalCount = count;
    }

    public static int getStaffCount() {
        return staffCount;
    }

    public static void setStaffCount(int count) {
        staffCount = count;
    }

    public static int getVisitorCount() {
        return visitorCount;
    }

    public static void setVisitorCount(int count) {
        visitorCount = count;
    }

    private static net.minecraft.nbt.ListTag taggedAnimals = new net.minecraft.nbt.ListTag();

    public static net.minecraft.nbt.ListTag getTaggedAnimals() {
        return taggedAnimals;
    }

    public static void setTaggedAnimals(net.minecraft.nbt.ListTag list) {
        taggedAnimals = list;
    }
}
