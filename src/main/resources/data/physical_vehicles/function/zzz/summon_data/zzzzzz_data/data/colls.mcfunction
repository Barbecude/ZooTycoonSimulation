#tag @s add physical_vehicle
#tag @s add phys_veh.collision_point_storage
execute store result score #x main_score run data get storage midwut:main temp[-1][0] 1000
execute store result score #y main_score run data get storage midwut:main temp[-1][1] 1000
execute store result score #z main_score run data get storage midwut:main temp[-1][2] 1000
execute store result storage midwut:main coll[0] int 1 run scoreboard players operation #x main_score -= #mass_center.x main_score
execute store result storage midwut:main coll[1] int 1 run scoreboard players operation #y main_score -= #mass_center.y main_score
execute store result storage midwut:main coll[2] int 1 run scoreboard players operation #z main_score -= #mass_center.z main_score
#ride @s mount @e[type=minecraft:text_display,tag=get_id,tag=phys_veh.main,limit=1,x=0]

data modify storage midwut:main colls append from storage midwut:main coll
data remove storage midwut:main temp[-1]
execute if data storage midwut:main temp[0] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/colls