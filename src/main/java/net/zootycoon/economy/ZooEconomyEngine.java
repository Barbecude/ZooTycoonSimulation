package net.zootycoon.economy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.FoodProperties;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.block.ZooBlocks;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

/**
 * ============================================================================
 * ZOO TYCOON SIMULATION - MESIN UTAMA PERHITUNGAN EKONOMI (ZooEconomyEngine)
 * ============================================================================
 * File ini adalah satu-satunya pusat kendali seluruh perhitungan ekonomi kebun binatang.
 * Anda dapat dengan mudah mengubah nilai numerik, persentase, dan formula di bawah ini.
 *
 * Bersifat mandiri (self-contained & fail-safe):
 * Seluruh fungsi dilengkapi pengaman fallback sehingga jika ada formula yang salah
 * diubah atau menghasilkan error, logika permainan tidak akan crash atau merusak sistem yang ada.
 */
public final class ZooEconomyEngine {

    private ZooEconomyEngine() {}

    // =========================================================================
    // 1. PENGATURAN UMUM & SALDO AWAL (GENERAL CONFIG)
    // =========================================================================

    /** Saldo emerald awal untuk pemain baru */
    public static int STARTER_EMERALDS = 320;

    /** Biaya rekrut staf (Emerald sekali bayar saat spawn) */
    public static int BUILDER_HIRING_COST = 150;
    public static int ZOOKEEPER_HIRING_COST = 250;
    public static int SECURITY_HIRING_COST = 300;
    public static int CASHIER_HIRING_COST = 200;

    /** Gaji harian staf yang dipotong setiap fajar (06:00 in-game) */
    public static int ZOOKEEPER_DAILY_WAGE = 100;
    public static int SECURITY_DAILY_WAGE = 60;
    public static int CASHIER_DAILY_WAGE = 40;
    public static int BUILDER_DAILY_WAGE = 0; // Kuli dibayar per proyek bangunan (bukan harian)

    // =========================================================================
    // 2. PENGATURAN HARGA TIKET & DINAMIKA PENGUNJUNG (TICKETS & VISITORS)
    // =========================================================================

    /** 5 Tier Pilihan Harga Tiket Masuk di Kasir (Emerald) */
    public static final int[] TICKET_PRICE_TIERS = {0, 20, 50, 80, 120};

    /** Batas harga tiket masuk kebun binatang (Emerald) */
    public static int DEFAULT_TICKET_PRICE = 20;
    public static int MIN_TICKET_PRICE = 0; // 0 = Tiket Gratis (Free Admission)
    public static int MAX_TICKET_PRICE = 200;

    /** Kapasitas dasar pengunjung */
    public static int BASE_VISITOR_CAPACITY = 4;
    public static int CASHIER_CAPACITY_BONUS = 3;

    /** Bonus kapasitas saat tiket GRATIS (0 Emerald) - lonjakan massa / overcrowding */
    public static int FREE_TICKET_EXTRA_CAPACITY = 10;
    public static double FREE_TICKET_STAR_MULTIPLIER = 4.0D; // Rating tinggi + gratis = super membludak

    /** Bonus kapasitas saat tiket dibayar dengan harga wajar */
    public static double NORMAL_TICKET_STAR_MULTIPLIER = 2.0D;

    /** Penalti kapasitas jika tiket kemahalan dibanding rating */
    public static double OVERPRICED_CAPACITY_PENALTY_PER_EMERALD = 0.3D;
    public static int MIN_OVERPRICED_CAPACITY = 2;

    /** Rasio spawn pengunjung anak-anak */
    public static float DEFAULT_CHILD_SPAWN_CHANCE = 0.30F;
    public static float FREE_TICKET_CHILD_SPAWN_CHANCE = 0.45F;

    // =========================================================================
    // 3. TOLERANSI HARGA, KUALITAS ZOO & PENURUNAN RATING
    // =========================================================================

    /** Ambang batas bintang di mana zoo dianggap sangat prestisius dan pengunjung toleran tiket mahal */
    public static double HIGH_RATING_PRICE_TOLERANCE_THRESHOLD = 4.8D;

    /** Peluang dasar calon pengunjung menolak tiket kemahalan */
    public static float BASE_TICKET_REJECT_CHANCE = 0.40F;
    public static float TICKET_REJECT_CHANCE_PER_EXTRA_EMERALD = 0.02F;
    public static float MAX_TICKET_REJECT_CHANCE = 0.85F;

    /**
     * Skor kualitas kebun binatang minimum (0.0 s/d 1.0).
     * Jika kualitas di bawah nilai ini DAN tiket kemahalan, penolakan pengunjung
     * akan menghukum rating kebun binatang secara langsung!
     */
    public static double MIN_ACCEPTABLE_ZOO_QUALITY = 0.55D;

    /** Jumlah poin rating yang berkurang saat pengunjung kecewa karena tiket mahal di zoo berkualitas buruk */
    public static int OVERPRICED_TICKET_RATING_PENALTY = 2;

    // =========================================================================
    // 4. HARGA MAKANAN & MINUMAN (FOOD & DRINK PRICING TIERS)
    // =========================================================================

    // Makanan (Burger - More Delight, Farmer's Delight, Vanilla)
    public static int BURGER_TIER_1_PRICE = 30; // Simple burger (2 bahan)
    public static int BURGER_TIER_2_PRICE = 50; // Cheeseburger, egg burger (3-4 bahan)
    public static int BURGER_TIER_3_PRICE = 80; // Loaded hamburger More Delight (7 bahan)
    public static int BURGER_FARMERS_DELIGHT_PRICE = 60; // Hamburger Farmer's Delight (5 bahan)

    // Minuman (Drink & Ice Cream - Croptopia, Farm and Charm, Farmer's Delight)
    public static int DRINK_TIER_1_PRICE = 20; // Jus buah Croptopia, teh/kopi dasar
    public static int DRINK_TIER_2_PRICE = 40; // Seduhan herbal Farm and Charm, Cider & Cocoa FD, Smoothies
    public static int DRINK_TIER_3_PRICE = 60; // Minuman fermentasi Croptopia (beer, wine, mead)

    // =========================================================================
    // 5. KERAMAIAN SATWA (OVERCROWDING THRESHOLDS)
    // =========================================================================

    public static double OVERCROWDING_RADIUS = 10.0D;
    public static int OVERCROWDING_VISITOR_THRESHOLD = 4; // Lebih dari 4 pengunjung = satwa risih
    public static int OVERCROWDING_RATING_PENALTY_TICKS = 1200; // Tiap 60 detik jika terus-menerus ramai
    public static int OVERCROWDING_RATING_PENALTY = 1;

    // =========================================================================
    // HASIL KEPUTUSAN PEMBELIAN TIKET (ENUM)
    // =========================================================================

    public enum TicketDecision {
        PURCHASE,
        REJECT_EXPENSIVE,
        REJECT_ANGRY_PENALTY_RATING
    }

    public record TicketPrediction(String crowdLevel, int crowdColor, String advice) {}

    public static TicketPrediction getTicketPrediction(int ticketPrice, double starRating) {
        try {
            int expected = getExpectedTicketPrice(starRating);

            if (ticketPrice <= 0) {
                if (starRating < 1.0D) {
                    return new TicketPrediction(
                            "Pengunjung: Membludak (+10 Kapasitas)",
                            0xFF388BFD,
                            "Tiket gratis menarik pengunjung pemula tanpa biaya masuk."
                    );
                } else if (starRating >= 4.0D) {
                    return new TicketPrediction(
                            "Pengunjung: Super Membludak (+25 Kapasitas)",
                            0xFF58A6FF,
                            "Zoo bintang tinggi + gratis memicu serbuan pengunjung sangat masif!"
                    );
                } else {
                    return new TicketPrediction(
                            "Pengunjung: Membludak (Free Admission)",
                            0xFF388BFD,
                            "Pengunjung berdatangan sangat ramai tanpa membayar tiket masuk."
                    );
                }
            }

            // Normal / Fair price
            if (ticketPrice <= expected) {
                if (starRating >= 4.0D && ticketPrice <= 20) {
                    return new TicketPrediction(
                            "Pengunjung: Sangat Padat (Terlalu Murah!)",
                            0xFF55FF55,
                            "Tiket terlalu murah untuk zoo bintang tinggi. Naikkan harga tiket!"
                    );
                }
                return new TicketPrediction(
                        "Pengunjung: Ramai & Stabil",
                        0xFF55FF55,
                        "Harga tiket ideal dan sesuai dengan reputasi kebun binatang saat ini."
                );
            }

            // Price higher than expected
            int diff = ticketPrice - expected;
            if (starRating >= HIGH_RATING_PRICE_TOLERANCE_THRESHOLD) {
                // World class 5-star zoo!
                if (ticketPrice <= 80) {
                    return new TicketPrediction(
                            "Pengunjung: Ramai & Bergengsi",
                            0xFF55FF55,
                            "Pengunjung dengan senang hati membayar harga premium untuk zoo bintang 5."
                    );
                } else {
                    return new TicketPrediction(
                            "Pengunjung: Eksklusif & Stabil (VIP)",
                            0xFFFFD700,
                            "Harga VIP eksklusif! Pengunjung tetap datang berkat reputasi legendaris."
                    );
                }
            }

            if (diff <= 20) {
                return new TicketPrediction(
                        "Pengunjung: Sedang (Agak Mahal)",
                        0xFFECC94B,
                        "Sebagian kecil pengunjung mungkin ragu. Pastikan fasilitas zoo terawat."
                );
            } else if (diff <= 50) {
                return new TicketPrediction(
                        "Pengunjung: Sepi (Kemahalan!)",
                        0xFFE53E3E,
                        "Banyak pengunjung menolak beli tiket! Tingkatkan rating bintang dahulu."
                );
            } else {
                return new TicketPrediction(
                        "Pengunjung: Menolak Datang (Sangat Mahal!)",
                        0xFFE53E3E,
                        "Tiket jauh melampaui rating! Pengunjung marah jika kondisi zoo buruk."
                );
            }
        } catch (Throwable t) {
            return new TicketPrediction("Pengunjung: Normal", 0xFFA0AEC0, "Kondisi standar.");
        }
    }

    // =========================================================================
    // LOGIKA PERHITUNGAN EKONOMI (DENGAN FAIL-SAFE FALLBACK)
    // =========================================================================

    public static int getStarterEmeralds() {
        return Math.max(0, STARTER_EMERALDS);
    }

    /**
     * Menghitung ekspektasi harga wajar tiket berdasarkan rating bintang (0.0 - 5.0).
     * Bintang 0.0 - 1.0 -> 10 Emerald
     * Bintang 2.0 -> 20 Emerald
     * Bintang 3.0 -> 30 Emerald
     * Bintang 4.0 -> 40 Emerald
     * Bintang 5.0 -> 50 Emerald
     */
    public static int getExpectedTicketPrice(double starRating) {
        try {
            int rounded = (int) Math.round(starRating);
            return Math.clamp(rounded * 10, 10, 50);
        } catch (Throwable t) {
            return 20;
        }
    }

    /**
     * Menghitung kapasitas maksimal pengunjung di area kebun binatang.
     */
    public static int calculateMaxVisitorCapacity(int cashierCount, double starRating, int ticketPrice) {
        try {
            int base = BASE_VISITOR_CAPACITY + (Math.max(0, cashierCount) * CASHIER_CAPACITY_BONUS);
            int expected = getExpectedTicketPrice(starRating);

            if (ticketPrice <= 0) {
                // Free admission: bonus kapasitas masif
                return base + FREE_TICKET_EXTRA_CAPACITY + (int) Math.round(starRating * FREE_TICKET_STAR_MULTIPLIER);
            } else if (ticketPrice <= expected) {
                // Harga wajar atau murah
                return base + (int) Math.round(starRating * NORMAL_TICKET_STAR_MULTIPLIER);
            } else {
                // Kemahalan
                // Zoo bintang tinggi (>= 4.5) tetap mempertahankan kapasitas stabil meski harga tinggi
                if (starRating >= HIGH_RATING_PRICE_TOLERANCE_THRESHOLD) {
                    return base + (int) Math.round(starRating * NORMAL_TICKET_STAR_MULTIPLIER);
                }
                int diff = ticketPrice - expected;
                return Math.max(MIN_OVERPRICED_CAPACITY, base + (int) Math.round(starRating * 1.5D) - (int) Math.round(diff * OVERPRICED_CAPACITY_PENALTY_PER_EMERALD));
            }
        } catch (Throwable t) {
            return 10;
        }
    }

    /**
     * Memperlambat laju spawn pengunjung jika tiket dipatok terlalu mahal.
     */
    public static boolean shouldThrottleSpawn(int ticketPrice, int expectedPrice, double starRating, long currentTick) {
        try {
            if (starRating >= HIGH_RATING_PRICE_TOLERANCE_THRESHOLD && ticketPrice <= 120) {
                return false;
            }
            if (ticketPrice > expectedPrice) {
                return (currentTick / 100) % 2 != 0;
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean shouldThrottleSpawn(int ticketPrice, int expectedPrice, long currentTick) {
        return shouldThrottleSpawn(ticketPrice, expectedPrice, 0.0D, currentTick);
    }

    /**
     * Menentukan peluang spawn anak-anak yang menemani pengunjung dewasa.
     */
    public static float getChildSpawnChance(int ticketPrice) {
        try {
            return ticketPrice <= 0 ? FREE_TICKET_CHILD_SPAWN_CHANCE : DEFAULT_CHILD_SPAWN_CHANCE;
        } catch (Throwable t) {
            return 0.30F;
        }
    }

    /**
     * Mengevaluasi skor kualitas kebun binatang pemain (0.0 = Buruk sekali, 1.0 = Sempurna).
     * Faktor yang dinilai:
     * 1. Kesejahteraan Satwa: persentase kenyang, tidak haus, bersih, dan tidak stres.
     * 2. Kebersihan Lahan: penalti jika ada blok sampah (ZooBlocks.TRASH) berserakan di area klaim.
     */
    public static double calculateZooQuality(ServerLevel level, UUID ownerUuid) {
        if (level == null || ownerUuid == null) return 0.70D;
        try {
            double animalScore = 1.0D;
            int trackedAnimals = 0;
            int totalAnimalWelfare = 0;

            for (var entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity animal && ZooAnimalHungerManager.isZooAnimalCandidate(animal)) {
                    if (ZooAnimalHungerManager.isTagged(animal) && ZooAnimalHungerManager.isOwnedBy(animal, ownerUuid)) {
                        trackedAnimals++;
                        int hunger = ZooAnimalHungerManager.getHunger(animal);
                        int thirst = ZooAnimalHungerManager.getThirst(animal);
                        int clean = ZooAnimalHungerManager.getCleanliness(animal);
                        boolean overcrowded = isAnimalOvercrowded(animal);

                        // Rata-rata kesehatan satwa
                        int statAvg = (hunger + thirst + clean) / 3;
                        if (overcrowded) statAvg = Math.max(0, statAvg - 25);
                        totalAnimalWelfare += statAvg;
                    }
                }
            }

            if (trackedAnimals > 0) {
                animalScore = (totalAnimalWelfare / (double) (trackedAnimals * 100));
            } else {
                animalScore = 0.75D; // Belum ada satwa: nilai netral
            }

            // Hitung keberadaan sampah berserakan di klaim pemain
            ZooClaimManager claimManager = ZooClaimManager.get(level);
            Collection<ZooClaim> claims = claimManager.getPlayerClaims(ownerUuid);
            int trashCount = 0;

            for (ZooClaim claim : claims) {
                var box = claim.getBox();
                // Sampling area klaim untuk mendeteksi tumpukan sampah
                int minX = box.minX();
                int maxX = box.maxX();
                int minZ = box.minZ();
                int maxZ = box.maxZ();

                // Periksa setiap 3 blok untuk efisiensi
                for (int x = minX; x <= maxX; x += 3) {
                    for (int z = minZ; z <= maxZ; z += 3) {
                        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                        BlockPos checkPos = new BlockPos(x, y, z);
                        if (level.getBlockState(checkPos).is(ZooBlocks.TRASH) || level.getBlockState(checkPos.below()).is(ZooBlocks.TRASH)) {
                            trashCount++;
                        }
                    }
                }
            }

            // Setiap sampah mengurangi 0.05 kualitas hingga maksimal -0.40
            double trashPenalty = Math.min(0.40D, trashCount * 0.05D);
            double finalScore = Math.clamp(animalScore - trashPenalty, 0.0D, 1.0D);
            return finalScore;
        } catch (Throwable t) {
            ZooTycoonMod.LOGGER.warn("Gagal mengevaluasi kualitas zoo: {}", t.getMessage());
            return 0.70D;
        }
    }

    /**
     * Mengevaluasi keputusan calon pengunjung saat berada di depan loket tiket.
     * Mengatur apakah pengunjung membeli tiket, menolak karena mahal, atau menolak keras
     * dan menurunkan rating kebun binatang jika zoo berkualitas buruk!
     */
    public static TicketDecision evaluateTicketPurchase(int ticketPrice, double starRating, double zooQuality, RandomSource random) {
        try {
            int expected = getExpectedTicketPrice(starRating);

            // 1. Tiket gratis atau di bawah/sesuai ekspektasi rating -> Selalu beli!
            if (ticketPrice <= expected) {
                return TicketDecision.PURCHASE;
            }

            // 2. Jika kebun binatang sudah bintang 5 (prestisius), pengunjung jauh lebih memaklumi harga
            if (starRating >= HIGH_RATING_PRICE_TOLERANCE_THRESHOLD) {
                if (ticketPrice > expected + 40 && random.nextFloat() < 0.35F) {
                    return TicketDecision.REJECT_EXPENSIVE;
                }
                return TicketDecision.PURCHASE;
            }

            // 3. Tiket kemahalan dibanding rating saat ini
            if (ticketPrice > expected) {
                int extra = ticketPrice - expected;
                float rejectChance = Math.min(MAX_TICKET_REJECT_CHANCE, BASE_TICKET_REJECT_CHANCE + (extra * TICKET_REJECT_CHANCE_PER_EXTRA_EMERALD));

                if (random.nextFloat() < rejectChance) {
                    // Pengunjung menolak membeli tiket!
                    // CEK: Apakah kualitas kebun binatang buruk?
                    if (zooQuality < MIN_ACCEPTABLE_ZOO_QUALITY) {
                        // Kualitas buruk + tiket mahal = Pengunjung sangat marah & rating kebun binatang turun!
                        return TicketDecision.REJECT_ANGRY_PENALTY_RATING;
                    } else {
                        // Kualitas masih lumayan, hanya pulang karena tidak mampu / kemahalan
                        return TicketDecision.REJECT_EXPENSIVE;
                    }
                }
            }

            return TicketDecision.PURCHASE;
        } catch (Throwable t) {
            return TicketDecision.PURCHASE;
        }
    }

    /**
     * Mengeksekusi penalti rating saat tiket kemahalan dan kondisi zoo buruk.
     */
    public static void applyOverpricedTicketPenalty(ServerLevel sLevel, UUID ownerUuid) {
        if (sLevel == null || ownerUuid == null || sLevel.getServer() == null) return;
        try {
            ServerPlayer sp = sLevel.getServer().getPlayerList().getPlayer(ownerUuid);
            int newRating = ZooRatingManager.get(sLevel.getServer()).deductRating(ownerUuid, OVERPRICED_TICKET_RATING_PENALTY, sp);
            if (sp != null) {
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.rating_down", "zootycoon.toast.rating.overpriced_bad_zoo", 2, newRating);
            }
        } catch (Throwable t) {
            ZooTycoonMod.LOGGER.warn("Gagal menerapkan penalti rating tiket kemahalan: {}", t.getMessage());
        }
    }

    public static int getFoodDrinkPrice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 20;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            String path = id.getPath().toLowerCase(Locale.ROOT);
            if (path.contains("burger") || path.contains("sandwich") || path.contains("steak")) {
                return getFoodDrinkPrice("BURGER", stack);
            }
        }
        return getFoodDrinkPrice("DRINK", stack);
    }

    /**
     * Menghitung harga jual makanan atau minuman di kasir secara berjenjang.
     */
    public static int getFoodDrinkPrice(String role, ItemStack stack) {
        if (role == null) return 20;
        try {
            if ("BURGER".equalsIgnoreCase(role)) {
                if (stack != null && !stack.isEmpty()) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (id != null) {
                        String namespace = id.getNamespace().toLowerCase(Locale.ROOT);
                        String path = id.getPath().toLowerCase(Locale.ROOT);

                        // Tier 3: Burger Kompleks (60 - 80 Emerald)
                        if (path.contains("loaded_hamburger")) return BURGER_TIER_3_PRICE;
                        if ("farmersdelight".equals(namespace) && path.contains("hamburger")) return BURGER_FARMERS_DELIGHT_PRICE;
                        if (path.contains("steak_and_potatoes")) return 60;

                        // Tier 2: Burger Standar (40 - 50 Emerald)
                        if (path.contains("hamburger_with_cheese") || path.contains("cheeseburger")) return BURGER_TIER_2_PRICE;
                        if (path.contains("hamburger_with_egg") || path.contains("tofuburger") || path.contains("steak_sandwich")) return 40;
                        if ("croptopia".equals(namespace) && path.contains("hamburger")) return 40;

                        // Tier 1: Simple Burger (30 Emerald)
                        if (path.contains("simple_hamburger")) return BURGER_TIER_1_PRICE;
                    }
                    FoodProperties food = stack.get(DataComponents.FOOD);
                    if (food != null && food.nutrition() >= 10) return 40;
                }
                return BURGER_TIER_1_PRICE;
            }

            if ("DRINK".equalsIgnoreCase(role) || "ICE_CREAM".equalsIgnoreCase(role)) {
                if (stack != null && !stack.isEmpty()) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (id != null) {
                        String namespace = id.getNamespace().toLowerCase(Locale.ROOT);
                        String path = id.getPath().toLowerCase(Locale.ROOT);

                        // Tier 3: Minuman Fermentasi / Mewah (60 Emerald)
                        if (path.contains("beer") || path.contains("wine") || path.contains("mead")) return DRINK_TIER_3_PRICE;
                        if (path.contains("chocolate_milkshake")) return 50;

                        // Tier 2: Teh Herbal Farm and Charm, Seduhan FD & Smoothies (40 Emerald)
                        if ("farm_and_charm".equals(namespace) && path.contains("tea")) return DRINK_TIER_2_PRICE;
                        if ("farmersdelight".equals(namespace) && (path.contains("apple_cider") || path.contains("hot_cocoa"))) return DRINK_TIER_2_PRICE;
                        if (path.contains("smoothie") || path.contains("milkshake") || path.contains("latte")) return DRINK_TIER_2_PRICE;

                        // Tier 1: Jus Buah Croptopia & Kopi/Teh Dasar (20 Emerald)
                        if (path.contains("juice") || path.contains("tea") || path.contains("coffee") || path.contains("limeade") || path.contains("lemonade")) {
                            return DRINK_TIER_1_PRICE;
                        }
                    }
                }
                return DRINK_TIER_1_PRICE;
            }

            if ("TICKET".equalsIgnoreCase(role)) {
                return DEFAULT_TICKET_PRICE;
            }
            return 20;
        } catch (Throwable t) {
            return 20;
        }
    }

    /**
     * Menghitung upah harian yang harus dibayar kepada staf kebun binatang.
     */
    public static int calculateDailyWages(int zookeepers, int security, int cashiers) {
        try {
            int zk = Math.max(0, zookeepers) * ZOOKEEPER_DAILY_WAGE;
            int sec = Math.max(0, security) * SECURITY_DAILY_WAGE;
            int cash = Math.max(0, cashiers) * CASHIER_DAILY_WAGE;
            return zk + sec + cash;
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * Mengambil biaya rekrut staf sekali bayar saat mempekerjakan NPC.
     */
    public static int getStaffHiringCost(String type) {
        if (type == null) return 0;
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "builder", "kuli" -> BUILDER_HIRING_COST;
            case "zookeeper" -> ZOOKEEPER_HIRING_COST;
            case "security", "satpam" -> SECURITY_HIRING_COST;
            case "cashier", "kasir" -> CASHIER_HIRING_COST;
            default -> 0;
        };
    }

    /**
     * Menghitung upah tukang bangunan (kuli) berdasarkan jumlah blok yang dipasang.
     */
    public static int calculateBuilderWage(int totalBlocks) {
        try {
            if (totalBlocks <= 15) return 20;
            if (totalBlocks <= 30) return 40;
            if (totalBlocks <= 60) return 80;
            if (totalBlocks <= 120) return 180;
            if (totalBlocks <= 250) return 350;
            if (totalBlocks <= 500) return 750;
            return 750 + (int) Math.ceil((totalBlocks - 500) * 2.0D);
        } catch (Throwable t) {
            return 100;
        }
    }

    /**
     * Menghitung biaya pembelian blok baru untuk ekspansi klaim tanah (Emerald).
     * Skala 1 digit 0 ekstra: 2.5 emerald per blok baru (sebelumnya 0.25 emerald per blok).
     */
    public static int calculateClaimCost(int newBlocks) {
        if (newBlocks <= 0) return 0;
        return Math.max(10, (int) Math.ceil((newBlocks * 10.0) / 4.0));
    }

    /**
     * Memeriksa apakah satwa sedang dalam kondisi terlalu ramai (> 4 pengunjung dalam radius 10 blok).
     */
    public static boolean isAnimalOvercrowded(LivingEntity animal) {
        if (animal == null || animal.level() == null || animal.level().isClientSide()) return false;
        try {
            var nearbyVisitors = animal.level().getEntitiesOfClass(
                    net.zootycoon.entity.ZooVisitorEntity.class,
                    animal.getBoundingBox().inflate(OVERCROWDING_RADIUS),
                    net.minecraft.world.entity.Entity::isAlive
            );
            return nearbyVisitors.size() > OVERCROWDING_VISITOR_THRESHOLD;
        } catch (Throwable t) {
            return false;
        }
    }
}
