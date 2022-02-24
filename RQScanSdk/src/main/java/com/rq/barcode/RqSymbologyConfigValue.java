package com.rq.barcode;

/**
 * barcode配置对应值，主要是checksum属性
 */
public class RqSymbologyConfigValue {

    public static class NormalCheckSum{
        public final static String Checksum_Disabled = "0";
        public final static String Checksum_Enabled = "1";
        public final static String Checksum_EnabledStripCheckCharacter = "2";
        public static String toString(String tag) {
            if(Checksum_Disabled.equals(tag)) {
                return "Disabled";
            } else if(Checksum_Enabled.equals(tag)) {
                return "Enabled";
            } else if(Checksum_EnabledStripCheckCharacter.equals(tag)) {
                return "EnabledStripCheckCharacter";
            }
            return null;
        }
    }

    public final static class MSIPlessy{
        public final static String MSIPlesseyPropertiesChecksum_Disabled = "0";
        public final static String MSIPlesseyPropertiesChecksum_DisabledMod10 = "1";
        public final static String MSIPlesseyPropertiesChecksum_EnabledMod10 = "2";
        public final static String MSIPlesseyPropertiesChecksum_DisabledMod10_10 = "3";
        public final static String MSIPlesseyPropertiesChecksum_EnabledMod10_10 = "4";
        public final static String MSIPlesseyPropertiesChecksum_DisabledMod11_10 = "5";
        public final static String MSIPlesseyPropertiesChecksum_EnabledMod11_10 = "6";

        public static String toString(String tag) {
            if(MSIPlesseyPropertiesChecksum_Disabled.equals(tag)) {
                return "Disabled";
            } else if(MSIPlesseyPropertiesChecksum_DisabledMod10.equals(tag)) {
                return "DisabledMod10";
            } else if(MSIPlesseyPropertiesChecksum_EnabledMod10.equals(tag)) {
                return "EnabledMod10";
            } else if(MSIPlesseyPropertiesChecksum_DisabledMod10_10.equals(tag)) {
                return "DisabledMod10_10";
            } else if(MSIPlesseyPropertiesChecksum_EnabledMod10_10.equals(tag)) {
                return "EnabledMod10_10";
            } else if(MSIPlesseyPropertiesChecksum_DisabledMod11_10.equals(tag)) {
                return "DisabledMod11_10";
            } else if(MSIPlesseyPropertiesChecksum_EnabledMod11_10.equals(tag)) {
                return "EnabledMod11_10";
            }
            return null;
        }
    }

    public final static class Code11{
        public final static String Code11PropertiesChecksum_Disabled = "0";
        public final static String Code11PropertiesChecksum_Disabled1Digit = "1";
        public final static String Code11PropertiesChecksum_Enabled1Digit = "2";
        public final static String Code11PropertiesChecksum_Disabled2Digit = "3";
        public final static String Code11PropertiesChecksum_Enabled2Digit = "4";

        public static String toString(String tag) {
            if(Code11PropertiesChecksum_Disabled.equals(tag)) {
                return "Disabled";
            } else if(Code11PropertiesChecksum_Disabled1Digit.equals(tag)) {
                return "Disabled1Digit";
            } else if(Code11PropertiesChecksum_Enabled1Digit.equals(tag)) {
                return "Enabled1Digit";
            } else if(Code11PropertiesChecksum_Disabled2Digit.equals(tag)) {
                return "Disabled2Digit";
            } else if(Code11PropertiesChecksum_Enabled2Digit.equals(tag)) {
                return "Enabled2Digit";
            }
            return null;
        }
    }

}
