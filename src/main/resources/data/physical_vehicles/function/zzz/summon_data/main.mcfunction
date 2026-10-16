
scoreboard players set #physical_vehicles.id.part main_score 0
scoreboard players set #physical_vehicles.id.seat main_score 0
scoreboard players add #physical_vehicles.id.vehicle main_score 1
#tag @s add phys_veh.selected_part
data modify storage midwut:main data set from entity @s[type=!player]
execute if entity @s[tag=!phys_veh.spawn] run data modify storage midwut:main data set value "summoned"
data modify storage midwut:main vehicle_name set value {"translate":"phys_veh.physical_vehicle","fallback":"Physical Vehicle"}
scoreboard players set #tracked_control main_score 0
scoreboard players set #have_on_board_clutch_lock main_score 0

execute as @e[type=minecraft:text_display,tag=get_id,tag=phys_veh.main,limit=1,x=0] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/main
execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=!phys_veh.main,tag=get_id,x=0] run function physical_vehicles:zzz/summon_data/zzzzzz_data/id
#tag @s remove phys_veh.selected_part

## Kill
tp @s[type=!player] ~ -10000 ~
kill @s[type=!player]
