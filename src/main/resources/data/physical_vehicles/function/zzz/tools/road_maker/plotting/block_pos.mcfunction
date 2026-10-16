scoreboard players operation #raycast main_score = #width main_score
$execute positioned 0.0 0.0 0.0 facing ~$(vx) ~$(vy) ~$(vz) facing ^-1 ^ ^ rotated ~ ~$(angle) positioned ~$(x) ~$(y) ~$(z) positioned ^ ^ ^0.5 positioned ^ ^ ^$(w) run function physical_vehicles:zzz/tools/road_maker/plotting/block_raycast
execute if score #make main_score matches 1.. run scoreboard players add #make main_score 1