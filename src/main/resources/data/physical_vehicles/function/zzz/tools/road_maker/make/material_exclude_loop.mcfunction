$execute if block ~ ~ ~ $(b) run return 1
data remove storage midwut:main road_exclude_[-1]
execute unless data storage midwut:main road_exclude_[0] run return fail
return run function physical_vehicles:zzz/tools/road_maker/make/material_exclude_loop with storage midwut:main road_exclude_[-1]
