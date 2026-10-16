execute if items entity @s weapon.offhand *[minecraft:custom_data~{phys_veh.vehicle_controller:true}] run return run item replace entity @s weapon.mainhand with minecraft:air

execute in midwut:main run item replace block 0 0 0 container.0 from entity @s weapon.mainhand
item replace entity @s weapon.mainhand from entity @s weapon.offhand
execute in midwut:main run item replace entity @s weapon.offhand from block 0 0 0 container.0

execute if predicate midwut:check/input/jump positioned as @s as @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1,x=0] run return run function physical_vehicles:zzz/player/driver/hand_brake

execute if entity @s[tag=phys_veh.gear_changing] run return run function physical_vehicles:zzz/player/driver/gear_change/finish with storage midwut:main Data.data.phys_veh.sounds[{id:"switching_gear_finish"}]

function physical_vehicles:zzz/player/driver/gear_change/start with storage midwut:main Data.data.phys_veh.sounds[{id:"switching_gear_start"}]
execute store result score @s phys_veh.selected_slot run data get entity @s SelectedItemSlot
scoreboard players set #gear_changing main_score 1