package com.example.simulation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ZooConfig {
        public static final ForgeConfigSpec COMMON_SPEC;

        // Economy
        public static final ForgeConfigSpec.IntValue STARTING_BALANCE;
        public static final ForgeConfigSpec.IntValue ANIMAL_INCOME_PER_CYCLE;
        public static final ForgeConfigSpec.IntValue STAFF_SALARY_PER_CYCLE;
        public static final ForgeConfigSpec.IntValue TRASH_PENALTY_PER_ITEM;
        public static final ForgeConfigSpec.IntValue TICKET_PRICE;

        // Costs
        public static final ForgeConfigSpec.IntValue HIRE_ZOOKEEPER_COST;
        public static final ForgeConfigSpec.IntValue HIRE_JANITOR_COST;
        public static final ForgeConfigSpec.IntValue HIRE_VET_COST;
        public static final ForgeConfigSpec.IntValue UPGRADE_LAND_COST;
        public static final ForgeConfigSpec.IntValue FOOD_REFILL_COST;

        // Animals
        public static final ForgeConfigSpec.IntValue VANILLA_ANIMAL_PRICE;
        public static final ForgeConfigSpec.IntValue MOD_ANIMAL_PRICE;
        public static final ForgeConfigSpec.IntValue ZAWA_ANIMAL_PRICE;

        // Gameplay
        public static final ForgeConfigSpec.IntValue INITIAL_SCAN_RADIUS;
        public static final ForgeConfigSpec.IntValue SCAN_INTERVAL_TICKS;
        public static final ForgeConfigSpec.IntValue VISITOR_MAX_STAY_TIME;
        public static final ForgeConfigSpec.IntValue VISITOR_SPAWN_COOLDOWN;

        static {
                ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

                builder.comment("IndoZoo Tycoon Configuration").push("economy");
                STARTING_BALANCE = builder
                                .comment("Starting balance for new Zoo Computer")
                                .defineInRange("startingBalance", 5000, 0, 1000000);
                ANIMAL_INCOME_PER_CYCLE = builder
                                .comment("Income per animal per cycle (20 seconds)")
                                .defineInRange("animalIncome", 500, 0, 10000);
                STAFF_SALARY_PER_CYCLE = builder
                                .comment("Salary per staff per cycle")
                                .defineInRange("staffSalary", 200, 0, 10000);
                TRASH_PENALTY_PER_ITEM = builder
                                .comment("Penalty per trash item")
                                .defineInRange("trashPenalty", 100, 0, 1000);
                TICKET_PRICE = builder
                                .comment("Price visitors pay to enter zoo")
                                .defineInRange("ticketPrice", 500, 0, 5000);
                builder.pop();

                builder.comment("Staff & Upgrade Costs").push("costs");
                HIRE_ZOOKEEPER_COST = builder.defineInRange("hireZookeeper", 2000, 100, 50000);
                HIRE_JANITOR_COST = builder.defineInRange("hireJanitor", 1500, 100, 50000);
                HIRE_VET_COST = builder.defineInRange("hireVet", 3000, 100, 50000);
                UPGRADE_LAND_COST = builder.defineInRange("upgradeLand", 5000, 100, 100000);
                FOOD_REFILL_COST = builder.defineInRange("foodRefill", 100, 10, 1000);
                builder.pop();

                builder.comment("Animal Pricing").push("animals");
                VANILLA_ANIMAL_PRICE = builder
                                .comment("Default price for minecraft vanilla animals")
                                .defineInRange("vanillaPrice", 3000, 100, 100000);
                MOD_ANIMAL_PRICE = builder
                                .comment("Default price for modded animals (Alex's Mobs, etc)")
                                .defineInRange("modPrice", 8000, 100, 100000);
                ZAWA_ANIMAL_PRICE = builder
                                .comment("Base price for ZAWA animals (before category multipliers)")
                                .defineInRange("zawaPrice", 12000, 1000, 200000);
                builder.pop();

                builder.comment("Gameplay Settings").push("gameplay");
                INITIAL_SCAN_RADIUS = builder
                                .comment("Initial scan radius for Zoo Computer")
                                .defineInRange("initialRadius", 20, 5, 100);
                SCAN_INTERVAL_TICKS = builder
                                .comment("How often to run economy calculations (in ticks)")
                                .defineInRange("scanInterval", 400, 20, 6000);
                VISITOR_MAX_STAY_TIME = builder
                                .comment("How long visitors stay before leaving (in ticks)")
                                .defineInRange("visitorStayTime", 6000, 1200, 24000);
                VISITOR_SPAWN_COOLDOWN = builder
                                .comment("Minimum ticks between visitor spawns")
                                .defineInRange("visitorCooldown", 400, 100, 2400);
                builder.pop();

                COMMON_SPEC = builder.build();
        }

        public static void register() {
                ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        }
}
