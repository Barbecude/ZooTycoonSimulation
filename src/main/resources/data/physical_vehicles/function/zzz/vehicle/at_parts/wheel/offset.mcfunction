#> physical_vehicles:zzz/vehicle/at_parts/offset

scoreboard players operation #r.x main_score = #m00 main_score
scoreboard players operation #r.x main_score *= @s phys_veh.offset.x
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= @s phys_veh.offset.y
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= @s phys_veh.offset.z

scoreboard players operation #r.y main_score = #m10 main_score
scoreboard players operation #r.y main_score *= @s phys_veh.offset.x
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= @s phys_veh.offset.y
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= @s phys_veh.offset.z

scoreboard players operation #r.z main_score = #m20 main_score
scoreboard players operation #r.z main_score *= @s phys_veh.offset.x
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= @s phys_veh.offset.y
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= @s phys_veh.offset.z

scoreboard players operation #r.x main_score += #tx2 main_score
scoreboard players operation #r.x main_score += #tx3 main_score

scoreboard players operation #r.y main_score += #ty2 main_score
scoreboard players operation #r.y main_score += #ty3 main_score

scoreboard players operation #r.z main_score += #tz2 main_score
scoreboard players operation #r.z main_score += #tz3 main_score

scoreboard players operation #r.x main_score /= #10000 const
scoreboard players operation #r.y main_score /= #10000 const
scoreboard players operation #r.z main_score /= #10000 const


scoreboard players operation #offset.x main_score = #r.x main_score
scoreboard players operation #offset.y main_score = #r.y main_score
scoreboard players operation #offset.z main_score = #r.z main_score


function physical_vehicles:zzz/vehicle/at_parts/offset/main
function physical_vehicles:zzz/vehicle/at_parts/wheel/main


#execute positioned as @s[tag=phys_veh.get_rotation,tag=!phys_veh.get_repeat] run function physical_vehicles:zzz/vehicle/at_parts/offset_get-rot

