/*
 * Copyright (c) 2021, Jordan Atwood <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.skillcalculator.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Spellbook;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum MagicAction implements SkillAction
{
	WIND_STRIKE("Wind Strike", 1, 5.5f, SpriteID.SPELL_WIND_STRIKE, false, Spellbook.NORMAL),
	CONFUSE("Confuse", 3, 13, SpriteID.SPELL_CONFUSE, false, Spellbook.NORMAL),
	ENCHANT_OPAL_BOLT("Enchant Opal Bolt", 4, 9, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	WATER_STRIKE("Water Strike", 5, 7.5f, SpriteID.SPELL_WATER_STRIKE, false, Spellbook.NORMAL),
	ARCEUUS_LIBRARY_TELEPORT("Arceuus Library Teleport", 6, 10, SpriteID.SPELL_ARCEUUS_LIBRARY_TELEPORT, true, Spellbook.ARCEUUS),
	ENCHANT_SAPPHIRE_BOLT("Enchant Sapphire Bolt", 7, 17.5f, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	ENCHANT_SAPPHIRE_JEWELLERY("Lvl-1 Enchant", 7, 17.5f, SpriteID.SPELL_LVL_1_ENCHANT, false, Spellbook.NORMAL),
	EARTH_STRIKE("Earth Strike", 9, 9.5f, SpriteID.SPELL_EARTH_STRIKE, false, Spellbook.NORMAL),
	WEAKEN("Weaken", 11, 21, SpriteID.SPELL_WEAKEN, false, Spellbook.NORMAL),
	FIRE_STRIKE("Fire Strike", 13, 11.5f, SpriteID.SPELL_FIRE_STRIKE, false, Spellbook.NORMAL),
	ENCHANT_JADE_BOLT("Enchant Jade Bolt", 14, 19, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	BONES_TO_BANANAS("Bones To Bananas", 15, 25, SpriteID.SPELL_BONES_TO_BANANAS, false, Spellbook.NORMAL),
	BASIC_REANIMATION("Basic Reanimation", 16, 32, SpriteID.SPELL_BASIC_REANIMATION, true, Spellbook.ARCEUUS),
	WIND_BOLT("Wind Bolt", 17, 13.5f, SpriteID.SPELL_WIND_BOLT, false, Spellbook.NORMAL),
	DRAYNOR_MANOR_TELEPORT("Draynor Manor Teleport", 17, 16, SpriteID.SPELL_DRAYNOR_MANOR_TELEPORT, true, Spellbook.ARCEUUS),
	BATTLEFRONT_TELEPORT("Battlefront Teleport", 23, 19, 1255, true, Spellbook.ARCEUUS),
	CURSE("Curse", 19, 29, SpriteID.SPELL_CURSE, false, Spellbook.NORMAL),
	BIND("Bind", 20, 30, SpriteID.SPELL_BIND, false, Spellbook.NORMAL),
	LOW_LEVEL_ALCHEMY("Low Level Alchemy", 21, 31, SpriteID.SPELL_LOW_LEVEL_ALCHEMY, false, Spellbook.NORMAL),
	WATER_BOLT("Water Bolt", 23, 16.5f, SpriteID.SPELL_WATER_BOLT, false, Spellbook.NORMAL),
	ENCHANT_PEARL_BOLT("Enchant Pearl Bolt", 24, 29, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	VARROCK_TELEPORT("Varrock Teleport", 25, 35, SpriteID.SPELL_VARROCK_TELEPORT, false, Spellbook.NORMAL),
	ENCHANT_EMERALD_BOLT("Enchant Emerald Bolt", 27, 37, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	ENCHANT_EMERALD_JEWELLERY("Lvl-2 Enchant", 27, 37, SpriteID.SPELL_LVL_2_ENCHANT, false, Spellbook.NORMAL),
	MIND_ALTAR_TELEPORT("Mind Altar Teleport", 28, 22, SpriteID.SPELL_MIND_ALTAR_TELEPORT, true, Spellbook.ARCEUUS),
	EARTH_BOLT("Earth Bolt", 29, 19.5f, SpriteID.SPELL_EARTH_BOLT, false, Spellbook.NORMAL),
	ENCHANT_TOPAZ_BOLT("Enchant Topaz Bolt", 29, 33, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	LUMBRIDGE_TELEPORT("Lumbridge Teleport", 31, 41, SpriteID.SPELL_LUMBRIDGE_TELEPORT, false, Spellbook.NORMAL),
	TELEKINETIC_GRAB("Telekinetic Grab", 33, 43, SpriteID.SPELL_TELEKINETIC_GRAB, false, Spellbook.NORMAL),
	RESPAWN_TELEPORT("Respawn Teleport", 34, 27, SpriteID.SPELL_RESPAWN_TELEPORT, true, Spellbook.ARCEUUS),
	FIRE_BOLT("Fire Bolt", 35, 22.5f, SpriteID.SPELL_FIRE_BOLT, false, Spellbook.NORMAL),
	GHOSTLY_GRASP("Ghostly Grasp", 35, 22.5f, SpriteID.SPELL_GHOSTLY_GRASP, true, Spellbook.ARCEUUS),
	FALADOR_TELEPORT("Falador Teleport", 37, 48, SpriteID.SPELL_FALADOR_TELEPORT, false, Spellbook.NORMAL),
	RESURRECT_LESSER_THRALL("Resurrect Lesser Thrall", 38, 55, SpriteID.SPELL_RESURRECT_LESSER_GHOST, true, Spellbook.ARCEUUS),
	CRUMBLE_UNDEAD("Crumble Undead", 39, 24.5f, SpriteID.SPELL_CRUMBLE_UNDEAD, false, Spellbook.NORMAL),
	SALVE_GRAVEYARD_TELEPORT("Salve Graveyard Teleport", 40, 30, SpriteID.SPELL_SALVE_GRAVEYARD_TELEPORT, true, Spellbook.ARCEUUS),
	TELEPORT_TO_HOUSE("Teleport To House", 40, 30, SpriteID.SPELL_TELEPORT_TO_HOUSE, true, Spellbook.NORMAL),
	WIND_BLAST("Wind Blast", 41, 25.5f, SpriteID.SPELL_WIND_BLAST, false, Spellbook.NORMAL),
	ADEPT_REANIMATION("Adept Reanimation", 41, 80, SpriteID.SPELL_ADEPT_REANIMATION, true, Spellbook.ARCEUUS),
	SUPERHEAT_ITEM("Superheat Item", 43, 53, SpriteID.SPELL_SUPERHEAT_ITEM, false, Spellbook.NORMAL),
	INFERIOR_DEMONBANE("Inferior Demonbane", 44, 27, SpriteID.SPELL_INFERIOR_DEMONBANE, true, Spellbook.ARCEUUS),
	CAMELOT_TELEPORT("Camelot Teleport", 45, 55.5f, SpriteID.SPELL_CAMELOT_TELEPORT, true, Spellbook.NORMAL),
	WATER_BLAST("Water Blast", 47, 28.5f, SpriteID.SPELL_WATER_BLAST, false, Spellbook.NORMAL),
	SHADOW_VEIL("Shadow Veil", 47, 58, SpriteID.SPELL_SHADOW_VEIL, true, Spellbook.ARCEUUS),
	FENKENSTRAINS_CASTLE_TELEPORT("Fenkenstrain's Castle Teleport", 48, 50, SpriteID.SPELL_FENKENSTRAINS_CASTLE_TELEPORT, true, Spellbook.ARCEUUS),
	KOUREND_CASTLE_TELEPORT("Kourend Castle Teleport", 48, 58, SpriteID.SPELL_TELEPORT_TO_KOUREND, true, Spellbook.NORMAL),
	ENCHANT_RUBY_BOLT("Enchant Ruby Bolt", 49, 59, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	ENCHANT_RUBY_JEWELLERY("Lvl-3 Enchant", 49, 59, SpriteID.SPELL_LVL_3_ENCHANT, false, Spellbook.NORMAL),
	IBAN_BLAST("Iban Blast", 50, 30, SpriteID.SPELL_IBAN_BLAST, true, Spellbook.NORMAL),
	MAGIC_DART("Magic Dart", 50, 30, SpriteID.SPELL_MAGIC_DART, true, Spellbook.NORMAL),
	SMOKE_RUSH("Smoke Rush", 50, 30, SpriteID.SPELL_SMOKE_RUSH, true, Spellbook.ANCIENT),
	DARK_LURE("Dark Lure", 50, 60, SpriteID.SPELL_DARK_LURE, true, Spellbook.ARCEUUS),
	SNARE("Snare", 50, 60, SpriteID.SPELL_SNARE, false, Spellbook.NORMAL),
	ARDOUGNE_TELEPORT("Ardougne Teleport", 51, 61, SpriteID.SPELL_ARDOUGNE_TELEPORT, true, Spellbook.NORMAL),
	SHADOW_RUSH("Shadow Rush", 52, 31, SpriteID.SPELL_SHADOW_RUSH, true, Spellbook.ANCIENT),
	EARTH_BLAST("Earth Blast", 53, 31.5f, SpriteID.SPELL_EARTH_BLAST, false, Spellbook.NORMAL),
	CIVITAS_ILLA_FORTIS_TELEPORT("Civitas illa Fortis Teleport", 54, 64, SpriteID.SPELL_CIVITAS_ILLA_FORTIS_TELEPORT, true, Spellbook.NORMAL),
	PADDEWWA_TELEPORT("Paddewwa Teleport", 54, 64, SpriteID.SPELL_PADDEWWA_TELEPORT, true, Spellbook.ANCIENT),
	HIGH_LEVEL_ALCHEMY("High Level Alchemy", 55, 65, SpriteID.SPELL_HIGH_LEVEL_ALCHEMY, false, Spellbook.NORMAL),
	BLOOD_RUSH("Blood Rush", 56, 33, SpriteID.SPELL_BLOOD_RUSH, true, Spellbook.ANCIENT),
	SKELETAL_GRASP("Skeletal Grasp", 56, 33, SpriteID.SPELL_SKELETAL_GRASP, true, Spellbook.ARCEUUS),
	CHARGE_WATER_ORB("Charge Water Orb", 56, 66, SpriteID.SPELL_CHARGE_WATER_ORB, true, Spellbook.NORMAL),
	ENCHANT_DIAMOND_BOLT("Enchant Diamond Bolt", 57, 67, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	ENCHANT_DIAMOND_JEWELLERY("Lvl-4 Enchant", 57, 67, SpriteID.SPELL_LVL_4_ENCHANT, false, Spellbook.NORMAL),
	RESURRECT_SUPERIOR_THRALL("Resurrect Superior Thrall", 57, 70, SpriteID.SPELL_RESURRECT_SUPERIOR_SKELETON, true, Spellbook.ARCEUUS),
	ICE_RUSH("Ice Rush", 58, 34, SpriteID.SPELL_ICE_RUSH, true, Spellbook.ANCIENT),
	WATCHTOWER_TELEPORT("Watchtower Teleport", 58, 68, SpriteID.SPELL_WATCHTOWER_TELEPORT, true, Spellbook.NORMAL),
	FIRE_BLAST("Fire Blast", 59, 34.5f, SpriteID.SPELL_FIRE_BLAST, false, Spellbook.NORMAL),
	MARK_OF_DARKNESS("Mark of Darkness", 59, 70, SpriteID.SPELL_MARK_OF_DARKNESS, true, Spellbook.ARCEUUS),
	CLAWS_OF_GUTHIX("Claws Of Guthix", 60, 35, SpriteID.SPELL_CLAWS_OF_GUTHIX, true, Spellbook.NORMAL),
	FLAMES_OF_ZAMORAK("Flames Of Zamorak", 60, 35, SpriteID.SPELL_FLAMES_OF_ZAMORAK, true, Spellbook.NORMAL),
	SARADOMIN_STRIKE("Saradomin Strike", 60, 35, SpriteID.SPELL_SARADOMIN_STRIKE, true, Spellbook.NORMAL),
	BONES_TO_PEACHES("Bones To Peaches", 60, 35.5f, SpriteID.SPELL_BONES_TO_PEACHES, true, Spellbook.NORMAL),
	CHARGE_EARTH_ORB("Charge Earth Orb", 60, 70, SpriteID.SPELL_CHARGE_EARTH_ORB, true, Spellbook.NORMAL),
	SENNTISTEN_TELEPORT("Senntisten Teleport", 60, 70, SpriteID.SPELL_SENNTISTEN_TELEPORT, true, Spellbook.ANCIENT),
	TROLLHEIM_TELEPORT("Trollheim Teleport", 61, 68, SpriteID.SPELL_TROLLHEIM_TELEPORT, true, Spellbook.NORMAL),
	WEST_ARDOUGNE_TELEPORT("West Ardougne Teleport", 61, 68, SpriteID.SPELL_WEST_ARDOUGNE_TELEPORT, true, Spellbook.ARCEUUS),
	SMOKE_BURST("Smoke Burst", 62, 36, SpriteID.SPELL_SMOKE_BURST, true, Spellbook.ANCIENT),
	SUPERIOR_DEMONBANE("Superior Demonbane", 62, 36, SpriteID.SPELL_SUPERIOR_DEMONBANE, true, Spellbook.ARCEUUS),
	WIND_WAVE("Wind Wave", 62, 36, SpriteID.SPELL_WIND_WAVE, true, Spellbook.NORMAL),
	CHARGE_FIRE_ORB("Charge Fire Orb", 63, 73, SpriteID.SPELL_CHARGE_FIRE_ORB, true, Spellbook.NORMAL),
	SHADOW_BURST("Shadow Burst", 64, 37, SpriteID.SPELL_SHADOW_BURST, true, Spellbook.ANCIENT),
	TELEPORT_APE_ATOLL("Teleport Ape Atoll", 64, 74, SpriteID.SPELL_TELEPORT_TO_APE_ATOLL, true, Spellbook.NORMAL),
	LESSER_CORRUPTION("Lesser Corruption", 64, 75, SpriteID.SPELL_LESSER_CORRUPTION, true, Spellbook.ARCEUUS),
	WATER_WAVE("Water Wave", 65, 37.5f, SpriteID.SPELL_WATER_WAVE, true, Spellbook.NORMAL),
	BAKE_PIE("Bake Pie", 65, 60, SpriteID.SPELL_BAKE_PIE, true, Spellbook.LUNAR),
	GEOMANCY("Geomancy", 65, 60, SpriteID.SPELL_GEOMANCY, true, Spellbook.LUNAR),
	HARMONY_ISLAND_TELEPORT("Harmony Island Teleport", 65, 74, SpriteID.SPELL_HARMONY_ISLAND_TELEPORT, true, Spellbook.ARCEUUS),
	CURE_PLANT("Cure Plant", 66, 60, SpriteID.SPELL_CURE_PLANT, true, Spellbook.LUNAR),
	MONSTER_EXAMINE("Monster Examine", 66, 61, SpriteID.SPELL_MONSTER_EXAMINE, true, Spellbook.LUNAR),
	CHARGE_AIR_ORB("Charge Air Orb", 66, 76, SpriteID.SPELL_CHARGE_AIR_ORB, true, Spellbook.NORMAL),
	KHARYRLL_TELEPORT("Kharyrll Teleport", 66, 76, SpriteID.SPELL_KHARYRLL_TELEPORT, true, Spellbook.ANCIENT),
	VILE_VIGOUR("Vile Vigour", 66, 76, SpriteID.SPELL_VILE_VIGOUR, true, Spellbook.ARCEUUS),
	VULNERABILITY("Vulnerability", 66, 76, SpriteID.SPELL_VULNERABILITY, true, Spellbook.NORMAL),
	NPC_CONTACT("Npc Contact", 67, 63, SpriteID.SPELL_NPC_CONTACT, true, Spellbook.LUNAR),
	BLOOD_BURST("Blood Burst", 68, 39, SpriteID.SPELL_BLOOD_BURST, true, Spellbook.ANCIENT),
	CURE_OTHER("Cure Other", 68, 65, SpriteID.SPELL_CURE_OTHER, true, Spellbook.LUNAR),
	HUMIDIFY("Humidify", 68, 65, SpriteID.SPELL_HUMIDIFY, true, Spellbook.LUNAR),
	ENCHANT_DRAGONSTONE_BOLT("Enchant Dragonstone Bolt", 68, 78, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	ENCHANT_DRAGONSTONE_JEWELLERY("Lvl-5 Enchant", 68, 78, SpriteID.SPELL_LVL_5_ENCHANT, true, Spellbook.NORMAL),
	MOONCLAN_TELEPORT("Moonclan Teleport", 69, 66, SpriteID.SPELL_MOONCLAN_TELEPORT, true, Spellbook.LUNAR),
	EARTH_WAVE("Earth Wave", 70, 40, SpriteID.SPELL_EARTH_WAVE, true, Spellbook.NORMAL),
	ICE_BURST("Ice Burst", 70, 40, SpriteID.SPELL_ICE_BURST, true, Spellbook.ANCIENT),
	TELE_GROUP_MOONCLAN("Tele Group Moonclan", 70, 67, SpriteID.SPELL_TELE_GROUP_MOONCLAN, true, Spellbook.LUNAR),
	DEGRIME("Degrime", 70, 83, SpriteID.SPELL_DEGRIME, true, Spellbook.ARCEUUS),
	CURE_ME("Cure Me", 71, 69, SpriteID.SPELL_CURE_ME, true, Spellbook.LUNAR),
	OURANIA_TELEPORT("Ourania Teleport", 71, 69, SpriteID.SPELL_OURANIA_TELEPORT, true, Spellbook.LUNAR),
	HUNTER_KIT("Hunter Kit", 71, 70, SpriteID.SPELL_HUNTER_KIT, true, Spellbook.LUNAR),
	CEMETERY_TELEPORT("Cemetery Teleport", 71, 82, SpriteID.SPELL_CEMETERY_TELEPORT, true, Spellbook.ARCEUUS),
	WATERBIRTH_TELEPORT("Waterbirth Teleport", 72, 71, SpriteID.SPELL_WATERBIRTH_TELEPORT, true, Spellbook.LUNAR),
	LASSAR_TELEPORT("Lassar Teleport", 72, 82, SpriteID.SPELL_LASSAR_TELEPORT, true, Spellbook.ANCIENT),
	EXPERT_REANIMATION("Expert Reanimation", 72, 138, SpriteID.SPELL_EXPERT_REANIMATION, true, Spellbook.ARCEUUS),
	TELE_GROUP_WATERBIRTH("Tele Group Waterbirth", 73, 72, SpriteID.SPELL_TELE_GROUP_WATERBIRTH, true, Spellbook.LUNAR),
	ENFEEBLE("Enfeeble", 73, 83, SpriteID.SPELL_ENFEEBLE, true, Spellbook.NORMAL),
	WARD_OF_ARCEUUS("Ward of Arceuus", 73, 83, SpriteID.SPELL_WARD_OF_ARCEUUS, true, Spellbook.ARCEUUS),
	SMOKE_BLITZ("Smoke Blitz", 74, 42, SpriteID.SPELL_SMOKE_BLITZ, true, Spellbook.ANCIENT),
	CURE_GROUP("Cure Group", 74, 74, SpriteID.SPELL_CURE_GROUP, true, Spellbook.LUNAR),
	TELEOTHER_LUMBRIDGE("Teleother Lumbridge", 74, 84, SpriteID.SPELL_TELEOTHER_LUMBRIDGE, true, Spellbook.NORMAL),
	FIRE_WAVE("Fire Wave", 75, 42.5f, SpriteID.SPELL_FIRE_WAVE, true, Spellbook.NORMAL),
	BARBARIAN_TELEPORT("Barbarian Teleport", 75, 76, SpriteID.SPELL_BARBARIAN_TELEPORT, true, Spellbook.LUNAR),
	STAT_SPY("Stat Spy", 75, 76, SpriteID.SPELL_STAT_SPY, true, Spellbook.LUNAR),
	SHADOW_BLITZ("Shadow Blitz", 76, 43, SpriteID.SPELL_SHADOW_BLITZ, true, Spellbook.ANCIENT),
	SPIN_FLAX("Spin Flax", 76, 75, SpriteID.SPELL_SPIN_FLAX, true, Spellbook.LUNAR),
	TELE_GROUP_BARBARIAN("Tele Group Barbarian", 76, 77, SpriteID.SPELL_TELE_GROUP_ICE_PLATEAU, true, Spellbook.LUNAR),
	SUPERGLASS_MAKE("Superglass Make", 77, 78, SpriteID.SPELL_SUPERGLASS_MAKE, true, Spellbook.LUNAR),
	KHAZARD_TELEPORT("Khazard Teleport", 78, 80, SpriteID.SPELL_KHAZARD_TELEPORT, true, Spellbook.LUNAR),
	TAN_LEATHER("Tan Leather", 78, 81, SpriteID.SPELL_TAN_LEATHER, true, Spellbook.LUNAR),
	DAREEYAK_TELEPORT("Dareeyak Teleport", 78, 88, SpriteID.SPELL_DAREEYAK_TELEPORT, true, Spellbook.ANCIENT),
	RESURRECT_CROPS("Resurrect Crops", 78, 90, SpriteID.SPELL_RESURRECT_CROPS, true, Spellbook.ARCEUUS),
	UNDEAD_GRASP("Undead Grasp", 79, 46.5f, SpriteID.SPELL_UNDEAD_GRASP, true, Spellbook.ARCEUUS),
	TELE_GROUP_KHAZARD("Tele Group Khazard", 79, 81, SpriteID.SPELL_TELE_GROUP_KHAZARD, true, Spellbook.LUNAR),
	DREAM("Dream", 79, 82, SpriteID.SPELL_DREAM, true, Spellbook.LUNAR),
	ENTANGLE("Entangle", 79, 89, SpriteID.SPELL_ENTANGLE, true, Spellbook.NORMAL),
	BLOOD_BLITZ("Blood Blitz", 80, 45, SpriteID.SPELL_BLOOD_BLITZ, true, Spellbook.ANCIENT),
	STRING_JEWELLERY("String Jewellery", 80, 83, SpriteID.SPELL_STRING_JEWELLERY, true, Spellbook.LUNAR),
	DEATH_CHARGE("Death Charge", 80, 90, SpriteID.SPELL_DEATH_CHARGE, true, Spellbook.ARCEUUS),
	STUN("Stun", 80, 90, SpriteID.SPELL_STUN, true, Spellbook.NORMAL),
	CHARGE("Charge", 80, 180, SpriteID.SPELL_CHARGE, true, Spellbook.NORMAL),
	WIND_SURGE("Wind Surge", 81, 44.5f, SpriteID.SPELL_WIND_SURGE, true, Spellbook.NORMAL),
	STAT_RESTORE_POT_SHARE("Stat Restore Pot Share", 81, 84, SpriteID.SPELL_STAT_RESTORE_POT_SHARE, true, Spellbook.LUNAR),
	DARK_DEMONBANE("Dark Demonbane", 82, 43.5f, SpriteID.SPELL_DARK_DEMONBANE, true, Spellbook.ARCEUUS),
	ICE_BLITZ("Ice Blitz", 82, 46, SpriteID.SPELL_ICE_BLITZ, true, Spellbook.ANCIENT),
	MAGIC_IMBUE("Magic Imbue", 82, 86, SpriteID.SPELL_MAGIC_IMBUE, true, Spellbook.LUNAR),
	TELEOTHER_FALADOR("Teleother Falador", 82, 92, SpriteID.SPELL_TELEOTHER_FALADOR, true, Spellbook.NORMAL),
	FERTILE_SOIL("Fertile Soil", 83, 87, SpriteID.SPELL_FERTILE_SOIL, true, Spellbook.LUNAR),
	BARROWS_TELEPORT("Barrows Teleport", 83, 90, SpriteID.SPELL_BARROWS_TELEPORT, true, Spellbook.ARCEUUS),
	CARRALLANGER_TELEPORT("Carrallanger Teleport", 84, 82, SpriteID.SPELL_CARRALLANGAR_TELEPORT, true, Spellbook.ANCIENT),
	BOOST_POTION_SHARE("Boost Potion Share", 84, 88, SpriteID.SPELL_BOOST_POTION_SHARE, true, Spellbook.LUNAR),
	DEMONIC_OFFERING("Demonic Offering", 84, 175, SpriteID.SPELL_DEMONIC_OFFERING, true, Spellbook.ARCEUUS),
	TELEPORT_TO_TARGET("Teleport To Target", 85, 45, SpriteID.SPELL_TELEPORT_TO_BOUNTY_TARGET, true, Spellbook.NORMAL),
	WATER_SURGE("Water Surge", 85, 46.5f, SpriteID.SPELL_WATER_SURGE, true, Spellbook.NORMAL),
	TELE_BLOCK("Tele Block", 85, 80, SpriteID.SPELL_TELE_BLOCK, false, Spellbook.NORMAL),
	FISHING_GUILD_TELEPORT("Fishing Guild Teleport", 85, 89, SpriteID.SPELL_FISHING_GUILD_TELEPORT, true, Spellbook.LUNAR),
	GREATER_CORRUPTION("Greater Corruption", 85, 95, SpriteID.SPELL_GREATER_CORRUPTION, true, Spellbook.ARCEUUS),
	SMOKE_BARRAGE("Smoke Barrage", 86, 48, SpriteID.SPELL_SMOKE_BARRAGE, true, Spellbook.ANCIENT),
	PLANK_MAKE("Plank Make", 86, 90, SpriteID.SPELL_PLANK_MAKE, true, Spellbook.LUNAR),
	TELE_GROUP_FISHING_GUILD("Tele Group Fishing Guild", 86, 90, SpriteID.SPELL_TELE_GROUP_FISHING_GUILD, true, Spellbook.LUNAR),
	CATHERBY_TELEPORT("Catherby Teleport", 87, 92, SpriteID.SPELL_CATHERBY_TELEPORT, true, Spellbook.LUNAR),
	ENCHANT_ONYX_BOLT("Enchant Onyx Bolt", 87, 97, SpriteID.SPELL_ENCHANT_CROSSBOW_BOLT, true, Spellbook.NORMAL),
	ENCHANT_ONYX_JEWELLERY("Lvl-6 Enchant", 87, 97, SpriteID.SPELL_LVL_6_ENCHANT, true, Spellbook.NORMAL),
	SHADOW_BARRAGE("Shadow Barrage", 88, 48, SpriteID.SPELL_SHADOW_BARRAGE, true, Spellbook.ANCIENT),
	TELE_GROUP_CATHERBY("Tele Group Catherby", 88, 93, SpriteID.SPELL_TELE_GROUP_CATHERBY, true, Spellbook.LUNAR),
	ICE_PLATEAU_TELEPORT("Ice Plateau Teleport", 89, 96, SpriteID.SPELL_ICE_PLATEAU_TELEPORT, true, Spellbook.LUNAR),
	RECHARGE_DRAGONSTONE("Recharge Dragonstone", 89, 97.5f, SpriteID.SPELL_RECHARGE_DRAGONSTONE, true, Spellbook.LUNAR),
	EARTH_SURGE("Earth Surge", 90, 48.5f, SpriteID.SPELL_EARTH_SURGE, true, Spellbook.NORMAL),
	TELE_GROUP_ICE_PLATEAU("Tele Group Ice Plateau", 90, 99, SpriteID.SPELL_TELE_GROUP_ICE_PLATEAU, true, Spellbook.LUNAR),
	ANNAKARL_TELEPORT("Annakarl Teleport", 90, 100, SpriteID.SPELL_ANNAKARL_TELEPORT, true, Spellbook.ANCIENT),
	APE_ATOLL_TELEPORT("Ape Atoll Teleport", 90, 100, SpriteID.SPELL_APE_ATOLL_TELEPORT, true, Spellbook.ARCEUUS),
	TELEOTHER_CAMELOT("Teleother Camelot", 90, 100, SpriteID.SPELL_TELEOTHER_CAMELOT, true, Spellbook.NORMAL),
	MASTER_REANIMATION("Master Reanimation", 90, 170, SpriteID.SPELL_MASTER_REANIMATION, true, Spellbook.ARCEUUS),
	ENERGY_TRANSFER("Energy Transfer", 91, 100, SpriteID.SPELL_ENERGY_TRANSFER, true, Spellbook.LUNAR),
	BLOOD_BARRAGE("Blood Barrage", 92, 51, SpriteID.SPELL_BLOOD_BARRAGE, true, Spellbook.ANCIENT),
	HEAL_OTHER("Heal Other", 92, 101, SpriteID.SPELL_HEAL_OTHER, true, Spellbook.LUNAR),
	SINISTER_OFFERING("Sinister Offering", 92, 180, SpriteID.SPELL_SINISTER_OFFERING, true, Spellbook.ARCEUUS),
	VENGEANCE_OTHER("Vengeance Other", 93, 108, SpriteID.SPELL_VENGEANCE_OTHER, true, Spellbook.LUNAR),
	ENCHANT_ZENYTE_JEWELLERY("Lvl-7 Enchant", 93, 110, SpriteID.SPELL_LVL_7_ENCHANT, true, Spellbook.NORMAL),
	ICE_BARRAGE("Ice Barrage", 94, 52, SpriteID.SPELL_ICE_BARRAGE, true, Spellbook.ANCIENT),
	VENGEANCE("Vengeance", 94, 112, SpriteID.SPELL_VENGEANCE, true, Spellbook.LUNAR),
	FIRE_SURGE("Fire Surge", 95, 50.5f, SpriteID.SPELL_FIRE_SURGE, true, Spellbook.NORMAL),
	HEAL_GROUP("Heal Group", 95, 124, SpriteID.SPELL_HEAL_GROUP, true, Spellbook.LUNAR),
	GHORROCK_TELEPORT("Ghorrock Teleport", 96, 106, SpriteID.SPELL_GHORROCK_TELEPORT, true, Spellbook.ANCIENT),
	SPELLBOOK_SWAP("Spellbook Swap", 96, 130, SpriteID.SPELL_SPELLBOOK_SWAP, true, Spellbook.LUNAR),
	RESURRECT_LESSER_GHOST("Resurrect Lesser Ghost", 38, 55, 1270, true, Spellbook.ARCEUUS),
	RESURRECT_LESSER_SKELETON("Resurrect Lesser Skeleton", 38, 55, 1271, true, Spellbook.ARCEUUS),
	RESURRECT_LESSER_ZOMBIE("Resurrect Lesser Zombie", 38, 55, 1300, true, Spellbook.ARCEUUS),
	RESURRECT_SUPERIOR_GHOST("Resurrect Superior Ghost", 57, 70, 2979, true, Spellbook.ARCEUUS),
	RESURRECT_SUPERIOR_SKELETON("Resurrect Superior Skeleton", 57, 70, 2981, true, Spellbook.ARCEUUS),
	RESURRECT_SUPERIOR_ZOMBIE("Resurrect Superior Zombie", 57, 70, 2983, true, Spellbook.ARCEUUS),
	RESURRECT_GREATER_GHOST("Resurrect Greater Ghost", 76, 88, 2980, true, Spellbook.ARCEUUS),
	RESURRECT_GREATER_SKELETON("Resurrect Greater Skeleton", 76, 88, 2982, true, Spellbook.ARCEUUS),
	RESURRECT_GREATER_ZOMBIE("Resurrect Greater Zombie", 76, 88, SpriteID.SPELL_RESURRECT_GREATER_ZOMBIE, true, Spellbook.ARCEUUS),
	;

	private final String name;
	private final int level;
	private final float xp;
	private final int sprite;
	private final boolean isMembers;
	private final Spellbook spellbook;

	@Override
	public String getName(final ItemManager itemManager)
	{
		return getName();
	}

	@Override
	public boolean isMembers(final ItemManager itemManager)
	{
		return isMembers();
	}

	public int getWidgetId() {
		return Rs2Widget.findWidget(name, Arrays.stream(Rs2Widget.getWidget(218, 0).getStaticChildren()).collect(Collectors.toList())).getId();
	}

	public String[] getActions() {
		return Rs2Widget.findWidget(name, Arrays.stream(Rs2Widget.getWidget(218, 0).getStaticChildren()).collect(Collectors.toList())).getActions();
	}
}
