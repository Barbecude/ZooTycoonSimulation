data modify storage midwut:main aiming.builded_title set value [{"text":"",shadow_color:0}]
data modify storage midwut:main aiming.builded_title[0].font set from storage midwut:main aiming.title.font

function physical_vehicles:zzz/vehicle/at_parts/aiming/build_text_background with storage midwut:main aiming.title

data modify storage midwut:main aiming.builded_title append from storage midwut:main aiming.title.static

scoreboard players operation #temp main_score = @s phys_veh.entity_rotation.x
#execute if score #temp main_score matches 31416.. run scoreboard players remove #temp main_score 62831
scoreboard players operation #temp main_score /= #-10 const
scoreboard players operation #temp main_score *= @s phys_veh.aiming.angle_multiplier
scoreboard players operation #temp main_score /= #100000 const
scoreboard players operation #temp main_score > @s phys_veh.aiming.offset_min
scoreboard players operation #temp main_score < @s phys_veh.aiming.offset_max
execute store result storage midwut:main macros.frame int 1 run scoreboard players get #temp main_score

execute if entity @s[tag=phys_veh.debug.frame] run tellraw @p [{storage:"midwut:main",nbt:"macros.frame"}]
data modify storage midwut:main temp set from storage midwut:main aiming.title.dynamic
function physical_vehicles:zzz/vehicle/at_parts/aiming/build_text_dloop



data modify entity @s data.phys_veh.aiming.builded_title set from storage midwut:main aiming.builded_title