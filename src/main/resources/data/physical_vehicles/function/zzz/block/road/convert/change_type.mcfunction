#function physical_vehicles:zzz/road/convert/change_type {type:"black"}

# black_concrete_powder
# gray_concrete_powder
# light_gray_concrete_powder
# red_concrete_powder
# white_concrete_powder
# yellow_concrete_powder
# iron_trapdoor
# grass_block
# coarse_dirt

$data modify block ~ ~ ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}].components."minecraft:custom_model_data".strings[0] set value "$(type)"
