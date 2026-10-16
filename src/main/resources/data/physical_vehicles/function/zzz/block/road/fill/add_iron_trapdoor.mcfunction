#function physical_vehicles:zzz/road/fill/add_iron_trapdoor {Slot:1}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.0625} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170500}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.125} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170501}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.1875} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170502}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.25} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170503}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.3125} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170504}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.375} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170505}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.4375} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170506}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.5} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170507}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.5625} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170508}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.625} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170509}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.6875} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170510}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.75} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170511}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.8125} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170512}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.875} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170513}}
$execute unless score #0 const matches $(Slot) if data block ~ ~ ~ Items[0].tag{height:0.9375} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170514}}
$execute unless score #0 const matches $(Slot) run return 1

data modify block ~ ~ ~ Items[0].Slot set value 1b
$execute if data block ~ ~ ~ Items[0].tag{height:0.0625} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170500}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.125} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170501}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.1875} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170502}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.25} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170503}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.3125} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170504}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.375} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170505}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.4375} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170506}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.5} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170507}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.5625} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170508}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.625} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170509}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.6875} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170510}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.75} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170511}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.8125} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170512}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.875} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170513}}
$execute if data block ~ ~ ~ Items[0].tag{height:0.9375} run data modify block ~ ~ ~ Items append value {Slot:$(Slot)b,id:"minecraft:jigsaw",Count:1b,tag:{kill:1,CustomModelData:170514}}
