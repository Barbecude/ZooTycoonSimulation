scoreboard players operation #vsl.x main_score = #vl.x main_score
scoreboard players operation #vsl.y main_score = #vl.y main_score
scoreboard players operation #vsl.z main_score = #vl.z main_score

scoreboard players operation #vsf.x main_score = #vf.x main_score
scoreboard players operation #vsf.y main_score = #vf.y main_score
scoreboard players operation #vsf.z main_score = #vf.z main_score

function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/vsl
function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/vsf

scoreboard players operation #vsl.x main_score += #vsl.y main_score
scoreboard players operation #vsl.x main_score += #vsl.z main_score

scoreboard players operation #vsf.x main_score += #vsf.y main_score
scoreboard players operation #vsf.x main_score += #vsf.z main_score
