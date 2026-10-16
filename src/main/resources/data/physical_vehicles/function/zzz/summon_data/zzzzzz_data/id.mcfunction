tag @s remove get_id
data modify storage midwut:main Data2 set from entity @s

execute if entity @s[type=llama,tag=phys_veh.llama_seat] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/llama_seat
execute if entity @s[type=item_display] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/item_display
execute if entity @s[type=text_display] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/text_display

scoreboard players operation @s phys_veh.id.vehicle = #physical_vehicles.id.vehicle main_score


#tp @s ~ ~ ~ 0 0