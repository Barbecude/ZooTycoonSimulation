execute if function physical_vehicles:zzz/midwut_crafting_table/tabs/main_clear run function midwut:y_functions/crafting_table/tab_trigger {tag:"physical_vehicles.main_tab"}
execute if function physical_vehicles:zzz/midwut_crafting_table/tabs/armored_truck_4x4_clear run function midwut:y_functions/crafting_table/tab_trigger {tag:"physical_vehicles.armored_truck_4x4_tab"}
execute if function physical_vehicles:zzz/midwut_crafting_table/tabs/zaz-968m_clear run function midwut:y_functions/crafting_table/tab_trigger {tag:"physical_vehicles.zaz-968m_tab"}
execute if function physical_vehicles:zzz/midwut_crafting_table/tabs/zaz-968a_clear run function midwut:y_functions/crafting_table/tab_trigger {tag:"physical_vehicles.zaz-968a_tab"}

execute if function physical_vehicles:zzz/midwut_crafting_table/items/skin run scoreboard players add @e[type=minecraft:marker,tag=midwut.selected_crafting_table,limit=1,x=0] phys_veh.count_wheels 1
execute if function physical_vehicles:zzz/midwut_crafting_table/items/mode run scoreboard players add @e[type=minecraft:marker,tag=midwut.selected_crafting_table,limit=1,x=0] phys_veh.count_driving_wheels 1

execute if function physical_vehicles:zzz/midwut_crafting_table/items/remover run function physical_vehicles:give/remover
execute if function physical_vehicles:zzz/midwut_crafting_table/items/car_key run function physical_vehicles:give/car_key
execute if function physical_vehicles:zzz/midwut_crafting_table/items/torque_wrench run function physical_vehicles:give/torque_wrench
execute if function physical_vehicles:zzz/midwut_crafting_table/items/armored_truck_4x4_black_clear run function physical_vehicles:give/armored_truck_4x4 {skin:"black"}
execute if function physical_vehicles:zzz/midwut_crafting_table/items/armored_truck_4x4_gray-red_clear run function physical_vehicles:give/armored_truck_4x4 {skin:"gray-red"}
execute if function physical_vehicles:zzz/midwut_crafting_table/items/buhanka run function physical_vehicles:give/buhanka
execute if function physical_vehicles:zzz/midwut_crafting_table/items/ecto-1a run function physical_vehicles:give/ecto-1a
execute if function physical_vehicles:zzz/midwut_crafting_table/items/hammer_monster run function physical_vehicles:give/hammer_monster
execute if function physical_vehicles:zzz/midwut_crafting_table/items/jeep_cj-5_1964 run function physical_vehicles:give/jeep_cj-5_1964
execute if function physical_vehicles:zzz/midwut_crafting_table/items/oldsmobile_cutlass_supreme_coupe_1980 run function physical_vehicles:give/oldsmobile_cutlass_supreme_coupe_1980
execute if function physical_vehicles:zzz/midwut_crafting_table/items/tt-66052 run function physical_vehicles:give/tt-66052
execute if function physical_vehicles:zzz/midwut_crafting_table/items/vanna run function physical_vehicles:give/vanna
execute if function physical_vehicles:zzz/midwut_crafting_table/items/zil-130 run function physical_vehicles:give/zil-130
execute if function physical_vehicles:zzz/midwut_crafting_table/items/zaz-968m run function physical_vehicles:give/zaz-968m with block ~ ~ ~ Items[2].components."minecraft:custom_data".config
execute if function physical_vehicles:zzz/midwut_crafting_table/items/zaz-968a run function physical_vehicles:give/zaz-968a with block ~ ~ ~ Items[2].components."minecraft:custom_data".config