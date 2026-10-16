execute store result storage midwut:main temp int 1 run scoreboard players add #temp main_score 1
data modify storage midwut:main Data.data.phys_veh.transmission.transfers[0].id set string storage midwut:main temp
data modify entity @s data.phys_veh.transmission.transfers append from storage midwut:main Data.data.phys_veh.transmission.transfers[0]
data remove storage midwut:main Data.data.phys_veh.transmission.transfers[0]
execute if data storage midwut:main Data.data.phys_veh.transmission.transfers[0] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/transfers_loop