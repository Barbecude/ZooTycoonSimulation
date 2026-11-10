---
trigger: model_decision
description: When i working on zooTycoonSimulation minecraft mod
---
use C:\Users\Zevaldo\Documents\my mods\zooTycoonSimulation\.agents\skills\explore-index

commit & push Setiap fitur baru commit layaknya profesional
no bold font, no emoji. jangan over info jangan terlalu banyak info duplikats di dialog atau toast. no over colored font. Jangan ubah dialog kecuali memang di minta
Wajib membaca docs/hologram/RULES.md dan docs/hologram/PROGRESS.md sebelum mengerjakan fitur hologram.

C:\Users\Zevaldo\AppData\Roaming\ModrinthApp\profiles\Fresh & Smooth\mods pindah mod ke sini kalo sudah beres build akhir

mod source yg dipakai untuk mencari tau isi kode atau mixin:
https://github.com/Codx-org/AlexsMobsContinued
https://github.com/crispytwig/Naturalist
https://github.com/axperty/moredelight
https://github.com/MehVahdJukaar/FarmersDelightRefabricated
https://github.com/Let-s-Do-Collection/FarmAndCharm

data hewan:
https://docs.google.com/document/d/1jJZwJqmeuR9pmZL68d9drwBwZZVqxTd8_w1RZepzcl4

pindahkan semua string yg ada di kode ini ke src\main\resources\assets\zootycoon\lang\id_id.json
src\main\resources\assets\zootycoon\lang\en_us.json


Scope guard yang ketat: Dilarang mengubah atau menyentuh file, class, dan komponen UI yang tidak disebutkan secara eksplisit dalam instruksi pengguna. Jika tugas hanya menyebut feeding trough, jangan pernah menyentuh file zookeeper, cashier, builder, atau file lainnya.

Larangan mengubah representasi visual/fitur: Dilarang mengganti render 3D entitas (InventoryScreen) menjadi item render 2D, atau memodifikasi tata letak UI yang sudah ada tanpa instruksi eksplisit.

Konfirmasi sebelum refactor: Saat melakukan bugfix, fokus hanya pada perbaikan bug yang dilaporkan. Dilarang melakukan optimasi atau refactoring tambahan atas inisiatif sendiri.

jangan pernah kasih zookeper awalan makanan, hanya player yang harus ngasih makanan ke zookeper setelah itu zookeper baru ngisi makanan ke feeding through

lokasi makanan satwa AnimalDietRegistry.java

fungsi yg udah fix (tidak boleh sampe rusak karena mengubah kode:
1. Logic builder dan ui dialog nya
2. Logic pemilihan zookeper untuk hewan
3. dialog zookeper dan ui nya
3. Logic dan ui cashier
4. logic antri
5. pengunjung / visitor
6. hunter
7. ui feeding through       
8. builder notes & ui notes
9. logika magnifying glass, animasi hologram
10. Reaksi satwa terhadap environtment, suhu, musim, biome,
11. Khusus satwa yang butuh air dia akan auto jalan ke air
12. Khusus Satwa yang bisa mencari makan sendiri dia akan makan lewat feeding thrugh yang di isi oleh zookeper (berkaitan dengan sistem lapar)
fungsi yang belum fix atau belum ada atau masih cacat (Dilarang mengerjakan semua tahap ini sebelum diminta secara eksplisit oleh pengguna):
13. Khusus Satwa yang perlu di kasi makan langsung dia akan di beri pakan langsung oleh zookeper (berkaitan dengan sistem lapar)
14. ui dialog button action zookeper
15. fungsi zookeper membersihkan satwa dengan alat
15. ui hologram satwa
17. hologram progress bar watering can dan brush (tapi icon nya belum)
18. magnifying glass logic
19. Gudang. empty barrel ui gui button and logic
20. cashier logic
21. Vehicle logic and assets
22. inspector logic

fungsi yg belum ada atau masih cacat (jangan eksekusi fitur ini sebelum secara eksplisit diminta) 

1. feedign through & zookeper
