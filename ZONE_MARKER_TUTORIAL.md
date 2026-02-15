# 📍 **Tutorial: Zone Marker System (Custom Zoo Area)**

## 🎯 **Problem yang Diselesaikan**
- ❌ **DULU**: Radius kotak dari komputer tidak efisien (komputer di sudut = space kosong)
- ❌ **DULU**: Staff jalan terlalu jauh sampai ke luar zoo
- ❌ **DULU**: Tidak ada visual untuk lihat boundary area

## ✅ **SEKARANG: Smart Zone System**
- ✅ Define area custom dengan **2 Zone Marker** di diagonal corners
- ✅ Staff **tidak bisa** jalan lebih dari 30 blok dari home
- ✅ **Particle boundary** otomatis muncul setiap 0.5 detik (visual!)

---

## 📦 **Cara Menggunakan Zone Marker**

### Step 1: Craft Zone Marker (2 buah)
```
Item baru ada di Creative Tab: "IndoZoo Tycoon"
- Nama: "Zone Marker"
- Glow kuning/emas (agar mudah terlihat)
```

### Step 2: Taruh Zone Marker di 2 Corner Zoo
```
Contoh: Zoo ukuran 40x30 blok

[Zone Marker #1]                         [Zone Marker #2]
  (X:-10, Z:-15)                           (X:30, Z:15)
        |                                        |
        +----------------------------------------+
        |                                        |
        |         🐔 🐄 🐷 🐑                    |
        |                                        |
        |         [Zoo Computer]                 |
        |         (X:10, Z:0)                    |
        |                                        |
        +----------------------------------------+
```

**Catatan PENTING:**
- Marker #1 dan #2 bisa di **ANY diagonal corner** (tidak harus northwest-southeast)
- Komputer **TIDAK HARUS** di center! ( جاز kalau kamu taruh di sudut)
- Zoo Computer akan auto-detect 2 marker terdekat dalam radius 50 blok

---

### Step 3: Lihat Particle Boundary
Setelah Zone Marker dipasang, dalam **10 ticks (0.5 detik)** kamu akan lihat:
- ⭐ **Vertical particle lines** di 4 corner (warna putih END_ROD)
- ⭐ **Horizontal particle lines** di ground level (outline boundary)

**Visualnya seperti ini:**
```
    *           *           *           *
    |                                   |
    |     Zoo Area (Safe Zone)          |
    |                                   |
    *___________________________________*
    ^                                   ^
  Corner                              Corner
```

Particles akan **terus muncul tiap 0.5 detik** selama mod running.

---

## 🚶 **Staff Movement Restriction**

### Sebelum (Masalah):
- Staff jalan kemana-mana sampai 100+ blok dari zoo
- Susah track posisi staff
- Staff bisa "hilang" ke hutan/gunung

### Sekarang (Fixed):
- Staff punya **Home Position** = posisi Zoo Computer
- **Max distance**: 30 blok dari home
- Jika staff jalan > 30 blok → **otomatis walk back** ke home

**Code Logic:**
```java
// Setiap tick, staff cek jarak dari home
if (distance > 30 blok) {
    navigasi.moveTo(homePos); // Pulang!
}
```

---

## 🔍 **Cara Cek Apakah Zone Aktif**

### Opsi 1: Lihat Particle
- Jika ada particle boundary → Zone aktif (custom area)
- Jika tidak ada particle → Fallback ke radius mode (20 blok kotak dari komputer)

### Opsi 2: Check Log Server
```
[IndoZoo] Zone markers detected: 2
[IndoZoo] Custom zone: (-10, 60, -15) to (30, 70, 15)
```

---

## 📊 **Comparison: Radius vs Zone Marker**

| Feature | Radius Mode (Default) | Zone Marker Mode |
|---------|----------------------|------------------|
| **Shape** | Kotak dari komputer | Custom rectangle |
| **Flexibility** | Komputer harus di center | Komputer bisa dimana saja |
| **Visual** | Tidak ada | Particle boundary ✅ |
| **Efficiency** | Space kosong jika komputer di sudut | Optimal! |
| **Staff constraint** | 30 blok dari komputer | 30 blok dari komputer |

---

## 💡 **Tips & Best Practices**

### 1. **Ukuran Zone Ideal**
- **Minimum**: 20x20 blok (cukup untuk 5-10 hewan)
- **Recommended**: 40x40 blok (comfortable untuk 20+ hewan)
- **Maximum**: 100x100 blok (performa mulai affected)

### 2. **Posisi Komputer Optimal**
- Dengan zone marker, komputer **tidak harus di tengah**
- Taruh komputer di **entrance** atau **staff room** untuk kemudahan akses
- Staff akan tetap restricted dalam zone markers

### 3. **Multiple Zones** (Advanced)
- Satu Zoo Computer **hanya bisa** detect 1 zone (2 markers)
- Jika ada >2 markers → System pakai 2 markers **terdekat** ke komputer
- Untuk multiple zones → Pasang multiple Zoo Computers

### 4. **Vertical Limits**
- Zone height = dari marker Y - 10 sampai Y + 10
- Total coverage: 20 blok vertical
- Hewan diluar range vertical tidak terdetect

---

## 🐛 **Troubleshooting**

### "Particle tidak muncul!"
**Solusi:**
- Cek apakah kamu sudah taruh **2 Zone Markers** (min requirement)
- Markers harus dalam **50 blok** dari Zoo Computer
- Tunggu 20 detik (satu full cycle) untuk deteksi pertama

### "Staff masih jalan jauh!"
**Solusi:**
- Staff max distance = 30 blok dari **komputer** (bukan dari zone boundary)
- Jika zone 100x100 tapi komputer di corner → Staff bisa stuck di sisi lain
- **Fix**: Taruh komputer lebih ke center atau kurangi zone size

### "Hewan tidak terdetect padahal dalam zone"
**Solusi:**
- Cek apakah hewan dalam **vertical range** (Y ± 10 dari marker)
- Tunggu 1 full economy cycle (20 detik)
- Cek dashboard: "Hewan: X" (real-time count)

### "Visitor tidak spawn meskipun ada Banner & hewan"
**Solusi:**
- Banner harus **dalam zone bounds** (tidak boleh di luar)
- Cek apakah `Hewan >= 2`
- Cek apakah `Visitor < 8` (max limit)

---

## 🎨 **Custom Zone Examples**

### Example 1: Long Zoo (Safari Style)
```
Markers: (0, 0) to (100, 20)
Shape: 100 x 20 blok (rectangle panjang)
Animals: Path dengan hewan di kiri-kanan
```

### Example 2: Square Zoo (Traditional)
```
Markers: (-25, -25) to (25, 25)
Shape: 50 x 50 blok (square)
Animals: Center pen dengan fence
```

### Example 3: L-Shaped Zoo (Corner lot)
```
TIDAK BISA! Zone selalu rectangle.
Workaround: Taruh 2 Zoo Computers untuk 2 separate zones
```

---

## 🔄 **Migration dari Radius ke Zone**

Jika kamu sudah punya zoo dengan radius mode:
1. Craft 2 Zone Markers
2. Taruh di corner zoo yang ada
3. Dalam 20 detik, system auto-switch ke zone mode
4. Particle boundary akan langsung muncul
5. **Data tetap aman** (balance, staff, dll)

---

**Selamat! Sekarang kamu bisa define zoo area dengan lebih flexible!** 🎉
