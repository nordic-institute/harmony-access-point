package eu.domibus.test.util;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class Test {
    public static void main(String[] args) {
        String tmp="";
        for(int i=0;i<1000;i++){
            tmp+="'"+i+"',";
        }
        System.out.println(tmp);
    }
}
