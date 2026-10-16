#> physical_vehicles:zzz/player/driver/main
execute if entity @s[type=#midwut:living/all,tag=!phys_veh.has_seat_improvements] if function physical_vehicles:zzz/vehicle/seat/have_improvements run function physical_vehicles:zzz/player/improvements/main
execute if entity @s[tag=phys_veh.gear_changing] run data modify storage midwut:main text[4].with[0].color set value "green"
title @s actionbar [{"storage":"midwut:main","nbt":"text","interpret":true}]

execute if predicate midwut:check/input/any_wasd if score #afk_timer main_score matches 121.. run scoreboard players set #afk_timer main_score 118

execute if score #on_ground main_score matches 0 run function physical_vehicles:zzz/player/driver/rolling_in_air
execute if score #on_ground main_score matches 1 run tag @s remove phys_veh.rolling_in_air
function physical_vehicles:zzz/player/driver/turning_steering_wheel_tag
execute if predicate midwut:check/input/left unless predicate midwut:check/input/right run scoreboard players add #wheel_angle main_score 1
execute if predicate midwut:check/input/right unless predicate midwut:check/input/left run scoreboard players remove #wheel_angle main_score 1
execute if predicate midwut:check/input/sprint run scoreboard players set #clutch_pedal main_score 1
execute if predicate midwut:check/input/backward run scoreboard players set #brake_pedal main_score 1
execute if predicate midwut:check/input/forward run scoreboard players set #throttle_pedal main_score 1
execute if predicate midwut:check/input/jump run scoreboard players set #hand_brake main_score 1

execute if entity @s[tag=phys_veh.gear_changing] run function physical_vehicles:zzz/player/driver/gear_change/tick
execute if items entity @s weapon.mainhand *[minecraft:custom_data~{phys_veh.vehicle_controller:true}] run function physical_vehicles:zzz/player/driver/gear_change/main

execute unless items entity @s weapon.offhand *[minecraft:custom_data~{phys_veh.vehicle_controller:true}] unless items entity @s weapon.offhand * run item replace entity @s weapon.offhand with minecraft:poisonous_potato[minecraft:custom_data={phys_veh.vehicle_controller:true},minecraft:consumable={consume_seconds:2147483647,has_consume_particles:false},minecraft:food={nutrition:0,saturation:0,can_always_eat:true},minecraft:custom_name={"translate":"phys_veh.vehicle_controller","fallback":"Vehicle Controller"},minecraft:lore=[{"translate":"phys_veh.vehicle_controller.lore","color":"gray","with":[{"keybind":"key.swapOffhand"}]},{"translate":"phys_veh.vehicle_controller.lore2","italic":false,"color":"gray","with":[{"keybind":"key.swapOffhand"}]},{"translate":"phys_veh.vehicle_controller.lore3","italic":false,"color":"gray","with":[{"keybind":"key.swapOffhand"}]}]]

scoreboard players operation #pedal_deltatime main_score = @s phys_veh.pedal_deltatime