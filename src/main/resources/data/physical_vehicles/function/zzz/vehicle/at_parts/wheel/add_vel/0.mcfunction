
scoreboard players operation #dv.x.x main_score /= #tick_rate_0 main_score
scoreboard players operation #dv.x.y main_score /= #tick_rate_0 main_score
scoreboard players operation #dv.x.z main_score /= #tick_rate_0 main_score
scoreboard players operation @s phys_veh.velocity.x += #dv.x.x main_score
scoreboard players operation @s phys_veh.velocity.y += #dv.x.y main_score
scoreboard players operation @s phys_veh.velocity.z += #dv.x.z main_score



scoreboard players operation #x4 main_score = #dw.x.x main_score
scoreboard players operation #y4 main_score = #dw.x.y main_score
scoreboard players operation #z4 main_score = #dw.x.z main_score

scoreboard players operation #x2 main_score = #m00_3 main_score
scoreboard players operation #x2 main_score *= #x4 main_score
scoreboard players operation #tx2 main_score = #m10_3 main_score
scoreboard players operation #tx2 main_score *= #y4 main_score
scoreboard players operation #tx3 main_score = #m20_3 main_score
scoreboard players operation #tx3 main_score *= #z4 main_score

scoreboard players operation #y2 main_score = #m01_3 main_score
scoreboard players operation #y2 main_score *= #x4 main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #y4 main_score
scoreboard players operation #ty3 main_score = #m21_3 main_score
scoreboard players operation #ty3 main_score *= #z4 main_score

scoreboard players operation #z2 main_score = #m02_3 main_score
scoreboard players operation #z2 main_score *= #x4 main_score
scoreboard players operation #tz2 main_score = #m12_3 main_score
scoreboard players operation #tz2 main_score *= #y4 main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #z4 main_score

scoreboard players operation #x2 main_score += #tx2 main_score
scoreboard players operation #x2 main_score += #tx3 main_score

scoreboard players operation #y2 main_score += #ty2 main_score
scoreboard players operation #y2 main_score += #ty3 main_score

scoreboard players operation #z2 main_score += #tz2 main_score
scoreboard players operation #z2 main_score += #tz3 main_score

function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/spinx
function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/spiny
function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/spinz


scoreboard players operation #dw.y.x main_score /= #count_driving_wheels main_score
scoreboard players operation #dw.y.x main_score *= @s phys_veh.mass
scoreboard players operation #dw.y.x main_score /= #tick_rate main_score
scoreboard players operation #dw.y.x main_score /= @s phys_veh.wheel.aligning_torque.divider
#execute if entity @s[tag=phys_veh.inverted_steering] run scoreboard players operation #dw.y.x main_score *= #-1 const
scoreboard players operation #temp main_score = @s phys_veh.wheel.angle
scoreboard players operation @s phys_veh.wheel.angle -= #dw.y.x main_score
scoreboard players operation @s phys_veh.wheel.angle > @s phys_veh.wheel.angle.left
scoreboard players operation @s phys_veh.wheel.angle < @s phys_veh.wheel.angle.right
execute if score #temp main_score matches 1.. if score @s phys_veh.wheel.angle matches ..-1 run scoreboard players set @s phys_veh.wheel.angle 0
execute if score #temp main_score matches ..-1 if score @s phys_veh.wheel.angle matches 1.. run scoreboard players set @s phys_veh.wheel.angle 0

scoreboard players set #dv.x.x main_score 0
scoreboard players set #dv.x.y main_score 0
scoreboard players set #dv.x.z main_score 0

scoreboard players set #dw.x.x main_score 0
scoreboard players set #dw.x.y main_score 0
scoreboard players set #dw.x.z main_score 0
scoreboard players set #dw.y.x main_score 0
