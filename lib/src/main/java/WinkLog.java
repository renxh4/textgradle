public class WinkLog {

    public static String TEXT_RESET = "\u001B[0m";

    public static String TEXT_YELLOW = "\u001B[33m";

    public static String TEXT_GREEN = "\u001B[32m";

    public static String TEXT_RED = "\u001B[31m";



    public static void d(String tag, String msg) {
//        System.out.println(tag + ":" + msg);

        System.out.println(TEXT_YELLOW + tag + ":" + msg + TEXT_RESET);
    }

    public static void d(String msg) {
        System.out.println(TEXT_RED+msg+TEXT_RESET);
    }

    public static void i(String msg) {
        System.out.println(TEXT_RED+msg+TEXT_RESET);
    }

    public static void w(String msg) {
        System.out.println(msg);


    }
}
