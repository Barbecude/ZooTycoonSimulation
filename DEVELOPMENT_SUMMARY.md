# IndoZoo Tycoon - Development Summary

## Session Date: 2026-02-14

### ✅ FEATURES COMPLETED

#### 1. **Dynamic Animal Shop System** (AUTO-DISCOVERY)
- **Problem Solved**: Tidak perlu hardcode hewan baru ke kode
- **Implementation**: 
  - `AnimalRegistry.java` scan semua EntityType dari ForgeRegistries
  - Auto-detect semua hewan dari vanilla + mod (Alex's Mobs, dll)
  - Dynamic UI dengan scroll (▲/▼ button)
  - Price calculation berdasarkan namespace (vanilla=Rp3k, mod=Rp8k)
- **Benefit**: Zero maintenance! Install mod baru → hewan langsung masuk toko

#### 2. **Config File System**
- **File**: `ZooConfig.java` 
- **Format**: TOML (`config/indozoo-common.toml`)
- **Configurable**:
  - Economy: Starting balance, income rates, penalties
  - Costs: Staff hiring, upgrades, food
  - Animals: Vanilla vs Mod pricing
  - Gameplay: Scan radius, intervals, visitor timings
- **Benefit**: Server admin bisa customize tanpa recompile

#### 3. **Staff AI Improvements**
- **OpenDoorGoal**: Staff bisa membuka pager/pintu otomatis
- **Feed Cooldown**: 10 detik cooldown agar tidak spam love particles
- **Smart Feeding**: Hanya feed jika hewan HP < max ATAU belum in-love

#### 4. **Visitor Lifecycle System**
- **Gate System**: NPC hanya spawn jika ada Banner dalam radius
- **AI Goals**:
  - `WatchAnimalsGoal`: Menonton hewan selama beberapa detik
  - `LeaveZooGoal`: Pulang ke gate setelah 5 menit
- **Persistence**: Gate position & stay time disimpan di NBT

#### 5. **3D Computer Block**
- **Model**: Custom 3D (monitor + pole + base)
- **Transparency**: `noOcclusion` untuk efek kaca
- **Dynamic State**: 
  - OFF (black screen) jika tidak ada hewan
  - ON (light blue glow) jika ada hewan terdeteksi

#### 6. **GUI Dashboard**
- **Design**: Premium dark theme dengan border ungu
- **Features**:
  - Real-time stats (saldo, hewan, staff, visitor, radius)
  - Dynamic animal shop dengan pagination
  - Management buttons (Hire, Upgrade)
- **Format**: Rupiah currency dengan separator (e.g., Rp 15.000)

#### 7. **Permanent HUD Overlay**
- **Display**: Top-left corner dengan background transparan
- **Info**: Saldo, Radius, Status
- **Radius**: Tetap tampil dalam 100m dari computer

#### 8. **Command System**
- `/zoocmd buy <animal_id>`: Dynamic animal purchase (support ResourceLocation)
- `/zoocmd hire`: Recruit staff
- `/zoocmd upgrade`: Increase scan radius
- `/zoocmd addmoney <amount>`: Dev/testing tool
- `/zoocmd reset`: Reset progress (command-only untuk keamanan)

#### 9. **Spawn Eggs**
- Staff Spawn Egg (testing)
- Visitor Spawn Egg (testing)

#### 10. **Economy System**
- **Income**: Rp500 per hewan per cycle (20s)
- **Costs**: Staff salary Rp200, trash penalty Rp100
- **Balance Sync**: ContainerData untuk real-time update di GUI

---

### 🚧 IN PROGRESS (Belum Selesai)

#### 11. **Staff Specialization** (50% done)
- **Next**: Create 3 EntityTypes (Zookeeper, Janitor, Veterinarian)
- **Next**: Different AI goals per type
- **Next**: Different textures/uniforms

#### 12. **Ticket Booth Block** (Not started)
- **Plan**: Block yang diletakkan dekat gate
- **Logic**: Visitor bayar tiket → Rp500 masuk saldo
- **UI**: Crafting recipe

#### 13. **Animal Happiness System** (Not started)
- **Plan**: Stat 0-100% per hewan
- **Factors**: Food, crowd, enclosure, water
- **Impact**: Visitor payment modifier

---

### 🔧 TECHNICAL DETAILS

**Minecraft Version**: 1.20.1  
**Forge Version**: 47.4.10  
**Mod ID**: `indozoo`  

**Build Status**: ✅ BUILD SUCCESSFUL  
**JAR Location**: `build/libs/indozoo-1.0.0.jar`

---

### 📁 FILE STRUCTURE

```
src/main/java/com/example/simulation/
├── IndoZooTycoon.java          # Main mod class, registries
├── ZooConfig.java              # Config system (NEW)
├── AnimalRegistry.java         # Auto-discovery system (NEW)
│
├── blocks/
│   ├── ZooComputerBlock.java   # 3D block with ACTIVE state
│   └── ZooComputerBlockEntity.java  # Core logic, economy, spawning
│
├── entities/
│   ├── StaffEntity.java        # AI worker (feed animals)
│   ├── VisitorEntity.java      # AI customer (watch → leave)
│   └── goals/
│       ├── RefillFoodGoal.java
│       ├── FeedAnimalGoal.java
│       └── (WatchAnimalsGoal & LeaveZooGoal inline)
│
├── gui/
│   ├── ZooComputerMenu.java    # Container sync
│   ├── ZooComputerScreen.java  # Dynamic UI with scroll
│   └── ZooOverlay.java         # Permanent HUD
│
├── commands/
│   └── ZooCommand.java         # All /zoocmd subcommands
│
└── client/
    └── ClientEvents.java       # Renderer registration, events

src/main/resources/assets/indozoo/
├── blockstates/
│   └── zoo_computer.json       # 8 variants (4 facing × 2 active)
├── models/block/
│   ├── zoo_computer_off.json   # Dark screen
│   └── zoo_computer_on.json    # Glowing screen
└── models/item/
    └── zoo_computer.json       # Item model
```

---

### 🐛 KNOWN ISSUES

1. **Deprecation Warnings**:
   - `ModLoadingContext.get()` deprecated in 1.21.1 (not affecting 1.20.1)
   - `ResourceLocation` constructor deprecated (we fixed with `fromNamespaceAndPath`)

2. **Performance Concerns**:
   - Multiple Zoo Computers scanning simultaneously could cause TPS lag
   - **Solution needed**: Cache scan results or increase cooldown

3. **Gate Detection**:
   - Only scans for standing Banner, not Wall Banner
   - **To test**: Does it work with different banner colors?

---

### 📋 NEXT STEPS (Priority Order)

1. ✅ **Config System** (DONE)
2. **Staff Specialization**:
   - Create `ZookeeperEntity`, `JanitorEntity`, `VeterinarianEntity`
   - Update hire command with type parameter
   - Update GUI with dropdown selector
3. **Ticket Booth Block**:
   - Create `TicketBoothBlock` + BlockEntity
   - Visitor AI: Walk to booth → pay → proceed
   - Crafting recipe
4. **Animal Happiness**:
   - Add happiness field to tracked animals (WorldData?)
   - Scan for enclosure quality
   - Modify visitor income based on happiness
5. **Particle & Sound Effects**
6. **Custom Textures** for Staff/Visitor

---

### 💡 DESIGN DECISIONS

**Why auto-discovery instead of manual registration?**  
→ Scalability! Works with ANY animal mod without code changes.

**Why TOML config instead of JSON?**  
→ More human-readable, better for server admins.

**Why separate staff types instead of one entity with variants?**  
→ Cleaner code, easier to extend AI per type.

**Why 20-second economy cycle?**  
→ Balance between responsiveness and performance.

---

### ⚠️ WARNINGS FOR FUTURE DEVELOPMENT

1. **Don't remove AnimalRegistry.initialize()** from FMLCommonSetupEvent → registry kosong!
2. **ContainerData max value = 65535** (short) → Saldo max Rp 65jt unless we use 2 shorts
3. **Visitor spawn rate** perlu dibatasi agar tidak flooding server
4. **Config changes** require restart (Forge limitation)

---

**Total Lines of Code Added**: ~2000+  
**Total Development Time**: ~4 hours  
**Bugs Fixed**: 5 major compilation errors

**Status**: ✅ Production-ready untuk testing dengan mod pack!
