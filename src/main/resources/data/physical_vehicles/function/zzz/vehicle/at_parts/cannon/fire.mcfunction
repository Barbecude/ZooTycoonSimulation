tag @s add phys_veh.cannon.is_recoiling

scoreboard players operation #input_id main_score = @s phys_veh.id.input
execute as @e[type=#physical_vehicles:seat,limit=1,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/input_id,predicate=midwut:check/passenger/any,x=0] on passengers run scoreboard players operation #player_id main_score = @s midwut.player_id
scoreboard players operation #vehicle_id main_score = @s phys_veh.id.vehicle

data modify storage midwut:main cannon set from entity @s data.phys_veh.cannon
execute unless score #phys_veh.infinity_shells main_score matches 1 run function physical_vehicles:zzz/vehicle/at_parts/cannon/shell_clear
execute store result score #mass main_score run data get storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh.mass 10
execute store result score #cannon_impulse main_score run data get storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh.start_velocity 10
scoreboard players operation #proj_velocity main_score = #cannon_impulse main_score
scoreboard players operation #cannon_impulse main_score *= #mass main_score
scoreboard players operation #cannon_impulse main_score *= #13 const
scoreboard players operation #force main_score = #cannon_impulse main_score
scoreboard players operation #force main_score /= #100 const
#tellraw @a [{score:{name:"#force",objective:"main_score"}}]
scoreboard players operation #force main_score *= @s phys_veh.max_delta_neg
#scoreboard players operation #force main_score /= #10 const
scoreboard players operation #force main_score /= #main.mass main_score

scoreboard players operation #rx main_score = @s phys_veh.m02
scoreboard players operation #ry main_score = @s phys_veh.m12
scoreboard players operation #rz main_score = @s phys_veh.m22
scoreboard players operation #rx main_score *= #force main_score
scoreboard players operation #ry main_score *= #force main_score
scoreboard players operation #rz main_score *= #force main_score
scoreboard players operation #rx main_score /= #tick_rate000- main_score
scoreboard players operation #ry main_score /= #tick_rate000- main_score
scoreboard players operation #rz main_score /= #tick_rate000- main_score
scoreboard players operation #vx main_score = #rx main_score
scoreboard players operation #vy main_score = #ry main_score
scoreboard players operation #vz main_score = #rz main_score
scoreboard players operation #rx main_score /= #10 const
scoreboard players operation #ry main_score /= #10 const
scoreboard players operation #rz main_score /= #10 const


#tellraw @a [{score:{name:"#vz",objective:"main_score"}}]
scoreboard players operation #pos.x main_score -= #main.pos.x main_score
scoreboard players operation #pos.y main_score -= #main.pos.y main_score
scoreboard players operation #pos.z main_score -= #main.pos.z main_score


#tellraw @a [{score:{name:"#rx",objective:"main_score"}},"  ",{score:{name:"#ry",objective:"main_score"}},"  ",{score:{name:"#rz",objective:"main_score"}}]

scoreboard players operation #x main_score = #pos.y main_score
scoreboard players operation #x main_score *= #rz main_score
scoreboard players operation #temp main_score = #pos.z main_score
scoreboard players operation #temp main_score *= #ry main_score
scoreboard players operation #x main_score -= #temp main_score
scoreboard players operation #x main_score /= #1000 const

scoreboard players operation #y main_score = #pos.x main_score
scoreboard players operation #y main_score *= #rz main_score
scoreboard players operation #temp main_score = #pos.z main_score
scoreboard players operation #temp main_score *= #rx main_score
scoreboard players operation #y main_score -= #temp main_score
scoreboard players operation #y main_score /= #-1000 const

scoreboard players operation #z main_score = #pos.x main_score
scoreboard players operation #z main_score *= #ry main_score
scoreboard players operation #temp main_score = #pos.y main_score
scoreboard players operation #temp main_score *= #rx main_score
scoreboard players operation #z main_score -= #temp main_score
scoreboard players operation #z main_score /= #1000 const

#tellraw @a [{score:{name:"#x",objective:"main_score"}},"  ",{score:{name:"#y",objective:"main_score"}},"  ",{score:{name:"#z",objective:"main_score"}}]

scoreboard players operation #x2 main_score = #m00_3 main_score
scoreboard players operation #x2 main_score *= #x main_score
scoreboard players operation #tx2 main_score = #m10_3 main_score
scoreboard players operation #tx2 main_score *= #y main_score
scoreboard players operation #tx3 main_score = #m20_3 main_score
scoreboard players operation #tx3 main_score *= #z main_score

scoreboard players operation #y2 main_score = #m01_3 main_score
scoreboard players operation #y2 main_score *= #x main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #y main_score
scoreboard players operation #ty3 main_score = #m21_3 main_score
scoreboard players operation #ty3 main_score *= #z main_score

scoreboard players operation #z2 main_score = #m02_3 main_score
scoreboard players operation #z2 main_score *= #x main_score
scoreboard players operation #tz2 main_score = #m12_3 main_score
scoreboard players operation #tz2 main_score *= #y main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #z main_score

scoreboard players operation #x2 main_score += #tx2 main_score
scoreboard players operation #x2 main_score += #tx3 main_score

scoreboard players operation #y2 main_score += #ty2 main_score
scoreboard players operation #y2 main_score += #ty3 main_score

scoreboard players operation #z2 main_score += #tz2 main_score
scoreboard players operation #z2 main_score += #tz3 main_score



#tellraw @a [{score:{name:"#x3",objective:"main_score"}},"  ",{score:{name:"#y3",objective:"main_score"}},"  ",{score:{name:"#z3",objective:"main_score"}}]
function physical_vehicles:zzz/vehicle/at_parts/cannon/sound with entity @s data.phys_veh.cannon.fire_sound

execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,tag=!phys_veh.debug.shoot_impulse,limit=1,x=0] run function physical_vehicles:zzz/vehicle/at_parts/cannon/vehicle_vels



scoreboard players operation #cannon_impulse main_score /= @s phys_veh.mass
scoreboard players operation @s phys_veh.velocity.z -= #cannon_impulse main_score


execute store result storage midwut:main macros.x float 0.0001 run scoreboard players get @s phys_veh.m02
execute store result storage midwut:main macros.y float 0.0001 run scoreboard players get @s phys_veh.m12
execute store result storage midwut:main macros.z float 0.0001 run scoreboard players get @s phys_veh.m22
execute positioned as @s summon minecraft:item_display run function physical_vehicles:zzz/vehicle/at_parts/cannon/shell_spawn
execute positioned as @s summon minecraft:marker if function physical_vehicles:zzz/vehicle/math/quaternions/macro_facing_pre rotated as @s run function physical_vehicles:zzz/vehicle/at_parts/cannon/particles with storage midwut:main cannon.fire_particles