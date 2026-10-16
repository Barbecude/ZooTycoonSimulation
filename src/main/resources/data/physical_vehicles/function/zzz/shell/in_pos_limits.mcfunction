execute if score #x main_score < #phys_veh.shell_pos_limit.nx main_score run return 1
execute if score #y main_score < #phys_veh.shell_pos_limit.ny main_score run return 1
execute if score #z main_score < #phys_veh.shell_pos_limit.nz main_score run return 1
execute if score #x main_score > #phys_veh.shell_pos_limit.px main_score run return 1
execute if score #y main_score > #phys_veh.shell_pos_limit.py main_score run return 1
execute if score #z main_score > #phys_veh.shell_pos_limit.pz main_score run return 1
return fail