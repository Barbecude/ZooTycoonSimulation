## Select Parts
#data modify storage midwut:main Data set from entity @s
data modify storage midwut:main Data set value {}
data modify storage midwut:main Data.data set from entity @s data
data modify storage midwut:main Data.Pos set from entity @s Pos
scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
tag @s add phys_veh.selected_part
function physical_vehicles:zzz/vehicle/other/tag_selected_part_add with storage midwut:main Data.data.phys_veh

execute if entity @s[tag=phys_veh.debug.particles] run function physical_vehicles:zzz/vehicle/collision/mass_center

## Rotation
function physical_vehicles:zzz/vehicle/math/quaternions/calcs
#function physical_vehicles:zzz/vehicle/math/moment_of_inertia


function physical_vehicles:zzz/vehicle/math/quaternions/vector_001
function physical_vehicles:zzz/vehicle/math/quaternions/vector_010
#function physical_vehicles:zzz/vehicle/math/quaternions/vector_100
#function physical_vehicles:zzz/vehicle/player_rotate/get_rotate
execute if entity @s[tag=phys_veh.have_steering_wheel] run function physical_vehicles:zzz/vehicle/math/steering_wheel


## Pos
execute store result score #main.pos.x main_score run data get storage midwut:main Data.Pos[0] 1000
execute store result score #main.pos.y main_score run data get storage midwut:main Data.Pos[1] 1000
execute store result score #main.pos.z main_score run data get storage midwut:main Data.Pos[2] 1000

execute unless function physical_vehicles:zzz/vehicle/collision/fric_add_v_check run function physical_vehicles:zzz/vehicle/collision/fric_add_v
execute unless function physical_vehicles:zzz/vehicle/collision/fric_add_w_check run function physical_vehicles:zzz/vehicle/collision/fric_add_w

## Speedometer
scoreboard players operation #IN:arctan2.X main_score = @s phys_veh.transmission_w
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
scoreboard players operation #IN:arctan2.X main_score *= @s phys_veh.base_radius
scoreboard players operation #IN:arctan2.X main_score *= #405 const
scoreboard players operation #IN:arctan2.X main_score /= #100000 const
scoreboard players operation #kmps main_score = #IN:arctan2.X main_score
scoreboard players operation #kmps-1 main_score = #kmps main_score
scoreboard players operation #kmps main_score /= #10 const
scoreboard players operation #kmps-1 main_score %= #10 const

# Forces
scoreboard players operation #f.x main_score = @s phys_veh.gravity.x
scoreboard players operation #f.x main_score *= @s phys_veh.mass
scoreboard players operation #f.y main_score = @s phys_veh.gravity.y
scoreboard players operation #f.y main_score *= @s phys_veh.mass
scoreboard players operation #f.z main_score = @s phys_veh.gravity.z
scoreboard players operation #f.z main_score *= @s phys_veh.mass


#function physical_vehicles:zzz/vehicle/force_to_point


## Vectors
scoreboard players operation #main.velocity.x main_score = @s phys_veh.velocity.x
scoreboard players operation #main.velocity.y main_score = @s phys_veh.velocity.y
scoreboard players operation #main.velocity.z main_score = @s phys_veh.velocity.z

## HP Particle (Percent)
#execute if entity @s[tag=phys_veh.smoke.percent] run function physical_vehicles:zzz/vehicle/smoke_percent

# Driver
scoreboard players set #clutch_pedal main_score 0
scoreboard players set #brake_pedal main_score 0
scoreboard players set #throttle_pedal main_score 0
scoreboard players set #change_name main_score 0
scoreboard players set #hand_brake main_score 0
scoreboard players set #rolling_in_air main_score 0
scoreboard players set #wheel_angle main_score 0
scoreboard players set #pedal_deltatime main_score 20
scoreboard players set #gear_changing main_score 0
scoreboard players operation #engine_rpm main_score = @s phys_veh.engine_rpm
scoreboard players operation #rpm_scaling_factor main_score = @s phys_veh.rpm_scaling_factor
scoreboard players operation #gear_num main_score = @s phys_veh.gear_num
execute store result score #on_ground main_score if entity @s[tag=phys_veh.on_ground]
data modify storage midwut:main text set value [{"translate":"phys_veh.speedometer_format","fallback":"%s.%s %s","with":[{"score": {"name": "#kmps","objective": "main_score"}},{"score": {"name": "#kmps-1","objective": "main_score"}},{"translate":"phys_veh.speedometer_measurement","fallback":"km/h"}]},"   ",{"translate":"phys_veh.rpm", "fallback":"%s x%s/min", "with":[{"score":{"name":"#engine_rpm","objective":"main_score"},"color":"white"},{"score":{"name":"#rpm_scaling_factor","objective":"main_score"}}]},"   ",{"translate":"phys_veh.gear","fallback":"%s gear","with":[{"score":{"name":"#gear_num","objective":"main_score"},"color":"white"}]}]
execute if score #engine_rpm main_score >= @s phys_veh.engine_redline_rpm run data modify storage midwut:main text[2].with[0].color set value "red"
execute as @e[type=minecraft:llama,tag=phys_veh.llama_seat_dr,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/vehicle/at_parts/seat_dr
execute if entity @s[tag=!phys_veh.on_ground] unless score #rolling_in_air main_score matches 0 run function physical_vehicles:zzz/vehicle/rolling_in_air
execute unless score #change_name main_score matches 0 run function physical_vehicles:zzz/vehicle/change_name
execute unless score #afk_timer main_score = @s phys_veh.afk_timer run scoreboard players operation @s phys_veh.afk_timer = #afk_timer main_score
execute unless score #gear_num main_score = @s phys_veh.gear_num run function physical_vehicles:zzz/vehicle/engine/change_gear
execute unless score #wheel_angle main_score matches 0 run function physical_vehicles:zzz/vehicle/add_wheel_angle
execute if entity @s[tag=phys_veh.hand_brake] run scoreboard players set #hand_brake main_score 1

execute if entity @s[tag=phys_veh.have_input] run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/tick


# Engine
function physical_vehicles:zzz/vehicle/engine/pedals/main
function physical_vehicles:zzz/vehicle/engine/engine_torque
function physical_vehicles:zzz/vehicle/engine/clutch_torque
function physical_vehicles:zzz/vehicle/engine/pre
function physical_vehicles:zzz/vehicle/engine/transmission_torque


scoreboard players operation #phys_veh.control.brake main_score = @s phys_veh.control.brake
execute if entity @s[tag=phys_veh.tracked_control] run function physical_vehicles:zzz/vehicle/engine/pedals/track_copy
scoreboard players operation #phys_veh.control.throttle main_score = @s phys_veh.control.throttle

scoreboard players operation @s phys_veh.engine_rpm = @s phys_veh.engine_w
scoreboard players operation @s phys_veh.engine_rpm *= #60000 const
scoreboard players operation @s phys_veh.engine_rpm /= #pi2_3 const
execute if score @s phys_veh.engine_w matches 1.. if function physical_vehicles:zzz/vehicle/engine/sound_timer run function physical_vehicles:zzz/vehicle/engine/sound_pre
scoreboard players operation @s phys_veh.engine_rpm /= @s phys_veh.rpm_scaling_factor

# Forces
scoreboard players operation #f.x main_score /= @s phys_veh.mass
scoreboard players operation #f.x main_score /= #tick_rate_0 main_score
scoreboard players operation #f.y main_score /= @s phys_veh.mass
scoreboard players operation #f.y main_score /= #tick_rate_0 main_score
scoreboard players operation #f.z main_score /= @s phys_veh.mass
scoreboard players operation #f.z main_score /= #tick_rate_0 main_score
scoreboard players operation @s phys_veh.velocity.x += #f.x main_score
scoreboard players operation @s phys_veh.velocity.y += #f.y main_score
scoreboard players operation @s phys_veh.velocity.z += #f.z main_score

## Others
#scoreboard players operation #main.hp main_score = @s phys_veh.hp
scoreboard players operation #main.mass main_score = @s phys_veh.mass
#scoreboard players operation #main.toughness main_score = @s phys_veh.toughness

## Entity Rotation
# @s phys_veh.moment_of_inertia.x
# @s phys_veh.moment_of_inertia.y
# @s phys_veh.moment_of_inertia.z
# @s phys_veh.moment_of_inertia_no_mass.x
# @s phys_veh.moment_of_inertia_no_mass.y
# @s phys_veh.moment_of_inertia_no_mass.z

## Main
scoreboard players set #wn0.x main_score -100000
scoreboard players set #wn0.y main_score 0
scoreboard players set #wn0.z main_score 0
scoreboard players set #wn1.x main_score 100000
scoreboard players set #wn1.y main_score 0
scoreboard players set #wn1.z main_score 0
scoreboard players set #wn2.x main_score 0
scoreboard players set #wn2.y main_score 0
scoreboard players set #wn2.z main_score -100000
scoreboard players set #wn3.x main_score 0
scoreboard players set #wn3.y main_score 0
scoreboard players set #wn3.z main_score 100000
#scoreboard players set #wn3.x main_score 0
#scoreboard players set #wn3.y main_score 0
#scoreboard players set #wn3.z main_score 0
scoreboard players set #norm.x main_score 0
scoreboard players set #norm.y main_score 0
scoreboard players set #norm.z main_score 0

scoreboard players set #dv.x.x main_score 0
scoreboard players set #dv.x.y main_score 0
scoreboard players set #dv.x.z main_score 0
scoreboard players set #dw.x.x main_score 0
scoreboard players set #dw.x.y main_score 0
scoreboard players set #dw.x.z main_score 0
scoreboard players set #dw.y.x main_score 0
scoreboard players set #fz main_score 0
scoreboard players set #transmission_w main_score 0
scoreboard players set #count_wheels.total main_score 0
tag @s remove phys_veh.on_ground

execute if entity @s[tag=phys_veh.tracked_control] as @e[type=minecraft:item_display,tag=phys_veh.wheel.driving,tag=phys_veh.selected_part,x=0,scores={phys_veh.count_wheels=1..}] run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/track
function physical_vehicles:zzz/vehicle/at_parts/wheel/facing with storage midwut:main Data.rotation
execute if score #count_wheels.total main_score matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/pre

function physical_vehicles:zzz/vehicle/locking_differential/main
execute if entity @s[tag=phys_veh.tracked_control] unless function physical_vehicles:zzz/vehicle/at_parts/track/timer positioned as @s run function physical_vehicles:zzz/vehicle/at_parts/track/sound_params

execute rotated as @s as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=physical_vehicle,tag=!phys_veh.collision_point_storage,tag=!phys_veh.llama_seat_dr,tag=!phys_veh.main,tag=!phys_veh.wheel,tag=!phys_veh.custom_order,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/vehicle/at_parts/main

scoreboard players set #order_id main_score 0
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,tag=phys_veh.custom_order,tag=!phys_veh.collision_point_storage,limit=1,x=0] run function physical_vehicles:zzz/vehicle/at_parts/order/loop


execute if entity @s[tag=phys_veh.have_collision] rotated as @s run function physical_vehicles:zzz/vehicle/collision/pre

#execute if entity @s[tag=ignore_vel] store result score @s phys_veh.velocity.x store result score @s phys_veh.velocity.y run scoreboard players set @s phys_veh.velocity.z 0
execute unless entity @s[scores={phys_veh.spin.x=0,phys_veh.spin.y=0,phys_veh.spin.z=0}] run function physical_vehicles:zzz/vehicle/math/spin/main
execute unless entity @s[scores={phys_veh.velocity.x=0,phys_veh.velocity.y=0,phys_veh.velocity.z=0}] run function physical_vehicles:zzz/vehicle/math/velocity/main


tag @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,distance=..96] remove phys_veh.selected_part