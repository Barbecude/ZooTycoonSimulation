    #======================#
    #  made by Antohs256M  #
    #======================#
#
#data modify storage midwut:main EntityData set from entity @s


#execute if entity @s[tag=phys_veh.wheel,type=area_effect_cloud] store result entity @s Rotation[0] float 0.001 run scoreboard players get *ang_b main_score
execute if entity @s[tag=phys_veh.transaltion_offset] run function physical_vehicles:zzz/vehicle/at_parts/transaltion_offset
execute if entity @s[tag=!phys_veh.disable.offset] run function physical_vehicles:zzz/vehicle/at_parts/offset
#execute if entity @s[tag=phys_veh.wheel,tag=phys_veh.wheel_f,type=item_display] run function physical_vehicles:zzz/vehicle/at_parts/wheel/rotation_front
#execute if entity @s[tag=phys_veh.wheel,type=item_display] run function physical_vehicles:zzz/vehicle/at_parts/wheel/rotation
execute if entity @s[tag=!phys_veh.disable.rotation,type=llama,predicate=midwut:check/periodic_tick/2] run function physical_vehicles:zzz/vehicle/at_parts/seat_rotation
execute if entity @s[type=minecraft:llama,tag=phys_veh.have_seat_improvements] on passengers if entity @s[type=#midwut:living/all] run function physical_vehicles:zzz/player/improvements/main
execute if entity @s[tag=!phys_veh.disable.rotation,tag=!phys_veh.seat,type=#midwut:not_living/entity_display] run function physical_vehicles:zzz/vehicle/at_parts/rotation
execute if entity @s[tag=phys_veh.model,tag=phys_veh.skin.jeb_] run function physical_vehicles:zzz/vehicle/at_parts/jeb_colors
execute if entity @s[tag=phys_veh.steering_wheel] run function physical_vehicles:zzz/vehicle/at_parts/steering_wheel
execute if entity @s[tag=phys_veh.track] run function physical_vehicles:zzz/vehicle/at_parts/track/main

#execute if score *phys_veh.llama_rot main_score matches 1 if entity @s[tag=phys_veh.sit,tag=!phys_veh.disable.rotation] run function physical_vehicles:zzz/vehicle/at_parts/rotation_sit/main

#execute if entity @s[tag=phys_veh.HP,type=rabbit,nbt=!{Health:1000.0f}] run function physical_vehicles:zzz/vehicle/at_parts/rabbit_hp
#execute if entity @s[tag=phys_veh.coll] run function physical_vehicles:zzz/vehicle/col/test
#execute if entity @s[type=area_effect_cloud] run function physical_vehicles:zzz/vehicle/at_parts/if_aec
#execute if score *phys_veh.llama_data main_score matches 1 if entity @s[tag=phys_veh.sit,type=llama] positioned as @s positioned ~ ~1 ~ if entity @a[limit=1,distance=..2,tag=phys_veh.sit] run function physical_vehicles:zzz/vehicle/hp_sit

#execute if entity @s[tag=phys_veh.objmc.rot-color] run function physical_vehicles:zzz/vehicle/at_parts/objmc_rot-color


