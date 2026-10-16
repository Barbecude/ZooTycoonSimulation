$execute unless data storage midwut:phys_veh skins.physical_vehicles:armored_truck_4x4[{id:"$(skin)"}] run function physical_vehicles:zzz/summon_data/non-existent_skin_id {storage_id:"midwut:phys_veh",vehicle_id:"physical_vehicles:armored_truck_4x4",skin:"$(skin)"}
$execute if data storage midwut:phys_veh skins.physical_vehicles:armored_truck_4x4[{id:"$(skin)"}] run function physical_vehicles:zzz/summon_data/loot_give_skin {path:"physical_vehicles:give/armored_truck_4x4/",skin:"$(skin)"}

