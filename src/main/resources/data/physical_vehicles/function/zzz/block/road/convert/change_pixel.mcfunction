# function physical_vehicles:zzz/block/road/convert/change_pixel {pixel:8}

# black_concrete_powder
# gray_concrete_powder
# light_gray_concrete_powder
# red_concrete_powder
# white_concrete_powder
# yellow_concrete_powder
# iron_trapdoor
# grass_block
# coarse_dirt

$data modify block ~ ~ ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}].components."minecraft:custom_model_data".floats[0] set value $(pixel)f
