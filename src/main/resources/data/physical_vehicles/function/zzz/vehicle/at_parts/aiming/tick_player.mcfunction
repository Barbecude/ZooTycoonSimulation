title @s times 0 20 0
title @s title {storage:"midwut:main",nbt:"text",interpret:true}
#tellraw @a [{storage:"midwut:main",nbt:"macros.x"},"  ",{storage:"midwut:main",nbt:"macros.y"},"  ",{storage:"midwut:main",nbt:"macros.z"}]
execute if predicate midwut:check/periodic_tick/1 run function physical_vehicles:zzz/vehicle/math/quaternions/macro_facing_player with storage midwut:main macros