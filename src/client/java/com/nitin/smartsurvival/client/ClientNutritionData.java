package com.nitin.smartsurvival.client;

import com.nitin.smartsurvival.nutrition.FoodGroup;
import java.util.EnumMap;
import java.util.Map;

/**
 * Client-side cached nutrition data for the local player.
 * Synced from server via network packets.
 */
public class ClientNutritionData {
    private static final Map<FoodGroup, Integer> NUTRITION_VALUES = new EnumMap<>(FoodGroup.class);

    static {
        // Initialize all groups to 0
        for (FoodGroup group : FoodGroup.values()) {
            if (group.isValid()) {
                NUTRITION_VALUES.put(group, 0);
            }
        }
    }

    /**
     * Get the nutrition value for a food group.
     * @param group the food group
     * @return the nutrition value (0-100)
     */
    public static int get(FoodGroup group) {
        return NUTRITION_VALUES.getOrDefault(group, 0);
    }

    /**
     * Set the nutrition value for a food group.
     * @param group the food group
     * @param value the value (0-100)
     */
    public static void set(FoodGroup group, int value) {
        NUTRITION_VALUES.put(group, Math.max(0, Math.min(100, value)));
    }

    /**
     * Update all nutrition values at once.
     * @param protein protein value
     * @param fiber fiber value
     * @param sugar sugar value
     * @param fat fat value
     */
    public static void updateAll(int protein, int fiber, int sugar, int fat) {
        set(FoodGroup.PROTEIN, protein);
        set(FoodGroup.FIBER, fiber);
        set(FoodGroup.SUGAR, sugar);
        set(FoodGroup.FAT, fat);
    }

    /**
     * Reset all values to 0 asdasf edffsdf  ef sdvsvvhf sdefergsdd efrsdfrgsd rg gsdggdf regsdsfgrtsg sgr gdsgrs sdfgrrsgfrs srgssdgrgs regserg 22rg r2g 13re
     * 11g11 tg 331113r
     * 21511563t131511
     *  rtgtyhdsgth 232
     *  22
     *  2
     *  22265222
     *
     *  531153
     *  15331
     *  35135313151
     *  2313553135
     *  1253554011
     *  151422212
     *  155424
     *  vfklnklbgd
     *  bkktgsd;bsns
     *  kdhgngxfm;n;nhfd'lk;h
     *  dhkm;fhkn;df;hdfhkn;dfm;dfhbkdfmkgfmkgfmk;dg;gk
     *  ;gmbd lfmfz's
     *  fgmrtkds'
     *  fg,;tls
     *  d
     *  d
     *  d
     *  d
     *  fgmtkmsd
     *  mdlmtlds
     *  mfltydd
     *  m,fl'se
     *  erdfsr
     *  gsrrfg
     *  rsegsr
     *  grsegr
     *  grsgr
     *  gserge
     *  gesrg
     *  gsr
     *  sreg
     *  rsegt
     *  gtrseyh
     *  tshbtsh
     *  thsehy
     *  juydyhj
     *  uyhjju
     *  trdshj
     *  trsdhsdrh
     *  trsdhdr
     *  rsdth
     *  sdrh
     *  drh
     *  uj
     *  ty
     *  s
     *  stg
     *  hrs
     *  yts
     *  ydjry
     *  dryjyjd
     *  ydjdtsr
     *  ssjotjnh
     *  yrsdhmklt
     *  tltlmnhth
     *  thtsdhdr
     *  tdrhthdtrh
     *  hdrttdthd
     *  htdrhdtth
     *  htdthtt
     *  rthdrtrthrth
     *  rthtrtrrtrt
     *
     */
    public static void reset() {
        for (FoodGroup group : FoodGroup.values()) {
            if (group.isValid()) {
                NUTRITION_VALUES.put(group, 0);
            }
        }
    }
}
