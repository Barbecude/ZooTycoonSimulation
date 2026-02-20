# Test Cases

## Capture Cage
1. Tangkap 1 mob kecil (mis. ayam) → tooltip menunjukkan isi 1 mob.
2. Klik kanan lagi untuk melepas → mob muncul dekat player tanpa tabrakan entity.
3. Spam klik kanan → cooldown mencegah spam (tidak spawn berkali-kali).
4. Lepas di area sempit/terhalang → muncul pesan “Area pelepasan terhalang” dan mob tidak hilang dari cage.

## Animal Tagging
1. Tag hewan normal (cow) → UI penamaan terbuka, setelah submit: nama tampil, ZooData ter-update.
2. Tag hewan rideable (zebra/horse) saat memegang Animal Tag → UI penamaan tetap diprioritaskan (bukan naik).
3. Tag hewan tinggi (giraffe) dengan klik kanan udara (raycast) → UI penamaan terbuka.
4. Tag berhasil → partikel + suara muncul.

## Toilet
1. 1 player klik toilet → player duduk di posisi duduk yang stabil.
2. 2 player klik toilet saat occupied → player kedua masuk antrian, mendapat pesan queue.
3. Player pertama keluar → player didorong keluar area pintu, pintu menutup otomatis.
4. Uji spam klik kanan oleh banyak player → tidak terjadi deadlock (occupancy jelas, queue berjalan).

## Hunter
1. Enclosure tertutup (tanpa pintu) berisi hewan bertag → hunter mendekat dan menaruh dirt untuk naik.
2. Pastikan hunter tidak merusak blok struktur (tidak break).
3. Multiple hunter + multiple hewan → tidak terjadi stutter/idle panjang.

## GameTests (Automated)
- [VisitorBehaviorGameTests.java](file:///c:/Users/chill/Downloads/ZooTycoonSimulation/src/main/java/com/example/simulation/VisitorBehaviorGameTests.java) mencakup distribusi visitor dan contoh hunter placement.
