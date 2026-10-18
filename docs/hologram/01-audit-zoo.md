# Audit Fitur Animal Inspection Hologram, Kebutuhan Satwa, dan Sinkronisasi

## A. Verifikasi Temuan Awal (T1 - T9) dari CONTEXT.md

1. T1: Raycast ProjectileUtil.getEntityHitResult tanpa cek blok padat, tembus tembok; render Font.DisplayMode.SEE_THROUGH membuat teks bocor lewat dinding.
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:47-51: Raycast memanggil `ProjectileUtil.getEntityHitResult(...)` tanpa melakukan penelusuran raycast blok (`Level.clip`) untuk mendeteksi collision blok padat.
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:167: Teks digambar menggunakan mode `Font.DisplayMode.SEE_THROUGH`, sehingga teks di-render di atas buffer geometri dunia tanpa depth check dan tembus pandang melewati dinding.

2. T2: Target lengket: inspectExpireTime 8-10 detik membuat hologram tertinggal saat crosshair berpindah.
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:32: Pada interaksi klik, `inspectExpireTime = System.currentTimeMillis() + 10000L` (10 detik).
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:55: Saat raycast mendeteksi target, `inspectExpireTime = System.currentTimeMillis() + 8000L` (8 detik).
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:69-74: Selama `System.currentTimeMillis() < inspectExpireTime`, variabel `currentTargetedAnimal` dipertahankan dan hologram terus dirender meskipun crosshair sudah beralih dari hewan tersebut.

3. T3: Jangkauan 36-48 blok terlalu jauh; tidak cek layar UI terbuka atau F1.
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:44: Vektor jangkauan raycast diset `look.scale(36.0)` (36 blok).
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:85: Batas jarak render dipertahankan hingga kuadrat `48.0 * 48.0` (48 blok).
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:59-74: Method `render` hanya mengecek `mc.player == null || mc.level == null`. Tidak ada pengecekan kondisi `mc.screen == null` (apakah ada GUI terbuka) maupun `!mc.options.hideGui` (mode F1).

4. T4: Nilai palsu hunger 80, thirst 80, cleanliness 100 saat sync kosong.
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:112-115:
    `ZooNetworkPayloads.AnimalSyncData sync = ZooTycoonClient.getAnimalData(entity.getId());`
    `int hunger = sync != null ? sync.hunger() : 80;`
    `int thirst = sync != null ? sync.thirst() : 80;`
    `int cleanliness = sync != null ? sync.cleanliness() : 100;`
    Jika data sinkronisasi client bernilai null, nilai indikator langsung diisi nilai default buatan (80, 80, 100).

5. T5: Sync tidak periodik; hewan belum terdaftar tidak punya data yang akurat.
- Status: SEBAGIAN BENAR / SEBAGIAN SALAH
- Bukti:
  - Klaim "Sync tidak periodik" adalah SALAH: Di src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:314-318, terdapat sync periodik setiap 40 tick (2 detik) melalui `ZooNetworkPayloads.sendAnimalSync(player)` di dalam `tickServer`.
  - Klaim "Hewan belum terdaftar tidak punya data yang akurat" adalah BENAR: Di src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1904-1907, `sendAnimalSync` hanya mengirimkan entitas yang memenuhi syarat `isTagged(animal)` dan `isOwnedBy(animal, player.getUUID())`. Hewan yang belum terdaftar tidak pernah masuk ke payload sinkronisasi, sehingga di client selalu bernilai null dan menampilkan nilai default palsu. Tidak ada mekanisme sinkronisasi on-demand untuk target inspeksi.

6. T6: Musim: terpasang Fabric Seasons, bukan Serene Seasons, jadi getCurrentSeason selalu SPRING (juga memengaruhi laju thirst di tickServer).
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:22: Menggunakan refleksi terhadap mod id "sereneseasons".
  - src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:41-43, 53: Jika mod tidak ditemukan, selalu mengembalikan string "SPRING".
  - Folder mods profil (`C:\Users\Zevaldo\AppData\Roaming\ModrinthApp\profiles\Fresh & Smooth\mods`): Terpasang `fabric-seasons-2.4.2-BETA+1.21.jar`, sedangkan Serene Seasons tidak terpasang.
  - src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:205-210: Pengecekan percepatan thirst pada musim kemarau ("SUMMER") tidak pernah aktif karena kondisi `SereneSeasonsCompat.isLoaded()` selalu bernilai false.

7. T7: Kenyamanan suhu berdasar nama entity, bukan data spesies.
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:102-105: Evaluasi hanya mencocokkan substring nama ID entitas:
    `boolean isColdAnimal = entityTypeStr.contains("polar") || entityTypeStr.contains("snow") || entityTypeStr.contains("penguin") || entityTypeStr.contains("wolf") || entityTypeStr.contains("fox");`
    `boolean isDesertWarmAnimal = entityTypeStr.contains("camel") || entityTypeStr.contains("lion") || entityTypeStr.contains("elephant") || entityTypeStr.contains("giraffe") || entityTypeStr.contains("parrot");`
    Logika ini tidak membaca data spesies satwa, tag bioma, atau atribut entitas sesungguhnya.

8. T8: Teks memakai emoji dan kode warna §.
- Status: BENAR
- Bukti:
  - src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:121-138: Seluruh baris teks disusun dengan kode warna format langsung seperti "§e", "§c", "§f", "§6", "§d", "§a", "§2", "§b", "§7" serta karakter simbol/emoji seperti kaca pembesar, hati, bintang, tetesan air, centang, dan tanda bahaya.

9. T9: Belum ada sistem mood hewan dan sistem habitat/ekosistem.
- Status: BENAR
- Bukti:
  - Sistem mood yang ada di codebase saat ini hanya diimplementasikan untuk NPC pengunjung (src/main/java/net/zootycoon/entity/ZooVisitorEntity.java:31 dan src/main/java/net/zootycoon/client/render/ZooVisitorMoodLayer.java:22). Belum ada field, enum, atau kalkulasi mood untuk satwa.
  - Belum ada sistem analisis habitat atau ekosistem (analisis komposisi blok, tanaman, air, luas kandang) di dalam codebase satwa kebun binatang.

---

## B. Tabel Sumber Data per Indikator

| Indikator | Class.method | path:baris | Sisi server/client | Status ada/sebagian/tidak ada | Catatan |
|---|---|---|---|---|---|
| nama | LivingEntity.getDisplayName() / EntityType.getDescription() | src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:121 | Server & Client | Ada | Mengambil nama kustom entitas jika ada, fallback ke nama jenis entitas |
| health | LivingEntity.getHealth(), getMaxHealth() | src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:109-110, src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1925-1926 | Server & Client | Ada | Nilai vanilla LivingEntity, juga dikirimkan melalui paket AnimalSyncData |
| hunger | ZooAnimalHungerManager.getHunger() / ZooTycoonClient.getAnimalData().hunger() | src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:537, src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:112-113 | Server (data asli) & Client (sinkronisasi) | Sebagian | Otoritatif di server (HUNGER_MAP + tag zoo_hunger_). Di client bernilai null jika hewan belum terdaftar dan jatuh ke fallback 80% |
| thirst | ZooAnimalHungerManager.getThirst() / ZooTycoonClient.getAnimalData().thirst() | src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:563, src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:112-114 | Server (data asli) & Client (sinkronisasi) | Sebagian | Otoritatif di server (THIRST_MAP + tag zoo_thirst_). Hewan belum terdaftar tidak disinkronkan dan jatuh ke fallback 80% |
| cleanliness | ZooAnimalHungerManager.getCleanliness() / ZooTycoonClient.getAnimalData().cleanliness() | src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:589, src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:112-115 | Server (data asli) & Client (sinkronisasi) | Sebagian | Otoritatif di server (CLEANLINESS_MAP + tag zoo_cleanliness_). Hewan belum terdaftar jatuh ke fallback 100% |
| makanan favorit | AnimalDietRegistry.getDiet(), getAcceptedFoods() | src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:138, 163 | Server & Client | Ada | Terdaftar di registry untuk Vanilla, Alex's Mobs, dan Naturalist, namun saat ini belum ditampilkan pada hologram |
| mood | Tidak ada | - | - | Tidak ada | Belum diimplementasikan untuk entitas hewan di kelas mana pun |
| habitat/bioma | Level.getBiome().value() | src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:77 | Server & Client | Sebagian | Hanya membaca base temperature bioma vanilla; belum ada evaluasi kecocokan habitat dan komposisi lingkungan |
| blok sekitar | Tidak ada | - | - | Tidak ada | Belum ada pemindaian blok lingkungan sekitar untuk habitat selain deteksi blok air terdekat saat haus (ZooAnimalHungerManager.java:381) |
| suhu | SereneSeasonsCompat.evaluateEnvironment().temperature() | src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:74-99, src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:117 | Client & Server | Sebagian | Dihitung dari base temperature bioma dimodifikasi offset musim statis ("SPRING"); kategori suhu dihasilkan dari nilai ini |
| musim | SereneSeasonsCompat.getCurrentSeason(), getSeasonDisplayName() | src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:39, 56 | Server & Client | Sebagian | Selalu mengembalikan "SPRING" karena mengacu ke mod Serene Seasons yang tidak terpasang (modpack memakai Fabric Seasons) |

---

## C. Mekanisme Kebutuhan Satwa (Hunger, Thirst, Cleanliness)

### 1. Nilai Awal, Laju Turun, Cara Naik Kembali, dan Kondisi yang Mempercepat

- Hunger (Kelaparan):
  - Nilai awal: 100 (MAX_HUNGER, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:36).
  - Laju turun: Interval dasar adalah 40 tick (2 detik kehilangan 1%, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:39, 279-284). Pemeriksaan dilakukan setiap 20 tick (src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:201).
  - Kondisi yang mempercepat: Jika kebersihan satwa kotor (cleanliness <= 25), laju turun menjadi 2x lebih cepat, yaitu interval 20 tick (1 detik kehilangan 1%, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:277-278).
  - Cara naik kembali: Melalui method `feedAnimal` (src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:660-698), memulihkan +35% hingga maksimal 100. Dapat dipicu oleh pemain langsung (klik kanan dengan pakan), pakan dari GUI, atau satwa makan otonom di Feeding Trough (src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:462).

- Thirst (Kehausan):
  - Nilai awal: 100 (MAX_THIRST, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:41).
  - Laju turun: Interval dasar adalah 260 tick (13 detik kehilangan 1%, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:203, 224-229).
  - Kondisi yang mempercepat: Jika mod Serene Seasons mendeteksi musim panas ("SUMMER"), interval turun menjadi 160 tick (8 detik kehilangan 1%, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:208). Catatan: Karena Serene Seasons tidak terpasang, kondisi ini saat ini tidak pernah aktif.
  - Cara naik kembali: Satwa berjalan ke sumber air saat thirst <= 65 (`handleUniversalAnimalNeeds`, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:331-417). Begitu mencapai Feeding Trough berisi air atau blok air alami, thirst langsung dipulihkan ke 100 (MAX_THIRST, baris 364, 406).

- Cleanliness (Kebersihan):
  - Nilai awal: 100 (MAX_CLEANLINESS, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:44).
  - Laju turun: Kehilangan 1% setiap 240 tick (12 detik, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:239-244). Penurunan hanya berlangsung jika satwa tidak berada di dalam air dan tidak terkena hujan.
  - Kondisi yang mempercepat: Tidak ada modifier yang mempercepat interval turun. Namun jika cleanliness <= 10, satwa terkena status Poison selama 80 tick dan partikel slime (src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:248-254).
  - Cara naik kembali:
    1. Pasif: Berada di dalam air (`isInWater()`) atau terkena hujan (`isRainingAt()`) memulihkan +5% setiap 60 tick (3 detik, src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:232-238).
    2. Aktif: Pemain membersihkan satwa dengan `cleanAnimal` menggunakan Brush atau Broom (src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:615-630, src/main/java/net/zootycoon/ZooTycoonMod.java:108-110), langsung memulihkan kebersihan ke 100.

### 2. Nilai yang Dikembalikan untuk Hewan yang Belum Terdaftar

- Pada sisi SERVER:
  - `ZooAnimalHungerManager.getHunger(entity)` (baris 538, 551): Mengembalikan nilai konstanta `MAX_HUNGER` (100).
  - `ZooAnimalHungerManager.getThirst(entity)` (baris 564, 577): Mengembalikan nilai konstanta `MAX_THIRST` (100).
  - `ZooAnimalHungerManager.getCleanliness(entity)` (baris 590, 603): Mengembalikan nilai konstanta `MAX_CLEANLINESS` (100).
  Kesimpulan: Server mengembalikan nilai default palsu 100% untuk hewan yang belum terdaftar.

- Pada sisi CLIENT:
  - `ZooTycoonClient.getAnimalData(entityId)` mengembalikan `null` karena hewan yang belum terdaftar tidak pernah dikirimkan melalui paket `sendAnimalSync`.
  - `ZooAnimalHologramRenderer.java:112-115` menangani nilai null tersebut dengan fallback hardcoded: hunger = 80, thirst = 80, cleanliness = 100.
  Kesimpulan: Client menampilkan nilai default palsu (80%, 80%, 100%) yang sama sekali tidak merepresentasikan kondisi aktual.

### 3. Lokasi Pemanggilan tickServer

- `src/main/java/net/zootycoon/ZooTycoonMod.java:56`: Dipanggil di dalam event Fabric `ServerTickEvents.END_WORLD_TICK.register(level -> { ... net.zootycoon.animal.ZooAnimalHungerManager.tickServer(level); ... })`.

---

## D. Daftar Semua Pemanggil (Grep)

### 1. cleanAnimal
- `src/main/java/net/zootycoon/ZooTycoonMod.java:109`: Dipanggil pada event UseEntityCallback saat pemain mengklik kanan hewan kandidat zoo dengan Brush atau Broom untuk memulihkan kebersihan satwa.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:615`: Deklarasi method `cleanAnimal(LivingEntity animal, Player player)` yang menyetel kebersihan satwa menjadi 100, memunculkan partikel brush, dan memicu toast.

### 2. isFoodValid
- `src/main/java/net/zootycoon/ZooTycoonMod.java:115`: Memvalidasi apakah item pakan yang dipegang pemain di tangan dapat dikonsumsi oleh satwa sebelum memicu pemberian makan langsung.
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:167`: Deklarasi method `isFoodValid(Entity entity, ItemStack stack)` yang memeriksa kecocokan item terhadap diet yang didukung atau method vanilla `isFood`.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:431`: Memeriksa kecocokan pakan yang tersimpan di Feeding Trough sebelum mengizinkan mob berjalan menuju palung pakan tersebut.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:662`: Memvalidasi item makanan di awal pemanggilan method `feedAnimal` sebelum nilai hunger dinaikkan.
- `src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java:575`: Memverifikasi validitas item pakan satwa binaan zookeeper saat proses transfer makanan atau pengisian palung pakan.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1430`: Memvalidasi item pakan di inventory pemain ketika pemain menekan tombol beri makan di layar UI Animal Management Tab.

### 3. getAcceptedFoods
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:163`: Deklarasi method `getAcceptedFoods(Entity entity)` untuk mengambil daftar `List<Item>` yang dapat dimakan oleh spesies satwa tersebut.
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:171`: Dipanggil di dalam `isFoodValid` untuk memeriksa apakah item yang diuji terdapat di dalam list pakan yang diterima.

### 4. getDiet
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:130`: Deklarasi method overload `getDiet(String entityTypeId)` untuk mengambil data diet satwa berdasarkan ID string registri entitas.
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:138`: Deklarasi method overload `getDiet(Entity entity)` untuk mengambil data diet satwa dari objek Entity yang sedang hidup.
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:160`: Dipanggil pada method helper `getFeedingType` untuk memperoleh tipe feeding satwa (TROUGH atau THROWN_MEAT).
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:164`: Dipanggil pada method helper `getAcceptedFoods` untuk mengambil koleksi item pakan yang diterima satwa.
- `src/main/java/net/zootycoon/animal/AnimalDietRegistry.java:199`: Dipanggil pada method `getDietDescription` untuk mengambil teks deskripsi tipe diet satwa.
- `src/main/java/net/zootycoon/client/ui/AnimalManagementTab.java:410`: Mengambil data pakan satwa untuk merender ikon pakan dan teks deskripsi diet pada panel informasi satwa.
- `src/main/java/net/zootycoon/client/ui/AnimalManagementTab.java:740`: Mengambil daftar pakan yang diterima untuk menampilkan rincian tooltip makanan saat kursor diarahkan ke slot pakan.
- `src/main/java/net/zootycoon/client/ui/FeedingTroughDialogScreen.java:171`: Mengambil pakan yang diterima spesies untuk menampilkan pilihan item pakan yang dapat dimasukkan ke palung pakan.
- `src/main/java/net/zootycoon/client/ui/FeedingTroughDialogScreen.java:246`: Mengambil diet satwa yang telah ditetapkan ke palung pakan untuk menampilkan daftar pakan yang valid pada dialog.
- `src/main/java/net/zootycoon/client/ui/FeedingTroughDialogScreen.java:340`: Mengambil diet dari daftar satwa di dropdown penugasan palung untuk memverifikasi kompatibilitas pakan.
- `src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java:405`: Mengambil diet satwa binaan untuk menentukan jenis item pakan yang harus diisikan zookeeper ke palung pakan.
- `src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java:546`: Mengambil daftar nama makanan yang disukai untuk diteruskan ke paket payload dialog zookeeper.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1759`: Mengambil diet satwa di sisi server saat membuka GUI palung pakan guna memvalidasi isi pakan di inventory player.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1764`: Mengambil fallback diet berdasarkan species ID jika entitas satwa sedang berada di chunk yang belum termuat saat membuka UI feeding trough.

### 5. SereneSeasonsCompat.*
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:205`: Memeriksa status keberadaan mod Serene Seasons melalui `SereneSeasonsCompat.isLoaded()` di server tick.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:206`: Memanggil `SereneSeasonsCompat.getCurrentSeason(level)` untuk mengevaluasi apakah musim saat ini adalah musim kemarau ("SUMMER").
- `src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:117`: Memanggil `SereneSeasonsCompat.evaluateEnvironment(entity)` untuk mendapatkan ringkasan suhu, musim, dan catatan kenyamanan lingkungan satwa pada hologram.
- `src/main/java/net/zootycoon/compat/SereneSeasonsCompat.java:11`: Deklarasi class `SereneSeasonsCompat`.
- `src/main/java/net/zootycoon/item/MagnifyingGlassItem.java:75`: Memanggil `SereneSeasonsCompat.evaluateEnvironment(target)` saat memindai satwa dengan kaca pembesar untuk dimasukkan ke teks toast notifikasi.

### 6. ZooAnimalHologramRenderer.*
- `src/main/java/net/zootycoon/client/ZooTycoonClient.java:409`: Mereset `currentTargetedAnimal = null` saat koneksi client terputus (ClientPlayConnectionEvents.DISCONNECT).
- `src/main/java/net/zootycoon/client/ZooTycoonClient.java:502`: Memanggil `ZooAnimalHologramRenderer.render(context)` di dalam event `WorldRenderEvents.AFTER_ENTITIES` untuk menggambar hologram di dunia.
- `src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:25`: Deklarasi class `ZooAnimalHologramRenderer`.
- `src/main/java/net/zootycoon/item/MagnifyingGlassItem.java:43`: Memanggil `ZooAnimalHologramRenderer.triggerScanRaycast()` di sisi client saat pemain mulai menggunakan (klik kanan/tahan) Kaca Pembesar.
- `src/main/java/net/zootycoon/item/MagnifyingGlassItem.java:94`: Memanggil `ZooAnimalHologramRenderer.setInspectedAnimal(target)` di client saat pemain mengklik langsung entitas hidup menggunakan Kaca Pembesar.

### 7. sendAnimalSync
- `src/main/java/net/zootycoon/ZooTycoonMod.java:111`: Mengirimkan sinkronisasi data satwa ke pemain sesaat setelah berhasil menyikat atau membersihkan satwa.
- `src/main/java/net/zootycoon/ZooTycoonMod.java:118`: Mengirimkan sinkronisasi data satwa ke pemain sesaat setelah pemain memberi pakan ke satwa secara langsung.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:142`: Mengirimkan sinkronisasi data satwa ke pemain saat satwa baru berhasil didaftarkan ke kebun binatang.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:316`: Mengirimkan pembaruan data satwa secara periodik (setiap 40 tick / 2 detik) ke seluruh pemain yang sedang berada di server.
- `src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java:529`: Mengirimkan pembaruan sinkronisasi satwa ke pemain setelah satwa dihapus dari pendaftaran kebun binatang.
- `src/main/java/net/zootycoon/command/ZooCommands.java:145`: Mengirimkan sinkronisasi satwa ke pemain setelah eksekusi perintah administrasi satwa (/satwa hunger, dll.).
- `src/main/java/net/zootycoon/construction/ConstructionManager.java:521`: Mengirimkan pembaruan satwa ke pemain setelah penempatan proyek kandang konstruksi selesai.
- `src/main/java/net/zootycoon/entity/ZooHunterEntity.java:553`: Mengirimkan pembaruan satwa ke pemilik satwa setelah interaksi pemburu liar dengan satwa selesai.
- `src/main/java/net/zootycoon/item/MagnifyingGlassItem.java:81`: Mengirimkan sinkronisasi satwa ke pemain setelah proses scan dan registrasi satwa melalui Kaca Pembesar.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1441`: Mengirimkan sinkronisasi data satwa ke pemain setelah memberi pakan lewat tombol aksi GUI tab manajemen.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1453`: Mengirimkan sinkronisasi data satwa ke pemain sebagai tanggapan atas paket C2S `RequestAnimalSyncPayload`.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1732`: Mengirimkan pembaruan data satwa ke pemain setelah pemain mengubah penugasan satwa pada Feeding Trough.
- `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1875`: Deklarasi method `sendAnimalSync(ServerPlayer player)` yang mengumpulkan data satwa tagged milik pemain dan mengirimkan paket S2C SyncAnimalsPayload serta SyncZookeepersPayload.

### 8. getAnimalData
- `src/main/java/net/zootycoon/client/ZooTycoonClient.java:72`: Deklarasi method `getAnimalData(int entityId)` yang mengambil data `AnimalSyncData` dari cache `clientAnimalMap`.
- `src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:112`: Mengambil data sinkronisasi kebutuhan satwa (hunger, thirst, cleanliness) di sisi client untuk ditampilkan pada teks hologram inspeksi.
- `src/main/java/net/zootycoon/client/render/ZooWorldIndicatorRenderer.java:132`: Mengambil data sinkronisasi untuk merender bar indikator lapar terapung di dunia di atas satwa kebun binatang.

---

## E. Jalur Registrasi Jaringan (Network)

1. Registrasi Tipe Payload C2S (`PayloadTypeRegistry.playC2S()`):
   - Dipanggil di: `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1026-1058`
   - Berada di dalam method: `ZooNetworkPayloads.registerServerReceivers()`
   - Method tersebut dipanggil saat mod initialization di: `src/main/java/net/zootycoon/ZooTycoonMod.java:42`

2. Registrasi Tipe Payload S2C (`PayloadTypeRegistry.playS2C()`):
   - Dipanggil di: `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1060-1075`
   - Berada di dalam method yang sama: `ZooNetworkPayloads.registerServerReceivers()`

3. Registrasi Receiver Server (`ServerPlayNetworking.registerGlobalReceiver`):
   - Dipanggil di: `src/main/java/net/zootycoon/network/ZooNetworkPayloads.java:1078-1872` (menangani semua payload C2S seperti ClaimLand, RequestBuild, ScanAnimal, RenameAnimal, FeedAnimal, dll.)

4. Registrasi Receiver Client (`ClientPlayNetworking.registerGlobalReceiver`):
   - Dipanggil di: `src/main/java/net/zootycoon/client/ZooTycoonClient.java:177-392`
   - Berada di dalam method: `ZooTycoonClient.onInitializeClient()` (menangani payload S2C seperti SyncAnimals, SyncClaims, SyncStaff, SyncEmeraldBalance, OpenBuilderScreen, OpenZookeeperScreen, ShowToast, dll.)

---

## F. Kunci Bahasa (Lang Keys) Hologram dan Magnifying Glass Saat Ini

### 1. Kaca Pembesar (Magnifying Glass)
- File `src/main/resources/assets/zootycoon/lang/en_us.json`:
  - Baris 381: `"item.zootycoon.magnifying_glass": "Magnifying Glass"`
  - Baris 382: `"item.zootycoon.magnifying_glass.desc": "Inspect animals to view detailed health, hunger, thirst, and habitat compatibility."`
  - Baris 383: `"item.zootycoon.magnifying_glass.desc2": "Hold right-click to scan animals at a distance."`
  - Baris 388: `"zootycoon.animal.scan_toast": "Successfully scanned %s!"`
- File `src/main/resources/assets/zootycoon/lang/id_id.json`:
  - Baris 388: `"item.zootycoon.magnifying_glass": "Kaca Pembesar Satwa"`
  - Baris 389: `"item.zootycoon.magnifying_glass.desc": "Periksa satwa untuk melihat info darah, lapar, haus, dan kecocokan habitat."`
  - Baris 390: `"item.zootycoon.magnifying_glass.desc2": "Tahan klik kanan untuk memindai satwa dari kejauhan."`
  - Baris 395: `"zootycoon.animal.scan_toast": "Berhasil memindai %s!"`

### 2. Hologram (`ZooAnimalHologramRenderer`)
- Status saat ini: TIDAK ADA kunci bahasa sama sekali di `en_us.json` maupun `id_id.json`.
- Seluruh teks yang tampil pada hologram (Health, Hunger, Kebersihan, Thirst, Iklim, (Beracun!), (Kotor), (Bersih), dll.) ditulis langsung secara hardcoded menggunakan `Component.literal(...)` di `src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java:121-138`.

---

## G. Ringkasan Penyebab Hologram Tidak Bekerja Benar

1. Raycast `ProjectileUtil.getEntityHitResult` tidak mengecek collision blok padat (`level.clip`), sehingga menembus dinding dan mendeteksi satwa di balik tembok.
2. Mode render font menggunakan `Font.DisplayMode.SEE_THROUGH`, menyebabkan seluruh teks hologram bocor dan terlihat menembus struktur bangunan.
3. Timer lengket `inspectExpireTime` (+8 s/d +10 detik) menahan target saat crosshair berpindah, sehingga hologram tidak responsif dan tertinggal di layar.
4. Jangkauan raycast dan render terlalu jauh (36 hingga 48 blok), serta tidak memeriksa apakah layar GUI sedang terbuka atau pemain sedang mengaktifkan mode F1.
5. Nilai indikator hunger, thirst, dan kebersihan menampilkan angka palsu (80%, 80%, 100%) saat data `sync` bernilai null karena hewan belum terdaftar.
6. Paket sinkronisasi server `sendAnimalSync` hanya mengirim data hewan yang sudah terdaftar (`isTagged`) dan dimiliki pemain, tanpa adanya sistem inspeksi on-demand per entitas target.
7. Evaluasi iklim dan musim rusak karena mengacu ke refleksi mod Serene Seasons yang tidak terpasang (profil menggunakan Fabric Seasons), sehingga selalu macet di "SPRING".
8. Seluruh teks hologram bersifat hardcoded literal tanpa dukungan file lokalisasi bahasa (`en_us.json` dan `id_id.json`), serta dipenuhi emoji dan format warna `§` mentah.
