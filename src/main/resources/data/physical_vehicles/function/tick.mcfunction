# warn-off-file target-selector-no-dimension

    #======================#
    #  made by Antohs256M  #
    #======================#
#
function midwut:marker
#execute as @e[tag=phys_veh.main,type=minecraft:text_display] unless function physical_vehicles:zzz/vehicle/test_afk at @s run function physical_vehicles:zzz/vehicle/tick
execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=!phys_veh.kinetic] at @s if loaded ~ ~ ~ run function physical_vehicles:zzz/vehicle/tick
execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.kinetic] at @s if loaded ~ ~ ~ run function physical_vehicles:zzz/kinetic/tick
execute as @a[predicate=!physical_vehicles:player/driver] if function physical_vehicles:zzz/player/has_vehicle_controller run function physical_vehicles:zzz/player/remove_vehicle_controller
execute as @a[tag=phys_veh.has_seat_improvements,predicate=!physical_vehicles:player/seat_imprs] run function physical_vehicles:zzz/player/dismount_seat/main
execute as @a[tag=phys_veh.seat_improvements_disable] run function physical_vehicles:zzz/player/seat_improvements_disable

#op @a
#function physical_vehicles:zzz/tools/road_maker/plotting/particles

#function physical_vehicles:zzz/tools/smooth_stone_slabs/tick

