tag @s add phys_veh.have_transfers
$execute store result score @s phys_veh.current_transfer run data get storage midwut:main Data.data.phys_veh.transmission.transfers[$(current_transfer)].value 10
$scoreboard players set @s phys_veh.current_transfer_index $(current_transfer)
data modify entity @s data.phys_veh.transmission.transfers set value []
scoreboard players set #temp main_score -1
function physical_vehicles:zzz/summon_data/zzzzzz_data/data/transfers_loop

