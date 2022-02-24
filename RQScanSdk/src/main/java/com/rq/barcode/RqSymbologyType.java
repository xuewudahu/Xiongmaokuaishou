package com.rq.barcode;

/**
 * 条码枚举，包含所有的条码
 */
public enum RqSymbologyType {

    SymbologyType_Undefined(0,"Undefined",""),
    SymbologyType_GC(1,"SymbologyType_GC",""),
    SymbologyType_DataMatrix(2,"data_matrix",""),
    SymbologyType_QR(3,"qr_code",""),
    SymbologyType_Aztec(4,"aztec",""),
    SymbologyType_MC(5,"SymbologyType_MC",""),
    SymbologyType_PDF417(6,"pdf417",""),
    SymbologyType_MPDF(7,"micropdf417",""),
    SymbologyType_Code39(8,"code39",""),
    SymbologyType_Interleaved2of5(9,"interleaved_2_of_5",""),
    SymbologyType_Codabar(10,"codabar",""),
    SymbologyType_UPCA(11,"upc-a",""),
    SymbologyType_UPCE(12,"upc-e",""),
    SymbologyType_EAN13(13,"ean13",""),
    SymbologyType_EAN8(14,"ean8",""),
    SymbologyType_DB14(15,"SymbologyType_DB14",""),
    SymbologyType_CCA(16,"cca",""),
    SymbologyType_CCB(17,"ccb",""),
    SymbologyType_CCC(18,"ccc",""),
    SymbologyType_DataBarStacked(19,"SymbologyType_DataBarStacked",""),
    SymbologyType_DataBarLimited(20,"SymbologyType_DataBarLimited",""),
    SymbologyType_DataBarExpanded(21,"SymbologyType_DataBarExpanded",""),
    SymbologyType_DataBarExpandedStacked(22,"SymbologyType_DataBarExpandedStacked",""),
    SymbologyType_HanXin(23,"hanxin_code",""),
    SymbologyType_QRMicro(24,"microqr",""),
    SymbologyType_QRModel1(25,"SymbologyType_QRModel1",""),
    SymbologyType_CustomNC(26,"SymbologyType_CustomNC",""),
    SymbologyType_Custom02(27,"SymbologyType_Custom02",""),
    SymbologyType_Extended(28,"SymbologyType_Extended",""),
    SymbologyType_Code11(29,"code11",""),
    SymbologyType_Code32(30,"code32",""),
    SymbologyType_Plessy(31,"plessy",""),
    SymbologyType_MSIPlessy(32,"msi_plessey",""),
    SymbologyType_Telepen(33,"telepen",""),
    SymbologyType_Trioptic(34,"trioptic",""),
    SymbologyType_Pharmacode(35,"SymbologyType_Pharmacode",""),
    SymbologyType_Matrix2of5(36,"matrix_2_of_5",""),
    SymbologyType_Straight2of5(37,"straight_2_of_5",""),
    SymbologyType_Code49(38,"code49",""),
    SymbologyType_Codr16k(39,"SymbologyType_Codr16k",""),
    SymbologyType_CodablockF(40,"codablockf",""),
    SymbologyType_USPSPostnet(41,"usps_postnet",""),
    SymbologyType_USPSPlanet(42,"usps_planet",""),
    SymbologyType_USPSIntelligentMail(43,"usps_intelligent_mail",""),
    SymbologyType_AustraliaPost(44,"australia_post",""),
    SymbologyType_DutchPost(45,"dutch_post",""),
    SymbologyType_JapanMail(46,"japan_mail",""),
    SymbologyType_RoyalMail(47,"royal_mail",""),
    SymbologyType_UPU(48,"upu",""),
    SymbologyType_KoreaPost(49,"korea_post",""),
    SymbologyType_HongKong2of5(50,"hong_kong_2_of_5",""),
    SymbologyType_NEC2of5(51,"nec_2_of_5",""),
    SymbologyType_IATA2of5(52,"iata_2_of_5",""),
    SymbologyType_CanadaPost(53,"canada_post",""),
    SymbologyType_Pro1(54,"SymbologyType_Pro1",""),
    SymbologyType_BC412(55,"SymbologyType_BC412",""),
    SymbologyType_GridMatrix(56,"grid_matrix",""),
    SymbologyType_DataBarStacked_CCA(57,"SymbologyType_DataBarStacked_CCA",""),
    SymbologyType_DataBarStacked_CCB(58,"SymbologyType_DataBarStacked_CCB",""),
    SymbologyType_DataBarStacked_CCC(59,"SymbologyType_DataBarStacked_CCC",""),
    SymbologyType_DataBarLimited_CCA(60,"SymbologyType_DataBarLimited_CCA",""),
    SymbologyType_DataBarLimited_CCB(61,"SymbologyType_DataBarLimited_CCB",""),
    SymbologyType_DataBarLimited_CCC(62,"SymbologyType_DataBarLimited_CCC",""),
    SymbologyType_DataBarExpanded_CCA(63,"SymbologyType_DataBarExpanded_CCA",""),
    SymbologyType_DataBarExpanded_CCB(64,"SymbologyType_DataBarExpanded_CCB",""),
    SymbologyType_DataBarExpanded_CCC(65,"SymbologyType_DataBarExpanded_CCC",""),
    SymbologyType_DataBarExpandedStacked_CCA(66,"SymbologyType_DataBarExpandedStacked_CCA",""),
    SymbologyType_DataBarExpandedStacked_CCB(67,"SymbologyType_DataBarExpandedStacked_CCB",""),
    SymbologyType_DataBarExpandedStacked_CCC(68,"SymbologyType_DataBarExpandedStacked_CCC",""),
    SymbologyType_DB14_CCA(69,"SymbologyType_DB14_CCA",""),
    SymbologyType_DB14_CCB(70,"SymbologyType_DB14_CCB",""),
    SymbologyType_DB14_CCC(71,"SymbologyType_DB14_CCC",""),
    SymbologyType_Code128_CCA(72,"code128",""),
    SymbologyType_Code128_CCB(73,"code128_ccb",""),
    SymbologyType_Code128_CCC(74,"code128_ccc",""),
    SymbologyType_UPCA_CCA(75,"SymbologyType_UPCA_CCA",""),
    SymbologyType_UPCA_CCB(76,"SymbologyType_UPCA_CCB",""),
    SymbologyType_UPCA_CCC(77,"SymbologyType_UPCA_CCC",""),
    SymbologyType_UPCE_CCA(78,"SymbologyType_UPCE_CCA",""),
    SymbologyType_UPCE_CCB(79,"SymbologyType_UPCE_CCB",""),
    SymbologyType_UPCE_CCC(80,"SymbologyType_UPCE_CCC",""),
    SymbologyType_EAN8_CCA(81,"SymbologyType_EAN8_CCA",""),
    SymbologyType_EAN8_CCB(82,"SymbologyType_EAN8_CCB",""),
    SymbologyType_EAN8_CCC(83,"SymbologyType_EAN8_CCC",""),
    SymbologyType_EAN13_CCA(84,"SymbologyType_EAN13_CCA",""),
    SymbologyType_EAN13_CCB(85,"SymbologyType_EAN13_CCB",""),
    SymbologyType_EAN13_CCC(86,"SymbologyType_EAN13_CCC",""),
    SymbologyType_TLC39(87,"SymbologyType_TLC39",""),
    SymbologyType_MAXICODE(88,"maxi_code",""),
    SymbologyType_Code93(89,"code93",""),
    SymbologyType_DOT(90,"dot_code",""),
    SymbologyType_GS1_DATABAR(91,"gs1_databar",""),
    SymbologyType_COMPOSITE(92,"composite_code","");

    private int mCode;
    private String mSampleName;
    private String mOtherName;

    RqSymbologyType(int code, String sampleName, String otherName) {
        this.mCode = code;
        this.mSampleName = sampleName;
        this.mOtherName = otherName;
    }

    public String toString(int decodeProgram){
        if(decodeProgram == RqEngineer.CORTEX) {
            return name();
        } else {
            return mOtherName;
        }
    }

    public String getSampleName(){
        return mSampleName;
    }

    public static RqSymbologyType getOrderStatusEnum(int decodeProgram,String codeStr) {
        for (RqSymbologyType orderStatusEnum: RqSymbologyType.values()) {
            if (orderStatusEnum.toString(decodeProgram).equals(codeStr)) {
                return orderStatusEnum;
            }
        }
        return null;
    }
}
