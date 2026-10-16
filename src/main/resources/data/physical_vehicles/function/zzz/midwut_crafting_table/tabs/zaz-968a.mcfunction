data modify storage midwut:main Tabs set value [\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.choosing_skin",\
        fallback: "Choosing a Skin"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.skin.tint",\
          fallback: "tint",\
          italic: false,\
          color: "white"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.skin: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.choosing_mode",\
        fallback: "Choosing a Mode"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.mode.stock",\
          fallback: "Stock",\
          italic: false,\
          color: "white"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.mode: true,\
        config: {skin: "tint", mode: "stock"}\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        strings: ["tint"],\
        colors: [16777215]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.zaz-968a",\
        fallback: "ЗАЗ-968А"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Falanta Fantomo",\
              color: "#FEBE04"\
            }\
          ]\
        },\
        {\
          translate: "options.generic_value",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              translate: "phys_veh.skin",\
              fallback: "Skin"\
            },\
            {\
              translate: "phys_veh.skin.tint",\
              fallback: "Tint"\
            }\
          ]\
        },\
        {\
          translate: "options.generic_value",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              translate: "phys_veh.mode",\
              fallback: "Mode"\
            },\
            {\
              translate: "phys_veh.mode.stock",\
              fallback: "Stock"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.zaz-968a: true\
      },\
      "!minecraft:consumable": {}\
    }\
  }\
]

execute store result score #skin_count main_score if data storage midwut:phys_veh skins.physical_vehicles:zaz-968a[]
execute store result score #mode_count main_score if data storage midwut:phys_veh modes.physical_vehicles:zaz-968a[]

scoreboard players operation @s phys_veh.count_wheels %= #skin_count main_score
scoreboard players operation @s phys_veh.count_driving_wheels %= #mode_count main_score

execute if score @s phys_veh.count_wheels matches 1 run data modify storage midwut:main temp set value {translate: "phys_veh.zaz-968a.skin.race_01", fallback: "race_01", id: "race_01", model: "race_01"}
execute if score @s phys_veh.count_wheels matches 2 run data modify storage midwut:main temp set value {translate: "phys_veh.zaz-968a.skin.race_02", fallback: "race_02", id: "race_02", model: "race_02"}
execute unless score @s phys_veh.count_wheels matches 0 run data modify storage midwut:main Tabs[0].components."minecraft:lore"[0] merge from storage midwut:main temp
execute unless score @s phys_veh.count_wheels matches 0 run data modify storage midwut:main Tabs[2].components."minecraft:lore"[1].with[1] merge from storage midwut:main temp
execute unless score @s phys_veh.count_wheels matches 0 run data modify storage midwut:main Tabs[2].components."minecraft:custom_model_data".strings[0] set from storage midwut:main temp.model
execute unless score @s phys_veh.count_wheels matches 0 run data modify storage midwut:main Tabs[1].components."minecraft:custom_data".config.skin set from storage midwut:main temp.id

execute if score @s phys_veh.count_driving_wheels matches 1 run data modify storage midwut:main temp set value {translate: "phys_veh.mode.race", fallback: "Race", id: "race"}
execute if score @s phys_veh.count_driving_wheels matches 2 run data modify storage midwut:main temp set value {translate: "phys_veh.mode.drift", fallback: "Drift", id: "drift"}
execute unless score @s phys_veh.count_driving_wheels matches 0 run data modify storage midwut:main Tabs[1].components."minecraft:lore"[0] merge from storage midwut:main temp
execute unless score @s phys_veh.count_driving_wheels matches 0 run data modify storage midwut:main Tabs[2].components."minecraft:lore"[2].with[1] merge from storage midwut:main temp
execute unless score @s phys_veh.count_driving_wheels matches 0 run data modify storage midwut:main Tabs[1].components."minecraft:custom_data".config.mode set from storage midwut:main temp.id

