package net.zootycoon.economy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.block.ZooBlocks;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.Collection;
import java.util.List;
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
    public static int BASE_VISITOR_CAPACITY = 45;
    public static int CASHIER_CAPACITY_BONUS = 15;

    /** Bonus kapasitas saat tiket GRATIS (0 Emerald) - lonjakan massa / overcrowding */
    public static int FREE_TICKET_EXTRA_CAPACITY = 45;
    public static double FREE_TICKET_STAR_MULTIPLIER = 8.0D; // Rating tinggi + gratis = super membludak

    /** Bonus kapasitas saat tiket dibayar dengan harga wajar */
    public static double NORMAL_TICKET_STAR_MULTIPLIER = 8.0D;

    /** Penalti kapasitas jika tiket kemahalan dibanding rating */
    public static double OVERPRICED_CAPACITY_PENALTY_PER_EMERALD = 0.3D;
    public static int MIN_OVERPRICED_CAPACITY = 10;

    /** Rasio spawn pengunjung anak-anak */
    public static float DEFAULT_CHILD_SPAWN_CHANCE = 0.30F;
    public static float FREE_TICKET_CHILD_SPAWN_CHANCE = 0.45F;

    /** Jeda waktu kedatangan pengunjung di loket tiket (ticks, 20 ticks = 1 detik) */
    public static int VISITOR_SPAWN_INTERVAL_TICKS = 160; // 8 detik jeda standar antar pengunjung
    public static int VISITOR_SPAWN_INTERVAL_FREE_TICKS = 180; // 9 detik jeda saat gratis agar loket tidak kebanjiran
    public static int MAX_TICKET_QUEUE_SIZE = 5; // Batas panjang antrean loket sebelum menunda kedatangan pengunjung baru

    // =========================================================================
    // 3. TOLERANSI HARGA, KUALITAS ZOO & PENURUNAN RATING
    // =========================================================================

    /** Ambang batas bintang di mana zoo dianggap sangat prestisius dan pengunjung toleran tiket mahal */
    public static double HIGH_RATING_PRICE_TOLERANCE_THRESHOLD = 4.8D;

    /** Peluang dasar calon pengunjung menolak tiket kemahalan */
    public static float BASE_TICKET_REJECT_CHANCE = 0.40F;
    public static float TICKET_REJECT_CHANCE_PER_EXTRA_EMERALD = 0.02F;
    public static float MAX_TICKET_REJECT_CHANCE = 1.0F; // Jika harga jelas kemahalan, 100% pengunjung menolak dan tidak spawn sama sekali!

    /**
     * Skor kualitas kebun binatang minimum (0.0 s/d 1.0).
     * Jika kualitas di bawah nilai ini DAN tiket kemahalan, penolakan pengunjung
     * akan menghukum rating kebun binatang secara langsung!
     */
    public static double MIN_ACCEPTABLE_ZOO_QUALITY = 0.55D;

    /** Jumlah poin rating yang berkurang saat pengunjung kecewa karena tiket mahal di zoo berkualitas buruk */
    public static int OVERPRICED_TICKET_RATING_PENALTY = 2;

    // =========================================================================
    // 4. HARGA MAKANAN, MINUMAN & ES KRIM (HARDCODED FOOD & DRINK PRICES)
    // =========================================================================

    public static final java.util.Map<String, Integer> HARDCODED_FOOD_PRICES = new java.util.LinkedHashMap<>();

    static {
        // --- Stand Burger: Burger, Sandwich, Wrap, French Fries & Nuggets ---
        HARDCODED_FOOD_PRICES.put("deluxe_cheeseburger", 80);
        HARDCODED_FOOD_PRICES.put("loaded_hamburger", 80);
        HARDCODED_FOOD_PRICES.put("steak_and_potatoes", 60);
        HARDCODED_FOOD_PRICES.put("hamburger", 60);
        HARDCODED_FOOD_PRICES.put("cheeseburger", 50);
        HARDCODED_FOOD_PRICES.put("hamburger_with_cheese", 50);
        HARDCODED_FOOD_PRICES.put("hamburger_with_egg", 50);
        HARDCODED_FOOD_PRICES.put("chicken_cheese_sandwich", 45);
        HARDCODED_FOOD_PRICES.put("croque_monsieur", 45);
        HARDCODED_FOOD_PRICES.put("steak_sandwich", 45);
        HARDCODED_FOOD_PRICES.put("chicken_wrap", 40);
        HARDCODED_FOOD_PRICES.put("cheese_wrap", 40);
        HARDCODED_FOOD_PRICES.put("cod_sandwich", 40);
        HARDCODED_FOOD_PRICES.put("salmon_sandwich", 40);
        HARDCODED_FOOD_PRICES.put("rabbit_sandwich", 40);
        HARDCODED_FOOD_PRICES.put("tofuburger", 40);
        HARDCODED_FOOD_PRICES.put("bacon_sandwich", 40);
        HARDCODED_FOOD_PRICES.put("fish_sandwich", 40);
        HARDCODED_FOOD_PRICES.put("simple_hamburger", 30);
        HARDCODED_FOOD_PRICES.put("french_fries", 30);
        HARDCODED_FOOD_PRICES.put("chicken_nuggets", 30);
        HARDCODED_FOOD_PRICES.put("chicken_nugget", 30);
        HARDCODED_FOOD_PRICES.put("fried_onions", 30);

        // --- Stand Es Krim: Es Krim & Varian Rasa ---
        HARDCODED_FOOD_PRICES.put("fruit_ice_cream", 45);
        HARDCODED_FOOD_PRICES.put("chocolate_ice_cream", 40);
        HARDCODED_FOOD_PRICES.put("sweet_berry_ice_cream", 40);
        HARDCODED_FOOD_PRICES.put("mango_ice_cream", 40);
        HARDCODED_FOOD_PRICES.put("pecan_ice_cream", 40);
        HARDCODED_FOOD_PRICES.put("strawberry_ice_cream", 40);
        HARDCODED_FOOD_PRICES.put("vanilla_ice_cream", 35);
        HARDCODED_FOOD_PRICES.put("ice_cream", 35);

        // --- Stand Minuman: Minuman Fermentasi, Susu, Teh, Kopi & Soda ---
        HARDCODED_FOOD_PRICES.put("beer", 60);
        HARDCODED_FOOD_PRICES.put("wine", 60);
        HARDCODED_FOOD_PRICES.put("mead", 60);
        HARDCODED_FOOD_PRICES.put("rum", 60);
        HARDCODED_FOOD_PRICES.put("chocolate_milkshake", 50);
        HARDCODED_FOOD_PRICES.put("matcha_latte", 40);
        HARDCODED_FOOD_PRICES.put("apple_cider", 40);
        HARDCODED_FOOD_PRICES.put("hot_cocoa", 40);
        HARDCODED_FOOD_PRICES.put("strawberry_smoothie", 40);
        HARDCODED_FOOD_PRICES.put("banana_smoothie", 40);
        HARDCODED_FOOD_PRICES.put("fruit_smoothie", 40);
        HARDCODED_FOOD_PRICES.put("creaming_soda", 30);
        HARDCODED_FOOD_PRICES.put("apple_sauce", 30);
        HARDCODED_FOOD_PRICES.put("cola_soda", 25);
        HARDCODED_FOOD_PRICES.put("orange_soda", 25);
        HARDCODED_FOOD_PRICES.put("lemon_soda", 25);
        HARDCODED_FOOD_PRICES.put("iced_tea", 25);
        HARDCODED_FOOD_PRICES.put("green_tea", 25);
        HARDCODED_FOOD_PRICES.put("yellow_tea", 25);
        HARDCODED_FOOD_PRICES.put("black_tea", 25);
        HARDCODED_FOOD_PRICES.put("coffee", 25);
        HARDCODED_FOOD_PRICES.put("lemonade", 20);
        HARDCODED_FOOD_PRICES.put("limeade", 20);
        HARDCODED_FOOD_PRICES.put("apple_juice", 20);
        HARDCODED_FOOD_PRICES.put("melon_juice", 20);
        HARDCODED_FOOD_PRICES.put("orange_juice", 20);
        HARDCODED_FOOD_PRICES.put("grape_juice", 20);
        HARDCODED_FOOD_PRICES.put("cranberry_juice", 20);
        HARDCODED_FOOD_PRICES.put("saguaro_juice", 20);
        HARDCODED_FOOD_PRICES.put("tomato_juice", 20);
        HARDCODED_FOOD_PRICES.put("pineapple_juice", 20);
    }

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

    public static String getTicketPriceTierLabel(int ticketPrice) {
        if (ticketPrice <= 0) return "Gratis (Ramah Warga)";
        if (ticketPrice <= 20) return "Standar (Ramah Keluarga)";
        if (ticketPrice <= 50) return "Menengah (Wisata Populer)";
        if (ticketPrice <= 80) return "Eksklusif (Pengunjung Mampu)";
        return "VIP Sultan (Koleksi Mewah)";
    }

    public static int calculateZooValuation(ServerLevel level, UUID ownerUuid) {
        if (level == null || ownerUuid == null || level.getServer() == null) return 0;
        try {
            int treasury = ZooEconomyManager.get(level.getServer()).getBalance(ownerUuid);
            var claimManager = net.zootycoon.claim.ZooClaimManager.get(level);
            List<net.zootycoon.claim.ZooClaim> claims = claimManager.getPlayerClaims(ownerUuid);
            int claimValue = claims.size() * 100;

            int animalCount = 0;
            int staffCount = 0;
            for (ServerLevel sl : level.getServer().getAllLevels()) {
                for (Entity e : sl.getAllEntities()) {
                    if (e instanceof LivingEntity living && living.isAlive()) {
                        if (net.zootycoon.animal.ZooAnimalHungerManager.isOwnedBy(living, ownerUuid)
                                || (claimManager.isClaimedBy(living.blockPosition(), ownerUuid) && net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(living))) {
                            animalCount++;
                        }
                        if (e.getTags().contains("zootycoon_staff")) {
                            staffCount++;
                        }
                    }
                }
            }

            int animalValue = animalCount * 250;
            int staffValue = staffCount * 200;

            return Math.max(0, treasury + claimValue + animalValue + staffValue);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static String getZooValuationTier(int valuation) {
        if (valuation < 1000) return "Perintis Zoo (Rintisan)";
        if (valuation < 5000) return "Kebun Binatang Berkembang (Komersial)";
        if (valuation < 15000) return "Taman Konservasi Mapan (Mapan)";
        if (valuation < 50000) return "Destinasi Wisata Terkenal (Ternama)";
        return "Mega Safari Tycoon (Kaliber Dunia)";
    }

    /**
     * Menghasilkan teks dialog natural dan santai dari kasir tiket ('Bos...')
     * yang bereaksi langsung terhadap harga tiket yang dipilih serta rating kebun binatang.
     */
    public static Component getTicketCashierSpeech(int ticketPrice, double starRating, boolean isZooOpen) {
        if (!isZooOpen) {
            return Component.literal("Kebun binatang lagi TUTUP Bos! Loket belum melayani pengunjung. Klik tombol di bawah kalau mau buka kebun binatang.");
        }

        try {
            int expected = getExpectedTicketPrice(starRating);

            // 1. Tiket Gratis (0)
            if (ticketPrice <= 0) {
                if (starRating >= 4.0D) {
                    return Component.literal(String.format(Locale.ROOT, "Tiket gratis Bos? Zoo kita udah bintang %.1f! Pengunjung bakal super membludak serbu kebun binatang, siap-siap kewalahan!", starRating));
                }
                return Component.literal("Tiket gratis Bos? Ramah warga banget! Pengunjung bakal membludak serbu kebun binatang kita!");
            }

            // 2. Bintang 5 / World Class Zoo
            if (starRating >= HIGH_RATING_PRICE_TOLERANCE_THRESHOLD) {
                if (ticketPrice >= 80) {
                    return Component.literal(String.format(Locale.ROOT, "Mantap Bos! Zoo kita udah bintang %.1f legendaris, pengunjung dengan senang hati bayar tiket VIP segini demi satwa kita!", starRating));
                }
                return Component.literal(String.format(Locale.ROOT, "Tiket %d Emerald murah banget untuk zoo bintang %.1f kita! Pengunjung bakal ramai dan puas banget.", ticketPrice, starRating));
            }

            // 3. Normal / Fair Price
            if (ticketPrice <= expected) {
                if (starRating >= 3.5D && ticketPrice <= 20) {
                    return Component.literal(String.format(Locale.ROOT, "Kemurahan Bos! Rating kita udah %.1f bintang, sayang banget tiket cuma %d Emerald. Harusnya bisa dinaikkan!", starRating, ticketPrice));
                }
                if (ticketPrice <= 20) {
                    return Component.literal(String.format(Locale.ROOT, "Tiket 20 Emerald harga standar ramah keluarga Bos! Sesuai sama reputasi bintang %.1f kita.", starRating));
                }
                return Component.literal(String.format(Locale.ROOT, "Harga pas Bos! Sesuai sama reputasi bintang %.1f kita, pengunjung bakal ramai dan tertib.", starRating));
            }

            // 4. Overpriced comparison
            int diff = ticketPrice - expected;
            if (diff <= 30) {
                return Component.literal(String.format(Locale.ROOT, "Tiket %d Emerald kategori menengah ke atas Bos. Untuk rating bintang %.1f, pastikan fasilitas zoo kita terawat ya!", ticketPrice, starRating));
            } else {
                return Component.literal(String.format(Locale.ROOT, "Tiket %d Emerald kemahalan Bos! Rating masih %.1f bintang, pengunjung bisa kecewa kalau kondisi zoo kurang bagus!", ticketPrice, starRating));
            }
        } catch (Throwable t) {
            return Component.literal("Loket tiket siap melayani pengunjung Bos!");
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

            int capacity;
            if (ticketPrice <= 0) {
                // Free admission: bonus kapasitas masif
                capacity = base + FREE_TICKET_EXTRA_CAPACITY + (int) Math.round(starRating * FREE_TICKET_STAR_MULTIPLIER);
            } else if (ticketPrice <= expected) {
                // Harga wajar atau murah
                capacity = base + (int) Math.round(starRating * NORMAL_TICKET_STAR_MULTIPLIER);
            } else {
                // Kemahalan
                // Zoo bintang tinggi (>= 4.5) tetap mempertahankan kapasitas stabil meski harga tinggi
                if (starRating >= HIGH_RATING_PRICE_TOLERANCE_THRESHOLD) {
                    capacity = base + (int) Math.round(starRating * NORMAL_TICKET_STAR_MULTIPLIER);
                } else {
                    int diff = ticketPrice - expected;
                    capacity = Math.max(MIN_OVERPRICED_CAPACITY, base + (int) Math.round(starRating * 1.5D) - (int) Math.round(diff * OVERPRICED_CAPACITY_PENALTY_PER_EMERALD));
                }
            }
            return Math.min(150, Math.max(MIN_OVERPRICED_CAPACITY, capacity));
        } catch (Throwable t) {
            return 25;
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
     * Menghitung jeda waktu (ticks) sebelum rombongan pengunjung berikutnya tiba di loket tiket.
     * Memberikan jeda yang wajar agar tidak terjadi antrean menumpuk secara instan, terutama saat tiket gratis.
     */
    public static int getVisitorSpawnIntervalTicks(int ticketPrice, net.minecraft.util.RandomSource random) {
        try {
            int base = ticketPrice <= 0 ? VISITOR_SPAWN_INTERVAL_FREE_TICKS : VISITOR_SPAWN_INTERVAL_TICKS;
            int jitter = random != null ? random.nextInt(41) - 20 : 0;
            return Math.max(120, base + jitter);
        } catch (Throwable t) {
            return 160;
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
     * Menghitung harga jual makanan atau minuman di kasir secara hardcode pasti.
     */
    public static int getFoodDrinkPrice(String role, ItemStack stack) {
        if (role == null) return 20;
        try {
            if (stack != null && !stack.isEmpty()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id != null) {
                    String path = id.getPath().toLowerCase(Locale.ROOT);

                    // 1. Cek langsung tabel harga hardcode pasti
                    Integer hardcoded = HARDCODED_FOOD_PRICES.get(path);
                    if (hardcoded != null) {
                        return hardcoded;
                    }

                    // 2. Fallback pola nama untuk varian mod luar
                    if ("BURGER".equalsIgnoreCase(role)) {
                        if (path.contains("deluxe_cheeseburger") || path.contains("loaded_hamburger")) return 80;
                        if (path.contains("steak_and_potatoes") || path.contains("hamburger")) return 60;
                        if (path.contains("cheeseburger")) return 50;
                        if (path.contains("steak_sandwich") || path.contains("croque") || path.contains("chicken_cheese_sandwich")) return 45;
                        if (path.contains("sandwich") || path.contains("wrap") || path.contains("tofuburger")) return 40;
                        if (path.contains("french_fries") || path.contains("nugget") || path.contains("fried_onions") || path.contains("simple_hamburger")) return 30;
                        return BURGER_TIER_1_PRICE;
                    }

                    if ("ICE_CREAM".equalsIgnoreCase(role) || path.contains("ice_cream")) {
                        if (path.contains("fruit") || path.contains("chocolate") || path.contains("berry")) return 40;
                        return 35;
                    }

                    if ("DRINK".equalsIgnoreCase(role)) {
                        if (path.contains("beer") || path.contains("wine") || path.contains("mead") || path.contains("rum")) return 60;
                        if (path.contains("milkshake") || path.contains("smoothie") || path.contains("latte")
                                || path.contains("cider") || path.contains("cocoa")) return 40;
                        if (path.contains("soda") || path.contains("tea") || path.contains("coffee")) return 25;
                        if (path.contains("juice") || path.contains("lemonade") || path.contains("limeade") || path.contains("apple_sauce")) return 20;
                        return DRINK_TIER_1_PRICE;
                    }
                }
            }

            if ("BURGER".equalsIgnoreCase(role)) return BURGER_TIER_1_PRICE;
            if ("ICE_CREAM".equalsIgnoreCase(role)) return 35;
            if ("DRINK".equalsIgnoreCase(role)) return DRINK_TIER_1_PRICE;
            if ("TICKET".equalsIgnoreCase(role)) return DEFAULT_TICKET_PRICE;
            return 20;
        } catch (Throwable t) {
            return 20;
        }
    }

    /**
     * Menghitung taksiran harga satuan bahan mentah untuk pembelian kilat di kasir.
     */
    public static int getRawIngredientUnitPrice(Item item) {
        if (item == null || item == Items.AIR) return 3;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return 3;
        String path = id.getPath().toLowerCase(Locale.ROOT);

        // Daging & Seafood (nilai tinggi)
        if (path.contains("beef") || path.contains("steak") || path.contains("mutton")
                || path.contains("pork") || path.contains("bacon") || path.contains("chicken")
                || path.contains("salmon") || path.contains("cod") || path.contains("fish")
                || path.contains("rabbit") || path.contains("venison")) {
            return 7;
        }

        // Produk susu & Keju
        if (path.contains("milk") || path.contains("cheese") || path.contains("butter") || path.contains("cream")) {
            return 5;
        }

        // Telur
        if (path.contains("egg")) {
            return 4;
        }

        // Sayuran & Buah-buahan segar
        if (path.contains("tomato") || path.contains("onion") || path.contains("cabbage")
                || path.contains("lettuce") || path.contains("bellpepper") || path.contains("pepper")
                || path.contains("apple") || path.contains("orange") || path.contains("lemon")
                || path.contains("lime") || path.contains("grape") || path.contains("berry")
                || path.contains("berries") || path.contains("cherry") || path.contains("peach")
                || path.contains("banana") || path.contains("mango") || path.contains("melon")
                || path.contains("carrot") || path.contains("potato") || path.contains("beetroot")) {
            return 4;
        }

        // Gandum, Beras, Gula, Madu, Teh, Kopi, Kakao
        if (path.contains("wheat") || path.contains("rice") || path.contains("corn")
                || path.contains("soy") || path.contains("sugar") || path.contains("honey")
                || path.contains("tea") || path.contains("coffee") || path.contains("cocoa")) {
            return 3;
        }

        // Es, Salju, Kertas, Mangkuk
        if (path.contains("paper") || path.contains("bowl") || path.contains("ice") || path.contains("snow")) {
            return 2;
        }

        return 3;
    }

    /**
     * Menghitung biaya bahan mentah realistis untuk 1 varian menu pesanan kasir,
     * dengan pembatas margin berkisar antara 56% - 68%.
     */
    public static int calculateMenuItemRawCost(String role, ItemStack menuItem, List<WarehousePayloads.RawIngredientItemDto> rawIngredients) {
        if (menuItem == null || menuItem.isEmpty()) return 0;
        int unitPrice = getFoodDrinkPrice(role, menuItem);
        int totalUnits = Math.max(1, menuItem.getCount());
        int totalSellingPrice = unitPrice * totalUnits;

        int rawSum = 0;
        if (rawIngredients != null && !rawIngredients.isEmpty()) {
            for (WarehousePayloads.RawIngredientItemDto raw : rawIngredients) {
                if (raw.stack() != null && !raw.stack().isEmpty()) {
                    rawSum += getRawIngredientUnitPrice(raw.stack().getItem()) * raw.stack().getCount();
                }
            }
        }

        int minCost = (int) Math.round(totalSellingPrice * 0.32D);
        int maxCost = (int) Math.round(totalSellingPrice * 0.44D);
        int cost = rawSum > 0 ? Math.clamp(rawSum, Math.max(1, minCost), Math.max(1, maxCost)) : (int) Math.round(totalSellingPrice * 0.38D);
        return Math.max(1, cost);
    }

    /**
     * Menghitung total biaya bahan mentah untuk seluruh item dalam request kasir.
     */
    public static int calculateRequestRawCost(String role, WarehousePayloads.StaffRequestDto req) {
        if (req == null || !"CASHIER".equalsIgnoreCase(req.staffType())) return 0;
        int totalCost = 0;
        if (req.menuItems() != null && !req.menuItems().isEmpty()) {
            for (WarehousePayloads.MenuItemRequestDto menuDto : req.menuItems()) {
                totalCost += calculateMenuItemRawCost(role, menuDto.targetItem(), menuDto.rawIngredients());
            }
        } else if (req.targetItems() != null) {
            for (ItemStack st : req.targetItems()) {
                totalCost += calculateMenuItemRawCost(role, st, List.of());
            }
        }
        return Math.max(1, totalCost);
    }

    /**
     * Menghitung total potensi penerimaan emerald jika seluruh stok menu kasir terjual.
     */
    public static int calculateRequestTotalRevenue(String role, WarehousePayloads.StaffRequestDto req) {
        if (req == null || !"CASHIER".equalsIgnoreCase(req.staffType())) return 0;
        int totalRevenue = 0;
        if (req.targetItems() != null) {
            for (ItemStack st : req.targetItems()) {
                int unit = getFoodDrinkPrice(role, st);
                totalRevenue += unit * Math.max(1, st.getCount());
            }
        }
        return Math.max(1, totalRevenue);
    }

    public record ProfitMarginInfo(int totalRevenue, int rawCost, int netProfit, int marginPercent) {}

    public static ProfitMarginInfo getProfitMarginInfo(String role, WarehousePayloads.StaffRequestDto req) {
        int totalRevenue = calculateRequestTotalRevenue(role, req);
        int rawCost = calculateRequestRawCost(role, req);
        int netProfit = Math.max(0, totalRevenue - rawCost);
        int marginPercent = totalRevenue > 0 ? (int) Math.round(((double) netProfit / (double) totalRevenue) * 100.0) : 0;
        return new ProfitMarginInfo(totalRevenue, rawCost, netProfit, marginPercent);
    }

    /**
     * Menghitung biaya emerald untuk pembelian suplai pakan atau alat perawatan Zookeeper.
     */
    public static int calculateZookeeperSupplyCost(WarehousePayloads.StaffRequestDto req) {
        if (req == null) return 30;
        String sub = req.subtitle() != null ? req.subtitle().toLowerCase(Locale.ROOT) : "";
        if (sub.contains("pemanas") || sub.contains("kedinginan")) {
            int count = req.requiredCount() > 0 ? req.requiredCount() : 16;
            return Math.max(10, count * 2);
        } else if (sub.contains("pendingin") || sub.contains("kepanasan")) {
            int count = req.requiredCount() > 0 ? req.requiredCount() : 16;
            return Math.max(10, count * 2);
        } else if (sub.contains("alat mandi") || sub.contains("kotor")) {
            return 40;
        } else {
            int needed = req.requiredCount() > 0 ? req.requiredCount() : 32;
            int unitPrice = 2;
            if (req.targetItems() != null && !req.targetItems().isEmpty()) {
                ItemStack first = req.targetItems().get(0);
                if (first != null && !first.isEmpty()) {
                    unitPrice = Math.max(1, Math.min(4, getRawIngredientUnitPrice(first.getItem()) / 2));
                }
            }
            return Math.max(10, needed * unitPrice);
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
     * Menghitung harga jual anak satwa (offspring) berdasarkan kelangkaan dan tingkat kesulitan menangkap.
     */
    public static int getAnimalSellPrice(String entityTypeId) {
        if (entityTypeId == null) return 80;
        String id = entityTypeId.toLowerCase(Locale.ROOT);
        if (id.contains(":")) id = id.substring(id.indexOf(':') + 1);

        // 1. Expensive / Legendary / Deep Pelagic & Glacial Apex (600 - 800)
        if (id.contains("orca") || id.contains("cachalot") || id.contains("whale")
                || id.contains("white_shark") || id.contains("great_white")
                || id.contains("mammoth") || id.contains("froststalker")
                || id.contains("giant_squid") || id.contains("mantis_shrimp")) {
            return 750;
        }

        // 2. Rare / Apex / Dangerous / Exotic (300 - 450)
        if (id.contains("elephant") || id.contains("snow_leopard") || id.contains("komodo")
                || id.contains("gorilla") || id.contains("alligator") || id.contains("crocodile")
                || id.contains("anaconda") || id.contains("eagle") || id.contains("vulture")
                || id.contains("cassowary") || id.contains("polar_bear")
                || id.contains("guster") || id.contains("bunfungus") || id.contains("mungus")
                || id.contains("rocky_roller") || id.contains("flutter")) {
            return 350;
        }

        // 3. Medium / Wild Savanna & Forest (150 - 250)
        if (id.contains("lion") || id.contains("tiger") || id.contains("hippo")
                || id.contains("giraffe") || id.contains("kangaroo") || id.contains("raccoon")
                || id.contains("bear") || id.contains("grizzly") || id.contains("panda")
                || id.contains("anteater") || id.contains("capuchin") || id.contains("monkey")
                || id.contains("wolf") || id.contains("fox")) {
            return 200;
        }

        // 4. Common / Domestic / Petting (50 - 100)
        if (id.contains("sheep") || id.contains("mini_sheep") || id.contains("deer")
                || id.contains("zebra") || id.contains("ostrich") || id.contains("camel")
                || id.contains("penguin") || id.contains("capybara") || id.contains("tortoise")
                || id.contains("cow") || id.contains("pig") || id.contains("chicken") || id.contains("rabbit")) {
            return 80;
        }

        return 100;
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
