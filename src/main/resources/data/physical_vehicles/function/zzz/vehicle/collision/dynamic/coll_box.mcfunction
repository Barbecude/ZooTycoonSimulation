
scoreboard players operation #r2.x main_score = #pos.x main_score
scoreboard players operation #r2.y main_score = #pos.y main_score
scoreboard players operation #r2.z main_score = #pos.z main_score
scoreboard players operation #r2.x main_score -= @s phys_veh.pos.x
scoreboard players operation #r2.y main_score -= @s phys_veh.pos.y
scoreboard players operation #r2.z main_score -= @s phys_veh.pos.z

scoreboard players operation #r3.x main_score = @s phys_veh.m00
scoreboard players operation #r3.x main_score *= #r2.x main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m10
scoreboard players operation #tx2 main_score *= #r2.y main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m20
scoreboard players operation #tx3 main_score *= #r2.z main_score

scoreboard players operation #r3.y main_score = @s phys_veh.m01
scoreboard players operation #r3.y main_score *= #r2.x main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11
scoreboard players operation #ty2 main_score *= #r2.y main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m21
scoreboard players operation #ty3 main_score *= #r2.z main_score

scoreboard players operation #r3.z main_score = @s phys_veh.m02
scoreboard players operation #r3.z main_score *= #r2.x main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m12
scoreboard players operation #tz2 main_score *= #r2.y main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22
scoreboard players operation #tz3 main_score *= #r2.z main_score

scoreboard players operation #r3.x main_score += #tx2 main_score
scoreboard players operation #r3.x main_score += #tx3 main_score

scoreboard players operation #r3.y main_score += #ty2 main_score
scoreboard players operation #r3.y main_score += #ty3 main_score

scoreboard players operation #r3.z main_score += #tz2 main_score
scoreboard players operation #r3.z main_score += #tz3 main_score

scoreboard players operation #r3.x main_score /= #10000 const
scoreboard players operation #r3.y main_score /= #10000 const
scoreboard players operation #r3.z main_score /= #10000 const

execute if score #r3.x main_score >= @s phys_veh.collision_box.nx if score #r3.x main_score <= @s phys_veh.collision_box.px if score #r3.y main_score >= @s phys_veh.collision_box.ny if score #r3.y main_score <= @s phys_veh.collision_box.py if score #r3.z main_score >= @s phys_veh.collision_box.nz if score #r3.z main_score <= @s phys_veh.collision_box.pz run function physical_vehicles:zzz/vehicle/collision/dynamic/coll