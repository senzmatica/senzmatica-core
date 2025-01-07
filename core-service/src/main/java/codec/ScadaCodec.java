package codec;

public class ScadaCodec {

    public String convert(Object obj) {
        try {

            String code = obj.toString();
            int indexD2 = code.indexOf("d2");
            if (indexD2 != -1) {
                int indexComma = code.indexOf(",", indexD2);
                if (indexComma != -1) {
                    String substring = code.substring(indexD2, indexComma + 1);
                    code = code.substring(0, indexD2) + code.substring(indexComma + 1);
                } else {
                    System.out.println("Comma not found after d2=");
                }
            } else {
                System.out.println("d2= not found in the input string.");
            }

            int indexD3 = code.indexOf("d3");
            if (indexD3 != -1) {
                int indexComma = code.indexOf(",", indexD3);
                if (indexComma != -1) {
                    String substring = code.substring(indexD3, indexComma + 1);
                    code = code.substring(0, indexD3) + code.substring(indexComma + 1);
                } else {
                    System.out.println("Comma not found after d2=");
                }
            } else {
                System.out.println("d3= not found in the input string.");
            }


            code = code.replaceAll("\\bd1\\b", "0-ID_MSG");
            //code = code.replaceAll("\\bd2\\b", "1-ID_IV");
            //code = code.replaceAll("\\bd3\\b", "2-RT");
            code = code.replaceAll("\\bd4\\b", "1-AP");
            code = code.replaceAll("\\bd5\\b", "2-CP_A");
            code = code.replaceAll("\\bd6\\b", "3-CP_B");
            code = code.replaceAll("\\bd7\\b", "4-CP_C");
            code = code.replaceAll("\\bd8\\b", "5-VP_AN");
            code = code.replaceAll("\\bd9\\b", "6-VP_BN");
            code = code.replaceAll("\\bd10\\b", "7-VP_CN");
            code = code.replaceAll("\\bd11\\b", "8-AC");
            code = code.replaceAll("\\bd12\\b", "9-LF");
            code = code.replaceAll("\\bd13\\b", "10-AC_AP");
            code = code.replaceAll("\\bd14\\b", "11-AC_RP");
            code = code.replaceAll("\\bd15\\b", "12-AC_PF");
            code = code.replaceAll("\\bd16\\b", "13-AC_E");
            code = code.replaceAll("\\bd17\\b", "14-DC_C");
            code = code.replaceAll("\\bd18\\b", "15-DC_V");
            code = code.replaceAll("\\bd19\\b", "16-DC_P");
            code = code.replaceAll("\\bd20\\b", "17-T_C");
            code = code.replaceAll("\\bd21\\b", "18-OS");
            code = code.replaceAll("\\bd22\\b", "19-S_D");
            code = code.replaceAll("\\bd23\\b", "20-V_PCC");
            code = code.replaceAll("\\bd24\\b", "21-S_PV");
            code = code.replaceAll("\\bd25\\b", "22-S_ECP");
            code = code.replaceAll("\\bd26\\b", "23-CC");
            code = code.replaceAll("\\bd27\\b", "24-T_RPI");
            code = code.replaceAll("\\bd28\\b", "25-S_M");
            code = code.replaceAll("\\bd29\\b", "26-S_U");
            code = code.replaceAll("\\be1\\b", "0-ID_MSG");
            code = code.replaceAll("\\be2\\b", "1-ID_IV");
            code = code.replaceAll("\\be3\\b", "2-RT");
            code = code.replaceAll("\\be4\\b", "3-EF");
            code = code.replaceAll("\\be5\\b", "4-S_U");

            String trimmed = code.substring(1, code.length() - 1);
            String replacedComma = trimmed.replaceAll(",", ";");
            String replaceInComma = replacedComma.replaceAll("\"", "");
            String removedSpace = replaceInComma.replaceAll(" ", "");
            return removedSpace;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Successfully Removed";
    }

    public static void main(String[] args) {
        ScadaCodec dataDecoder = new ScadaCodec();

        String input = "{\"d10\":0,\"d12\":0,\"d11\":0,\"d14\":0,\"d13\":0,\"d16\":0,\"d15\":0,\"d18\":0,\"d17\":0,\"d1\":6807,\"d19\":0,\"d2\":0,\"d3\":\"2024-03-07 09:49:19\",\"d4\":0,\"d5\":0,\"d6\":0,\"d7\":0,\"d8\":0,\"d9\":0,\"d21\":0,\"d20\":0,\"d23\":0,\"d22\":0,\"d25\":0,\"d24\":0,\"d27\":-263.2,\"d26\":0,\"d29\":0,\"d28\":0}";

        System.out.println(dataDecoder.convert(input));

    }
}
